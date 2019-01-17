package t3

import cats.instances.list._
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Properties}
import t3.plainlist._

object LogicSpec extends Properties("t3.Logic") {
  implicit val arbitraryCells: Arbitrary[List[Logic.Cell]] =
    Arbitrary(Gen.listOf(Gen.oneOf(Logic.cells)))

  property("doesn't throw") = forAll { cells: List[Logic.Cell] =>
    val (ts: List[_], gs: List[_]) = Logic.game(List(cells))
    true
  }
}
