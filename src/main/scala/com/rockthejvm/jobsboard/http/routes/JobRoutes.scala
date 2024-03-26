package com.rockthejvm.jobsboard.http.routes

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.Monad

class JobRoutes[F[_]: Monad] extends Http4sDsl[F] {
  // POST /jobs?offset=x&limit=y { filters }  // TODO: add query params and filtesr
  private val allJobsRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] { case GET -> Root / "jobs" =>
      Ok("All jobs!")
    }
  }

  // GET /jobs/{uuid}
  private val jobRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] { case GET -> Root / "jobs" / UUIDVar(uuid) =>
      Ok(s"Job $uuid")
    }
  }

  // POST /jobs { job }
  private val createJobRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] { case req @ POST -> Root / "jobs" =>
      Ok("Job created!")
    }
  }

  // PUT /jobs/{uuid} { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] { case req @ PUT -> Root / "jobs" / UUIDVar(uuid) =>
      Ok(s"Job $uuid updated!")
    }
  }

  // DELETE /jobs/{uuid}
  private val deleteJobRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] { case DELETE -> Root / "jobs" / UUIDVar(uuid) =>
      Ok(s"Job $uuid deleted!")
    }
  }

  val routes = Router(
    "/jobs" -> ??? // complete this
  )
}

object JobRoutes {
  def apply[F[_]: Monad]: HealthRoutes[F] = new HealthRoutes[F]
}
