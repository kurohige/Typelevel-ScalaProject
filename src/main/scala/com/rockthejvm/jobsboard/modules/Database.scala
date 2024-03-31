package com.rockthejvm.jobsboard.modules

import com.rockthejvm.jobsboard.config.*
import cats.effect.*
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.*
import org.typelevel.log4cats.Logger

object Database {
  def makePostgresResource[F[_]: Async: Logger](
      config: PostgresConfig
  ): Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool(config.nThreads)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      config.url,
      config.user,
      config.password,
      ec
    )
  } yield xa
}
