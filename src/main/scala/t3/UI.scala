package t3

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.subjects.PublishSubject

import scala.util.Try

object UI extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val terminal = new DefaultTerminalFactory().createTerminal()
    val screen = new TerminalScreen(terminal)
    screen.startScreen()

    try {
      val panel = new Panel()
      panel.setLayoutManager(new GridLayout(1))

      val buttons = Vector.fill(9)(new Button(" ") {
        setRenderer(new Button.BorderedButtonRenderer())
      })

      lazy val cellActions = PublishSubject[Logic.Cell]
      lazy val newGameActions = PublishSubject[Unit]
      lazy val (labels, messages) = reactive.game(cellActions, newGameActions)

      panel.addComponent(new Panel() {
        addComponent(new Label("<status>") {
          for (m <- messages) {
            setText(m.toString)
          }
        })
      })

      panel.addComponent(new Panel() {
        setLayoutManager(new GridLayout(3))
        for ((cell, button) <- Logic.cells.zip(buttons)) {
          addComponent(button)
          button.addListener(_ => {
            logger.warn(s"Button press $cell... start")
            cellActions.onNext(cell)
            logger.warn(s"Button press $cell... done")
          })
        }
        buttons.foreach(addComponent)
      })

      val window = new BasicWindow()

      panel.addComponent(new Panel() {
        setLayoutManager(new GridLayout(2))
        addComponent(new Button("Restart") {
          addListener(_ => {
            logger.warn("Reset press... start")
            for (cell <- Logic.cells)
              buttons(cell).setLabel(" ")
            newGameActions.onNext(())
            logger.warn("Reset press... done")
          })
        })
        addComponent(new Button("Exit") {
          addListener(_ => window.close())
        })
      })

      for ((cell, player) <- labels) {
        logger.warn(s"Last label ${(cell, player)}")
        buttons(cell).setLabel(player.toString)
      }
      newGameActions.onNext(())

      window.setComponent(panel)

      val gui = new MultiWindowTextGUI(
        screen,
        new DefaultWindowManager(),
        new EmptySpace(TextColor.ANSI.BLUE)
      )
      gui.addWindowAndWait(window)
    } catch {
      case e: Throwable =>
        println(Try(screen.stopScreen()), Try(terminal.close()))
        e.printStackTrace()
    }
  }
}
