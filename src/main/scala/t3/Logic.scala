package t3
import cats.implicits._
import cats.{FunctorFilter, Monad}
import com.typesafe.scalalogging.LazyLogging
import t3.MoreUtils.ops._
import t3.ungeneric.Constrained

import scala.language.reflectiveCalls

object Logic extends LazyLogging {
  val Cell = Constrained.requiring[Int](Vector.range(0, 9).contains)
  type Cell = Cell.T

  val cells: Vector[Cell] = Vector.range(0, 9).map(Cell(_))

  sealed trait Player
  object Player {
    final case object X extends Player
    final case object O extends Player
  }

  type Board = Map[Cell, Player]

  implicit final class EvenMoreUtils[F[_]: Monad: MoreUtils: FunctorFilter, A](
    self: F[A]
  ) {
    def unique: F[A] = {
      val occupied = self.scanLeft(Set.empty[A]) { _ + _ }
      occupied zip self collect {
        case (o, c) if !(o contains c) => c
      }
    }
  }

  sealed trait GameState
  final case class NextTurn(player: Player) extends GameState

  sealed trait GameOver extends GameState
  final case object Drawn extends GameOver
  final case class Win(player: Player) extends GameOver

  type WinCombination <: Set[Cell]
  val WinCombinations: Seq[WinCombination] = {
    def c(xs: Int*) = xs.map(Cell(_)).toSet.asInstanceOf[WinCombination]

    Seq(
      // horizontals
      c(0, 1, 2),
      c(3, 4, 5),
      c(6, 7, 8),
      // verticals
      c(0, 3, 6),
      c(1, 4, 7),
      c(2, 5, 8),
      // diagonals
      c(0, 4, 8),
      c(2, 4, 6),
    )
  }

  def playerCapabilities(player: Player, board: Board): Seq[WinCombination] =
    WinCombinations filter { c =>
      (c flatMap board.get) subsetOf Set(player)
    }

  def winners(board: Board): Set[Player] = {
    val winners = WinCombinations map { _.map(board.get).toSeq } collect {
      case Seq(Some(p)) => p
    }
    winners.toSet
  }

  def gameOver(board: Board): Option[GameOver] = {
    lazy val x = playerCapabilities(Player.X, board)
    lazy val o = playerCapabilities(Player.O, board)
    lazy val ws = winners(board)
    if (x.isEmpty && o.isEmpty) Some(Drawn)
    else if (ws.size == 1) Some(Win(ws.head))
    else None
  }

  def players[F[_]: MoreUtils]: F[Player] =
    MoreUtils[F].of(Seq.fill(10)(Seq(Player.X, Player.O)).flatten: _*)

  def round[F[_]: Monad: FunctorFilter: MoreUtils](
    cells: F[Cell]
  ): (F[(Cell, Player)], F[GameState]) = {
    val uniqueTurns = cells.unique zip players[F]

    val gameOvers = uniqueTurns
      .scanLeft[Board](Map.empty) { _ + _ }
      .map(gameOver)
      .takeUntil(_.isEmpty)

    val finalTurns = gameOvers.takeWhile(_.isEmpty) zip uniqueTurns map { _._2 }

    val states = gameOvers zip players[F] map {
      case (go, p) => go getOrElse NextTurn(p)
    }

    (finalTurns, states)
  }

  def game[F[_]: Monad: FunctorFilter: MoreUtils](
    cells: F[F[Cell]]
  ): (F[(Cell, Player)], F[GameState]) = {
    val rounds = cells map round[F]
    val board = rounds.flatMap(_._1)
    val states = rounds.flatMap(_._2)
    (board, states)
  }
}
