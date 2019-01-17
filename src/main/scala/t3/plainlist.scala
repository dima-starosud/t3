package t3

object plainlist {
  implicit lazy val observableMoreUtils: MoreUtils[List] =
    new MoreUtils[List] {
      override def scanLeft[A, B](fa: List[A])(z: B)(op: (B, A) => B): List[B] =
        fa.scanLeft(z)(op)

      override def zip[A, B](fa: List[A])(fb: List[B]): List[(A, B)] = fa zip fb

      override def of[A](values: A*): List[A] =
        List(values: _*)

      override def takeWhile[A](fa: List[A])(p: A => Boolean): List[A] =
        fa.takeWhile(p)

      override def takeUntil[A](fa: List[A])(p: A => Boolean): List[A] = {
        val (t, f) = fa.span(!p(_))
        t ++ f.take(1)
      }
    }
}
