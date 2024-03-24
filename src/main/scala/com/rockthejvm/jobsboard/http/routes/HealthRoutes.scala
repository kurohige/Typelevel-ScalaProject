package com.rockthejvm.jobsboard.http.routes

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.Monad

class HealthRoutes[F[_]: Monad] extends Http4sDsl[F] {
  private val healthRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] { case GET -> Root =>
      Ok("All going well!")
    }
  }

  val routes = Router(
    "/health" -> healthRoute
  )
}

object HealthRoutes {
  def apply[F[_]: Monad]: HealthRoutes[F] = new HealthRoutes[F]
}
