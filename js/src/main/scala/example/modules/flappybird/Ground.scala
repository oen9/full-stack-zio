package example.modules.flappybird

import com.github.oen9.slinky.bridge.konva.KonvaHelper
import com.github.oen9.slinky.bridge.reactkonva.Group
import com.github.oen9.slinky.bridge.reactkonva.Image
import com.github.oen9.slinky.bridge.reactkonva.Rect
import com.github.oen9.slinky.bridge.useimage.UseImage._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.FunctionalComponent

@react object Ground {
  case class Props(groundShift: Int, debug: Boolean = false)

  val groundWidth = 18
  val groundNum = (GameLogic.width / groundWidth.toDouble).ceil.toInt

  val component = FunctionalComponent[Props] {props =>
    val (groundImg, _) = useImage("front-res/img/flappy/ground.png")
    val (debugRect, setDebugRect) = useState(KonvaHelper.IRect())

    useLayoutEffect(() => {
      if (props.debug) {
        val rect = KonvaHelper.IRect(
          x = 0,
          y = GameLogic.groundY,
          width = GameLogic.width,
          height = GameLogic.height,
        )
        setDebugRect(rect)
      }
    }, Seq(props))

    Group(
      (0 to groundNum).map { i =>
        Image(
          image = groundImg,
          x = i * groundWidth + props.groundShift,
          y = GameLogic.groundY,
          scaleX = 0.5,
          scaleY = 0.5,
        ).withKey(i.toString())
      },

      if (props.debug) {
        Rect(
          x = debugRect.x.toInt,
          y = debugRect.y.toInt,
          width = debugRect.width.toInt,
          height = debugRect.height.toInt,
          stroke = "brown",
        ),
      } else Group()
    )
  }
}
