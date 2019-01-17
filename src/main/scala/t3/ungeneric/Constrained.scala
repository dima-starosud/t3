package t3.ungeneric

import scala.language.higherKinds

sealed trait Constrained[A, F[_ <: A]] {
  type T <: A

  def apply(a: A): F[T]
}

object Constrained {
  type Id[A] = A

  def requiring[A](cond: A => Boolean): Constrained[A, Id] = apply[A, Id] { a =>
    require(cond(a))
    a
  }

  def apply[A, F[_ <: A]](c: A => F[A]): Constrained[A, F] =
    new ConstrainedImpl[A, F](c)

  private final class ConstrainedImpl[A, F[_ <: A]](c: A => F[A])
      extends Constrained[A, F] {
    override type T = A
    override def apply(a: A): F[A] = c(a)
  }
}
