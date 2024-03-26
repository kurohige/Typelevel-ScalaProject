package com.rockthejvm.jobsboard.http

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.Monad
import com.comcast.ip4s.Literals.idn
import cats.implicits.*

import com.rockthejvm.jobsboard.http.routes.*

class HttpApi[F[_]: Monad] private {
  private val healthRoutes = HealthRoutes[F]
  private val jobRoutes    = JobRoutes[F]

  // when updating the routes on application ember server, we will list the endpoints and routes as:
  // .withHttpApp(HttpApi[IO].endpoints.orNotFound)
  val endpoints = Router(
    "/api" -> (healthRoutes.routes <+> jobRoutes.routes)
  )
}

object HttpApi {
  def apply[F[_]: Monad]: HttpApi[F] = new HttpApi[F]
}
