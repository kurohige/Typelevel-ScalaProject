package com.rockthejvm.jobsboard

import cats.*
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*

import cats.Monad
import cats.effect.{IO, IOApp}
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.headers.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.circe.*
import org.typelevel.ci.CIString
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import pureconfig.ConfigSource
import com.rockthejvm.jobsboard.config.*
import com.rockthejvm.jobsboard.config.syntax.*
import com.rockthejvm.jobsboard.logging.syntax.*

import pureconfig.error.ConfigReaderException
import com.rockthejvm.jobsboard.modules.*
import com.rockthejvm.foundations.Doobie.xa

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run = ConfigSource.default.loadF[IO, AppConfig].flatMap {
    case AppConfig(postgressConfig, emberConfig) =>
      val appResource = for {
        xa      <- Database.makePostgresResource[IO](postgressConfig)
        core    <- Core[IO](xa)
        httpApi <- HttpApi[IO](core)
        server <- EmberServerBuilder
          .default[IO]
          .withHost(emberConfig.host)
          .withPort(emberConfig.port)
          .withHttpApp(httpApi.endpoints.orNotFound)
          .build
      } yield server

      appResource.use(_ => IO.println("Rock the JVM!") *> IO.never)
  }
}
