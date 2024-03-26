package com.rockthejvm.jobsboard.http.routes

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.Monad
import com.comcast.ip4s.Literals.idn
import cats.implicits.*

class JobRoutes[F[_]: Monad] extends Http4sDsl[F] {
  // POST /jobs?offset=x&limit=y { filters }  // TODO: add query params and filtesr
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root =>
    Ok("TODO")
  }

  // GET /jobs/{uuid}
  private val jobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    Ok(s"TODO find job for $id")
  }

  // POST /jobs / create { job }
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root / "create" =>
    Ok("TODO")
  }

  // PUT /jobs/{uuid} { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case PUT -> Root / UUIDVar(id) =>
    Ok(s"TODO update job at $id")
  }

  // DELETE /jobs/{uuid}
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      Ok(s"TODO delete job at $id")
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> jobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Monad]: HealthRoutes[F] = new HealthRoutes[F]
}
