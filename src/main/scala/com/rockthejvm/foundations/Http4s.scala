package com.rockthejvm.foundations

import cats.effect.IOApp
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



  override def run: cats.effect.IO[Unit] = ???
}
