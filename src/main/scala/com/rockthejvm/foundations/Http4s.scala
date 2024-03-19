package com.rockthejvm.foundations

import cats.effect.IOApp
object Http4s extends IOApp.Simple {

  // simulate an HTTP server with "students" and "courses"
  type Student = String
  case class Instructor(name: String, lastName: String)
  case class Course(id: String, title: String, instructor: Instructor, students: List[Student])
  override def run: cats.effect.IO[Unit] = ???
}
