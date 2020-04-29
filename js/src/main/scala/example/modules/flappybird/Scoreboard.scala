package example.modules.flappybird

import com.github.oen9.slinky.bridge.konva.Konva.KonvaEventObject
import com.github.oen9.slinky.bridge.reactkonva.Group
import com.github.oen9.slinky.bridge.reactkonva.Image
import com.github.oen9.slinky.bridge.reactkonva.Text
import com.github.oen9.slinky.bridge.useimage.UseImage._
import org.scalajs.dom.raw.Event
import slinky.core.annotations.react
import slinky.core.FunctionalComponent

@react object Scoreboard {
  case class Props(
    gameOver: Boolean,
    score: Int,
    bestScore: Int,
    onClickRestart: KonvaEventObject[Event] => Unit
  )

  val component = FunctionalComponent[Props] { props =>
    val (restartImg, _) = useImage("front-res/img/flappy/restart.png")
    val (scoreImg, _)   = useImage("front-res/img/flappy/score.png")

    if (props.gameOver) {
      Group(
        Image(
          image = scoreImg,
          x = GameLogic.width / 2 - 43,
          y = GameLogic.height / 2 - 100,
          scaleX = 0.5,
          scaleY = 0.5
        ),
        Image(
          image = restartImg,
          x = GameLogic.width / 2 - 54,
          y = GameLogic.height / 2 + 50,
          scaleX = 0.5,
          scaleY = 0.5,
          onClick = props.onClickRestart,
          onTap = props.onClickRestart
        ),
        Text(
          x = GameLogic.width / 2 - 43,
          y = GameLogic.height / 2 - 125,
          width = 86,
          height = 144,
          align = "center",
          verticalAlign = "middle",
          text = s"${props.score}",
          fontSize = 24,
          fill = "black"
        ),
        Text(
          x = GameLogic.width / 2 - 43,
          y = GameLogic.height / 2 - 80,
          width = 86,
          height = 144,
          align = "center",
          verticalAlign = "middle",
          text = s"${props.bestScore}",
          fontSize = 24,
          fill = "black"
        )
      )
    } else {
      Group()
    }
  }
}
