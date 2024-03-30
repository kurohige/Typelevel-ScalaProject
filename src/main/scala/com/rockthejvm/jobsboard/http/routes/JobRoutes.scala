package com.rockthejvm.jobsboard.http.routes

import io.circe.generic.auto.*

import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.effect.*
import cats.implicits.*

import scala.collection.mutable
import java.util.UUID
import com.rockthejvm.jobsboard.domain.job.*
import com.rockthejvm.jobsboard.http.responses.*

import com.rockthejvm.jobsboard.logging.syntax.*
import org.typelevel.log4cats.Logger

class JobRoutes[F[_]: Concurrent: Logger] extends Http4sDsl[F] {
  /*
    in the following endpoints we use "case req @" to match the request and then use the for comprehension to extract
    the body of the request.
   */

  // "database"
  private val database = mutable.Map[UUID, Job]()

  // POST /jobs?offset=x&limit=y { filters }  // TODO: add query params and filtesr
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root =>
    Ok(database.values)
  }

  // GET /jobs/{uuid}
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    database.get(id) match {
      case Some(job) => Ok(job)
      case None      => NotFound(FailureResponse(s"Job with ID $id not found"))
    }
  }

  // POST /jobs / create { job }
  private def createJob(jobInfo: JobInfo): F[Job] =
    Job(
      id = UUID.randomUUID(),
      date = System.currentTimeMillis(),
      ownerEmail = "TODO@rockthejvm.com",
      jobInfo = jobInfo,
      active = true
    ).pure[F]

  import com.rockthejvm.jobsboard.logging.syntax.*
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      for {
        jobInfo <- req.as[JobInfo].logError(e => s"Parsing payload failed: $e")
        job     <- createJob(jobInfo).logError(e => s"Creating job failed: $e")
        _       <- database.put(job.id, job).pure[F]
        resp    <- Created(job.id)
      } yield resp
  }

  // PUT /jobs/{uuid} { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(job) =>
          for {
            jobInfo <- req.as[JobInfo]
            _       <- database.put(id, job.copy(jobInfo = jobInfo)).pure[F]
            resp    <- Ok()
          } yield resp
        case None => NotFound(FailureResponse(s"Cannot update job at $id, job not found"))
      }
  }

  // DELETE /jobs/{uuid}
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(_) =>
          for {
            _    <- database.remove(id).pure[F]
            resp <- Ok()
          } yield resp
        case None => NotFound(FailureResponse(s"Cannot delete job at $id, job not found"))
      }
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent: Logger]: JobRoutes[F] = new JobRoutes[F]
}
