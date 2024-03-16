package com.rockthejvm.foundations

object Cats {

  /*
    Cats is a library for functional programming in Scala.
    type classes
    - Applicative
     - Functor
     - Monad
     - FlatMap
     - ApplicativeError/MonadError
   */

  // functor - "mappable" structures
  trait MyFunctor[F[_]] {
    def map[A, B](initialValue: F[A])(f: A => B): F[B]
  }

  import cats.Functor
  import cats.instances.list.* // import all type class instances for List
  val listFunctor = Functor[List]
  val mappedList  = listFunctor.map(List(1, 2, 3))(_ + 1)

  // generalizable "mappable" structures
  def increment[F[_]](container: F[Int])(implicit functor: Functor[F]): F[Int] =
    functor.map(container)(_ + 1)

  import cats.syntax.functor.* // import the extension methods
  def increment_V2[F[_]: Functor](container: F[Int]): F[Int] = container.map(_ + 1)

  // applicative - pure, wrap existing values into "wrapper" values
  trait MyApplicative[F[_]] extends MyFunctor[F] {
    def pure[A](value: A): F[A]
    def ap[A, B](initialValue: F[A])(f: F[A => B]): F[B]
  }

  import cats.Applicative
  val applicativeList: Applicative[List] = Applicative[List]
  val aSimpleList: List[Int]             = applicativeList.pure(2)
  import cats.syntax.applicative.* // import the extension methods
  val aSimpleList_V2: List[Int] = 42.pure[List]

  // flatMap - "flattenable" structures
  trait MyFlatMap[F[_]] extends MyFunctor[F] {
    def flatMap[A, B](initialValue: F[A])(f: A => F[B]): F[B]
  }

  import cats.FlatMap
  val flatMapList: FlatMap[List] = FlatMap[List]
  val flatMappedList             = flatMapList.flatMap(List(1, 2, 3))(x => List(x, x + 1))

  import cats.syntax.flatMap.* // import the extension methods
  def crossProduct[F[_]: FlatMap, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] =
    containerA.flatMap(a => containerB.map(b => (a, b)))

  def crossProduct_V2[F[_]: FlatMap, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] =
    for {
      a <- containerA
      b <- containerB
    } yield (a, b)

  // Monad - applicative + flatMap
  trait MyMonad[F[_]] extends Applicative[F] with FlatMap[F] {
    override def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(a => pure(f(a)))
  }

  import cats.Monad
  val monadList: Monad[List] = Monad[List]
  def crossProduct_V3[F[_]: Monad, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] =
    for {
      a <- containerA
      b <- containerB
    } yield (a, b)

  // applicative-error - computations that can fail
  trait MyApplicativeError[F[_], E] extends MyApplicative[F] {
    def raiseError[A](e: E): F[A]
  }

  import cats.ApplicativeError
  type ErrorOr[A] = Either[String, A]
  val applicativeEither: ApplicativeError[ErrorOr, String] = ApplicativeError[ErrorOr, String]
  val desiredValue: ErrorOr[Int]                           = applicativeEither.pure(42)
  val failedValue: ErrorOr[Int] = applicativeEither.raiseError("Something went wrong")

  import cats.syntax.applicativeError.* // import the extension methods
  val failedValue_V2: ErrorOr[Int] = "Something went wrong".raiseError[ErrorOr, Int]

  // monad-error - applicative-error + flatMap
  trait MyMonadError[F[_], E] extends ApplicativeError[F, E] with Monad[F]
  import cats.MonadError
  val monadErrorEither: MonadError[ErrorOr, String] = MonadError[ErrorOr, String]

  def main(args: Array[String]): Unit = {}

}
