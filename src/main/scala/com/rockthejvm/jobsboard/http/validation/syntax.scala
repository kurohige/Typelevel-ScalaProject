package com.rockthejvm.jobsboard.http.validation

import cats.*
import cats.implicits.*
import org.http4s.*
import org.http4s.implicits.*
import validators.*

object syntax {
  extension [F[_]](req: Request[F])
    def validate[A: Validator](serverLogicIfValid: A => F[Response[F]]): F[Response[F]] = ???

}
