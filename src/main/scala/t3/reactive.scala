package t3
import java.util.concurrent.atomic.AtomicReference

import cats.{Functor, FunctorFilter, Monad, StackSafeMonad}
import rx.lang.scala.Observable
import rx.lang.scala.subjects.PublishSubject
import t3.Logic.{Cell, Player}

object reactive {
  implicit lazy val sourceMonadAndAlternative: Monad[Observable] =
    new StackSafeMonad[Observable] {
      override def pure[A](x: A): Observable[A] = Observable.just(x)
      override def flatMap[A, B](fa: Observable[A])(
        f: A => Observable[B]
      ): Observable[B] = fa.flatMap(f)
    }

  implicit lazy val sourceFunctorFilter: FunctorFilter[Observable] =
    new FunctorFilter[Observable] {
      override def functor: Functor[Observable] = Monad[Observable]
      override def mapFilter[A, B](fa: Observable[A])(
        f: A => Option[B]
      ): Observable[B] = fa collect Function.unlift(f)
    }

  implicit lazy val observableMoreUtils: MoreUtils[Observable] =
    new MoreUtils[Observable] {
      override def scanLeft[A, B](fa: Observable[A])(z: B)(
        op: (B, A) => B
      ): Observable[B] = fa.scan(z)(op)

      override def zip[A, B](fa: Observable[A])(
        fb: Observable[B]
      ): Observable[(A, B)] = fa zip fb

      override def of[A](values: A*): Observable[A] =
        Observable.just(values: _*)

      override def takeWhile[A](fa: Observable[A])(
        p: A => Boolean
      ): Observable[A] = fa.takeWhile(p)

      override def takeUntil[A](fa: Observable[A])(
        p: A => Boolean
      ): Observable[A] = fa.takeUntil(!p(_))
    }

  def slideRounds(cells: Observable[Cell],
                  newGame: Observable[Unit]): Observable[Observable[Cell]] = {
    val slided = PublishSubject[Observable[Cell]]
    val round = new AtomicReference(PublishSubject[Cell])

    for (() <- newGame) {
      round.get().onCompleted()
      round set PublishSubject[Cell]
      slided.onNext(round.get())
    }

    for (cell <- cells) {
      round.get().onNext(cell)
    }

    slided
  }

  def game(
    cells: Observable[Cell],
    newGame: Observable[Unit]
  ): (Observable[(Cell, Player)], Observable[Any]) = {
    Logic.game(slideRounds(cells, newGame))
  }

}
