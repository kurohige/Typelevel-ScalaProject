package com.rockthejvm.foundations

import cats.effect.{IO, IOApp}
import doobie.util.transactor.Transactor
object Doobie extends IOApp.Simple {

  case class Student(id: Int, name: String)

  // we'll need a transactor
  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",                 // JDBC connector
    "jdbc:postgresql://localhost:5432/demo", // database URL
    "docker",                                // username
    "docker"                                 // password
  )

  // read
  def findAllStudentNames: IO[List[String]] = {
    import doobie.implicits._
    import doobie.util.fragment.Fragment
    val query: doobie.Query0[String] = sql"SELECT name FROM students".query[String]
    val action                       = query.to[List]
    action.transact(xa)
  }

  // write
  def saveStudent(id: Int, name: String): IO[Int] = {
    import doobie.implicits._
    val query  = sql"INSERT INTO students VALUES ($id, $name)"
    val action = query.update.run
    action.transact(xa)
  }

  // read as CC with fragments
  def findStudentsByInitial(letter: String): IO[List[Student]] = {
    import doobie.implicits._
    val selectPart = fr"Select id, name"
    val fromPart   = fr"FROM students"
    val wherePart  = fr"WHERE left(name, 1) = $letter"

    val statement = (selectPart ++ fromPart ++ wherePart)
    val action    = statement.query[Student].to[List]
    action.transact(xa)
  }

  override def run: cats.effect.IO[Unit] = findStudentsByInitial("m").flatMap { students =>
    IO(println(students))
  }

}
