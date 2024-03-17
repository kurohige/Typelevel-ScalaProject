package com.rockthejvm.foundations

import cats.{Defer, MonadError}
import cats.effect.{
  Concurrent,
  Deferred,
  Fiber,
  GenSpawn,
  IO,
  IOApp,
  MonadCancel,
  Ref,
  Resource,
  Spawn,
  Sync,
  Temporal
}

import java.io.{File, FileWriter, PrintWriter}
import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.concurrent.duration.*
import scala.util.Random

object CatsEffect extends IOApp.Simple {

  /*
    describing computation as values
   */
  // IO = is a data structure describing arbitrary computations (including side effects)

  val firstIO: IO[Int] = IO.pure(42) // pure value
  val delayedIO: IO[Int] = IO {
    // complex code
    println("I'm just about to produce the meaning of life")
    42
  }

  def evaluateIO[A](io: IO[A]): Unit = {
    import cats.effect.unsafe.implicits.global // import the default execution context
    val meaningOfLife = io.unsafeRunSync()
    println(s"the result of the effect is: $meaningOfLife")
  }

  // transformations
  // map + flatmap
  val improvedMeaningOfLife: IO[Int] = firstIO.map(_ * 2)
  val printedMeaningOfLife: IO[Unit] = firstIO.flatMap(mol => IO(println(mol)))

  // for - comprehensions
  def smallProgram(): IO[Unit] = for {
    line1 <- IO(StdIn.readLine())
    line2 <- IO(StdIn.readLine())
    _     <- IO(println(line1 + line2))

  } yield ()

  // old style of standard scala apps
//  def main(args: Array[String]): Unit = {
//    println("Enter two lines: ")
//    evaluateIO(smallProgram())
//  }

  // raise/"catch" errors
  val aFailure: IO[Int] = IO.raiseError(new RuntimeException("a proper failure"))
  val dealWithIT = aFailure.handleErrorWith { case _: RuntimeException =>
    IO(println("I'm still here, no worries"))
  }

  // fibers = "lightweight threads"
  val delayedPrint: IO[Unit] = IO.sleep(1.second) *> IO(println(Random.nextInt(100)))
  val manyPrints = for {
    fib1 <- delayedPrint.start
    fib2 <- delayedPrint.start
    _    <- fib1.join
    _    <- fib2.join
  } yield ()

  val cancelledFiber = for {
    fib <- delayedPrint.onCancel(IO(println("I'm cancelled"))).start
    _   <- IO.sleep(500.millis) *> IO(println("Cancelling!")) *> fib.cancel
    _   <- fib.join
  } yield ()

  // uncancellation
  val ignoredCancellation = for {
    fib <- IO.uncancelable(_ => delayedPrint.onCancel(IO(println("I'm cancelled")))).start
    _   <- IO.sleep(500.millis) *> IO(println("Cancelling!")) *> fib.cancel
    _   <- fib.join
  } yield ()

  // resources
  val readingResource = Resource.make(
    IO(scala.io.Source.fromFile("src/main/scala/com/rockthejvm/foundations/CatsEffect.scala"))
  )(source => IO(source.close()))

  val readingEffect = readingResource.use { source =>
    IO(source.getLines().foreach(println))
  }

  // compose resources
  val copiedFileResource = Resource.make(
    IO(new PrintWriter(new FileWriter(new File("src/main/resources/dumpedFile.scala"))))
  )(writer => IO(println("closing duplicated file")) *> IO(writer.close()))

  val compositeResource = for {
    soruce      <- readingResource
    destination <- copiedFileResource
  } yield (soruce, destination)

  val copyFileEffect = compositeResource.use { case (source, destination) =>
    IO(source.getLines().foreach(destination.println))
  }

  // abstract kind s of computations
  trait MyMonadCancel[F[_], E] extends MonadError[F, E] {
    trait CancellationFlagResetter {
      def apply[A](fa: F[A]): F[A] // with the cancellation flag reset
    }

    def canceled: F[Unit]
    def uncancelable[A](poll: CancellationFlagResetter => F[A]): F[A]
  }

  // monadCancel for IO
  val monadCancelIO: MonadCancel[IO, Throwable] = MonadCancel[IO]
  val uncancelableIO = monadCancelIO.uncancelable(_ =>
    IO(println("I'm uncancelable")) *> IO(42)
  ) // same as IO.uncancelable(...)

  // spawn = ability to create fibers
  trait MyGenSpawn[F[_], E] extends MonadCancel[F, E] {
    def start[A](fa: F[A]): F[Fiber[F, E, A]] // creates a fiber
    // never, cede, racepair
  }

  trait MySpawn[F[_]] extends GenSpawn[F, Throwable]

  val spawnIO = Spawn[IO]
  val fiber   = spawnIO.start(delayedPrint) // creates a fiber, same as delayedPrint.start

  // concurrent = concurrency primitives ( atomic references + promises)
  trait MyConcurrent[F[_]] extends Spawn[F] {
    def ref[A](a: A): F[Ref[F, A]]
    def deferred[A]: F[Deferred[F, A]]
  }

  // Temporal = ability to suspend computations for a given time
  trait MyTemporal[F[_]] extends Concurrent[F] {
    def sleep(time: FiniteDuration): F[Unit]
  }

  // Sync is the ability to suspend synchronous computations arbitrary expressions in an effect
  trait MySync[F[_]] extends MonadCancel[F, Throwable] with Defer[F] {
    def delay[A](expression: => A): F[A]
    def blocking[A](expression: => A): F[A] // runs on a dedicated thread pool
  }

  // Async = ability to suspend asynchronous computations(i.e. an other thread pools) into an effect managed by the effect system
  trait MyAsync[F[_]] extends Sync[F] with Temporal[F] {
    def executionContext: F[ExecutionContext]
    def async[A](cb: (Either[Throwable, A] => Unit) => Unit): F[A]
  }

  // CE apps have a "run" method returnin an IO, which will internally be evaluated in main function
  override def run = copyFileEffect

}
