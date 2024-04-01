package com.rockthejvm.jobsboard.fixtures

import cats.data.OptionT
import cats.effect.*
import tsec.authentication.{IdentityStore, JWTAuthenticator, SecuredRequestHandler}
import tsec.mac.jca.HMACSHA256
import tsec.jws.mac.JWTMac

import scala.concurrent.duration.DurationInt
import org.http4s.{AuthScheme, Credentials, Request}
import org.http4s.headers.Authorization

trait SecuredRouteFixture {}
