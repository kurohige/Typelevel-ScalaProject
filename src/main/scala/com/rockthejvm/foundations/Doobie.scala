package com.rockthejvm.foundations

import cats.effect.kernel.MonadCancelThrow
import cats.effect.{IO, IOApp}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
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

  // organize code
  // define a trait that would act as the repository in the form of a type class
  trait Students[F[_]] {
    def findById(id: Int): F[Option[Student]]
    def findAll: F[List[Student]]
    def create(name: String): F[Int]
  }

  object Students {
    import doobie.implicits._
    def make[F[_]: MonadCancelThrow](xa: Transactor[F]): Students[F] = new Students[F] {
      def findById(id: Int): F[Option[Student]] =
        sql"select id, name from students where id = $id".query[Student].option.transact(xa)

      def findAll: F[List[Student]] =
        sql"select id, name from students".query[Student].to[List].transact(xa)

      def create(name: String): F[Int] =
        sql"insert into students (name) values ($name)".update
          .withUniqueGeneratedKeys[Int]("id")
          .transact(xa)
    }
  }

  val postgresResource = for {
    ce <- ExecutionContexts.fixedThreadPool[IO](16)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/demo",
      "docker",
      "docker",
      ce
    )
  } yield xa

  val smallProgram: IO[Unit] = postgresResource.use { xa =>
    val studentsRepo = Students.make[IO](xa)
    for {
      id   <- studentsRepo.create("Jose")
      jose <- studentsRepo.findById(id)
      _    <- IO(println(s"The first student of RockTheJVM is $jose"))
    } yield ()
  }
  override def run: cats.effect.IO[Unit] = smallProgram

}
