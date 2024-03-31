package com.rockthejvm.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.*

import com.rockthejvm.jobsboard.core.*
import org.typelevel.log4cats.Logger
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val jobs: Jobs[F])

// postgres -> jobs -> core -> httpApi -> main/app
object Core {

  def apply[F[_]: Async: Logger](xa: Transactor[F]): Resource[F, Core[F]] =
    Resource
      .eval(LiveJobs[F](xa))
      .map(jobs => new Core[F](jobs))
}
