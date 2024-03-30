package com.rockthejvm.jobsboard.http

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.effect.*
import com.comcast.ip4s.Literals.idn
import cats.implicits.*
import org.typelevel.log4cats.Logger

import com.rockthejvm.jobsboard.logging.syntax.*
import com.rockthejvm.jobsboard.http.routes.*

class HttpApi[F[_]: Concurrent: Logger] private {
  private val healthRoutes = HealthRoutes[F]
  private val jobRoutes    = JobRoutes[F]

  // when updating the routes on application ember server, we will list the endpoints and routes as:
  // .withHttpApp(HttpApi[IO].endpoints.orNotFound)
  val endpoints = Router(
    "/api" -> (healthRoutes.routes <+> jobRoutes.routes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent: Logger]: HttpApi[F] = new HttpApi[F]
}
