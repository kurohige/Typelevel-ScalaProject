package com.rockthejvm.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import pureconfig.error.CannotConvert
import com.comcast.ip4s.Literals.host

// Implement given configReader: ConfigReader[EmberConfig]
final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {
  // need given ConfigReader[Host] and ConfigReader[Port] =>
  //    compiler generates ConfigReader[EmberConfig]
  given hostReader: ConfigReader[Host] =
    ConfigReader[String].emap { hostString =>
      Host
        .fromString(hostString)
        .toRight(
          CannotConvert(
            hostString,
            Host.getClass.toString,
            s"Invalid host string: $hostString"
          )
        )
    }

  /* given hostReader: ConfigReader[Host] =
    ConfigReader[String].emap { hostString =>
      Host.fromString(hostString) match {
        case None =>
          Left(
            CannotConvert(hostString, Host.getClass.toString, s"Invalid host string: $hostString")
          ) // error, return a left
        case Some(host) => Right(host)
      }
    } */

  /*
        Important ===> "toRight()":
        on this code sample, we are using the same approach as the previous one but for pattern matching daniel is teaching us
        a more slick way to do it, so we can refactor the code to be more readable and concise, the name of this approach is by using:
        .toRight() as we know that toLeft will return the error and toRight will return the right value
   */

  /*
        Important ===> "emap()":
        on this code sample, we are using a function directly from the library pureconfig, this function is called "emap" and it is
        used to transform the result of reading a configuration into another type, B, by applying a function f: A => Either[FailureReason, B].
        Either[FailureReason, B] rather than just B. The Either type is used to represent a value that can be one of two things; in this case,
        it's either a FailureReason representing an error or a successfully transformed value of type B.
   */

  given portReader: ConfigReader[Port] =
    ConfigReader[Int].emap { portInt =>
      Port
        .fromInt(portInt)
        .toRight(
          CannotConvert(
            portInt.toString,
            Port.getClass.toString,
            s"Invalid port number: $portInt"
          )
        )
    }

}
