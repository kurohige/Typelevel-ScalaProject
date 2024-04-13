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
import com.rockthejvm.jobsboard.http.validation.syntax.*
import com.rockthejvm.jobsboard.core.*

import com.rockthejvm.jobsboard.logging.syntax.*
import org.typelevel.log4cats.Logger

class JobRoutes[F[_]: Concurrent: Logger] private (jobs: Jobs[F]) extends HttpValidationDsl[F] {
  /*
    in the following endpoints we use "case req @" to match the request and then use the for comprehension to extract
    the body of the request.
   */

  // "database"

  // POST /jobs?offset=x&limit=y { filters }  // TODO: add query params and filtesr
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root =>
    for {
      jobsList <- jobs.all().logError(e => s"Fetching all jobs failed: $e")
      resp     <- Ok(jobsList)
    } yield resp
  }

  // GET /jobs/{uuid}
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    jobs.find(id).flatMap {
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
      req.validate[JobInfo] { jobInfo =>
        for {
          jobInfo <- req.as[JobInfo].logError(e => s"Parsing payload failed: $e")
          jobId <- jobs
            .create("TODO@rockthejvm.com", jobInfo)
            .logError(e => s"Creating job failed: $e")
          resp <- Created(jobId)
        } yield resp
      }
  }

  // PUT /jobs/{uuid} { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      for {
        jobInfo     <- req.as[JobInfo]
        maybeNewJob <- jobs.update(id, jobInfo).logError(e => s"Updating job $id failed: $e")
        resp <- maybeNewJob match {
          case Some(newJob) => Ok(newJob)
          case None         => NotFound(FailureResponse(s"Cannot update job at $id, job not found"))
        }
      } yield resp
  }

  // DELETE /jobs/{uuid}
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      jobs.find(id).flatMap {
        case Some(job) =>
          for {
            _    <- jobs.delete(id).logError(e => s"Deleting job $id failed: $e")
            resp <- Ok(s"Job $id deleted")
          } yield resp
        case None => NotFound(FailureResponse(s"Job with ID $id not found"))
      }
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent: Logger](jobs: Jobs[F]) = new JobRoutes[F](jobs)
}
