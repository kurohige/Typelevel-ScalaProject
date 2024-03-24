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
import com.rockthejvm.jobsboard.http.routes.HealthRoutes

/*
    1 - add a plain health endpoint to our app
    2 - add minimal configuration
    3 - basic http server layout

 */

object Application extends IOApp.Simple {

  override def run: cats.effect.IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHttpApp(HealthRoutes[IO].routes.orNotFound)
    .build
    .use(_ => IO.println("Rock the JVM!") *> IO.never)
}
