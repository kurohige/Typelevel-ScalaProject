package com.rockthejvm.foundations

import cats.Monad
import cats.effect.{IO, IOApp}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{OptionalValidatingQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.ember.server.EmberServerBuilder
object Http4s extends IOApp.Simple {

  // simulate an HTTP server with "students" and "courses"
  type Student = String
  case class Instructor(name: String, lastName: String)
  case class Course(
      id: String,
      title: String,
      year: Int,
      instructor: Instructor,
      students: List[Student]
  )

  object CourseRepository {
    // a "database" of courses
    val catsEffectCourse = Course(
      "cats-effect",
      "Rock the JVM Cats Effect",
      2024,
      Instructor("Daniel", "Daniel"),
      List("Alice", "Bob", "Charlie")
    )

    val courses: Map[String, Course] = Map(
      catsEffectCourse.id -> catsEffectCourse
    )

    // API
    def findCoursesById(courseID: String): Option[Course] = courses.get(courseID)

    def findCoursesByInstructor(instructor: Instructor): List[Course] =
      courses.values.filter(_.instructor == instructor).toList
  }

  // essential REST endpoints
  // GET localhost:8080/courses?instructor=Daniel%200Daniel&year=2024
  // Get localhost:8080/courses/cats-effect/students

  object InstructorQueryParamMatcher
      extends QueryParamDecoderMatcher[String]("instructor") // Daniel%20Daniel
  object YearQueryParamMatcher
      extends OptionalValidatingQueryParamDecoderMatcher[Int]("year") // 2024

  def courseRoutes[F[_]: Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "courses" :? InstructorQueryParamMatcher(
            instructor
          ) +& YearQueryParamMatcher(maybeYear) =>
        val courses = CourseRepository.findCoursesByInstructor(instructor)
        maybeYear match {
          case Some(year) => Ok(courses.filter(_.year == year))
          case None       => Ok(courses)
        }
    }
  }

  override def run: cats.effect.IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHttpApp(courseRoutes[IO].orNotFound)
    .build
    .use(_ => IO.println("Sernver ready!") *> IO.never)
}
