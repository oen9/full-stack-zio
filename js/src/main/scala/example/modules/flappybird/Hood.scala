package example.modules.flappybird

import com.github.oen9.slinky.bridge.reactkonva.Group
import com.github.oen9.slinky.bridge.reactkonva.Text
import example.modules.flappybird.GameLogic.GameState
import slinky.core.annotations.react
import slinky.core.FunctionalComponent

@react object Hood {
  case class Props(gs: GameState)

  val component = FunctionalComponent[Props] { props =>
    Group(
      Text(
        text = s"fps: ${props.gs.fps}",
        x = 20,
        y = 20,
        fontSize = 14
      ),
      Text(
        text = s"score: ${props.gs.score}",
        x = 20,
        y = 40,
        fontSize = 14
      ),
      if (props.gs.opt.debug) {
        Group(
          Text(
            text = s"${props.gs.bird.toString()}",
            x = 20,
            y = 60,
            fill = "grey",
            fontSize = 14
          ),
          Text(
            text = s"${props.gs.pipe1.toString()}",
            x = 20,
            y = 80,
            fill = "grey",
            fontSize = 14
          ),
          Text(
            text = s"${props.gs.pipe2.toString()}",
            x = 20,
            y = 100,
            fill = "grey",
            fontSize = 14
          )
        )
      } else Group()
    )
  }
}
