package com.rockthejvm.jobsboard

import cats.*
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.circe.*
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
import org.typelevel.ci.CIString


/* 
    1 - add a plain health endpoint to our app
    2 - add minimal configuration
    3 - basic http server layout

 */

object Application extends IOApp.Simple{

    def healthEndPoint[F[_]: Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*
    HttpRoutes.of[F] { case GET -> Root / "health" =>
      Ok("All going well!")
    }
  }

  def allRoutes[F[_]: Monad]: HttpRoutes[F] = healthEndPoint[F]

  def routerWithPathPrefixes = Router( "/private" -> healthEndPoint[IO]).orNotFound

  override def run: cats.effect.IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHttpApp(routerWithPathPrefixes)
    .build
    .use(_ => IO.println("Rock the JVM!") *> IO.never)
}