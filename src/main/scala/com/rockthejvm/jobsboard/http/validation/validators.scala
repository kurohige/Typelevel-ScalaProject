package com.rockthejvm.jobsboard.http.validation

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.implicits.*
import com.rockthejvm.jobsboard.domain.job.*

import java.net.URL
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object validators {

  sealed trait ValidationFailure(val erroMessage: String)
  // empty fields, invalid URL, invalid email, etc.
  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]
  case class EmptyField(fieldName: String) extends ValidationFailure(s"$fieldName is empty")
  case class InvalidUrl(fieldName: String)
      extends ValidationFailure(s"$fieldName is not a valid URL")

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  def validateRequired[A](field: A, fieldName: String)(
      required: A => Boolean
  ): ValidationResult[A] =
    if (required(field)) field.validNel
    else EmptyField(fieldName).invalidNel

  def validateUrl(url: String, fieldName: String): ValidationResult[String] =
    Try(URL(url).toURI()) match {
      case Success(_) => url.validNel
      case Failure(e) => InvalidUrl(fieldName).invalidNel
    }

  given jobInfoValidator: Validator[JobInfo] = (jobInfo: JobInfo) => {
    val JobInfo(
      company,     // should not be empty
      title,       // should not be empty
      description, // should not be empty
      externalUrl, // should be a valid URL
      remote,
      location, // should not be empty
      salaryLo,
      salaryHi,
      currency,
      country,
      tags,
      image,
      seniority,
      other
    ) = jobInfo

    val validCompany     = validateRequired(company, "company")(_.nonEmpty)
    val validTitle       = validateRequired(title, "title")(_.nonEmpty)
    val validDescription = validateRequired(description, "description")(_.nonEmpty)
    val validExternalUrl = validateUrl(externalUrl, "externalUrl")
    val validLocation    = validateRequired(location, "location")(_.nonEmpty)

    (
      validCompany,       // company
      validTitle,         // title
      validDescription,   // description
      validExternalUrl,   // externalUrl
      remote.validNel,    // remote
      validLocation,      // location
      salaryLo.validNel,  // salaryLo
      salaryHi.validNel,  // salaryHi
      currency.validNel,  // currency
      country.validNel,   // country
      tags.validNel,      // tags
      image.validNel,     // image
      seniority.validNel, // seniority
      other.validNel      // other
    ).mapN(JobInfo.apply) // ValidatedNel[ValidationFailure, JobInfo]
  }

}
