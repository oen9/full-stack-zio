package example.modules.flappybird

import com.github.oen9.slinky.bridge.konva.KonvaHelper
import com.github.oen9.slinky.bridge.reactkonva.Group
import com.github.oen9.slinky.bridge.reactkonva.Operations
import com.github.oen9.slinky.bridge.reactkonva.Rect
import com.github.oen9.slinky.bridge.reactkonva.Sprite
import com.github.oen9.slinky.bridge.useimage.UseImage._
import scalajs.js
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.ReactRef
import slinky.core.FunctionalComponent

@react object Bird {
  case class Props(
    angle: Int,
    y: Int,
    ref: ReactRef[Operations.SpriteRef],
    debug: Boolean = false,
  )

  val birdWidth = 92
  val birdHeight = 64
  val angleWidthFix = - 17
  val angleHeightFix = 1

  val animations = js.Dynamic.literal(
    idle = js.Array(
      0 * birdWidth, 0, birdWidth, birdHeight,
      1 * birdWidth, 0, birdWidth, birdHeight,
      2 * birdWidth, 0, birdWidth, birdHeight,
    )
  )

  val component = FunctionalComponent[Props] { props =>
    val (birdImg, _) = useImage("front-res/img/flappy/bird.png")
    val (debugRect, setDebugRect) = useState(KonvaHelper.IRect())

    useLayoutEffect(() => {
      props.ref.current.rotation(props.angle)
    }, Seq(props.angle))

    useLayoutEffect(() => {
      if (props.debug) {
        setDebugRect(props.ref.current.getClientRect())
      }
    }, Seq(props))

    Group(
      Sprite(
        x = 50,
        y = props.y,
        width = birdWidth + angleWidthFix,
        height = birdHeight + angleHeightFix,
        image = birdImg,
        animations = animations,
        animation = "idle",
        frameRate = 8,
        frameIndex = 0,
        offsetX = birdWidth / 2,
        offsetY = birdHeight / 2,
        scaleX = 0.5,
        scaleY = 0.5,
      ).withRef(props.ref),

      if (props.debug) {
        Rect(
          x = debugRect.x.toInt,
          y = debugRect.y.toInt,
          width = debugRect.width.toInt,
          height = debugRect.height.toInt,
          stroke = "yellow",
        ),
      } else Group()
    )
  }
}
