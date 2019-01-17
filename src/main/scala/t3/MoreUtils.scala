package t3
import simulacrum.typeclass

@typeclass trait MoreUtils[F[_]] {
  def scanLeft[A, B](fa: F[A])(z: B)(op: (B, A) => B): F[B]
  def takeWhile[A](fa: F[A])(p: A => Boolean): F[A]
  def takeUntil[A](fa: F[A])(p: A => Boolean): F[A]
  def zip[A, B](fa: F[A])(fb: F[B]): F[(A, B)]
  def of[A](values: A*): F[A]
}
