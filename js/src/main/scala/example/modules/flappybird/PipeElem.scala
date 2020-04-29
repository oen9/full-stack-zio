package example.modules.flappybird

import com.github.oen9.slinky.bridge.konva.KonvaHelper
import com.github.oen9.slinky.bridge.reactkonva.Group
import com.github.oen9.slinky.bridge.reactkonva.Image
import com.github.oen9.slinky.bridge.reactkonva.Operations
import com.github.oen9.slinky.bridge.reactkonva.Rect
import com.github.oen9.slinky.bridge.useimage.UseImage._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.ReactRef
import slinky.core.FunctionalComponent

@react object PipeElem {
  case class Props(
    pipe: GameLogic.Pipe,
    holeSize: Int,
    upperPipeRef: ReactRef[Operations.ShapeRef],
    lowerPipeRef: ReactRef[Operations.ShapeRef],
    debug: Boolean = false
  )

  val component = FunctionalComponent[Props] { props =>
    val (pipeImg, _)                        = useImage("front-res/img/flappy/pipe.png")
    val (debugPipe1Rect, setDebugPipe1Rect) = useState(KonvaHelper.IRect())
    val (debugPipe2Rect, setDebugPipe2Rect) = useState(KonvaHelper.IRect())

    useLayoutEffect(() => props.upperPipeRef.current.rotation(180), Seq())

    useLayoutEffect(
      () =>
        if (props.debug) {
          setDebugPipe1Rect(props.upperPipeRef.current.getClientRect())
          setDebugPipe2Rect(props.lowerPipeRef.current.getClientRect())
        },
      Seq(props)
    )

    Group(
      Image(
        image = pipeImg,
        x = props.pipe.x,
        y = props.pipe.y,
        scaleX = 0.5,
        scaleY = 0.5,
        offsetX = GameLogic.pipeWidth * 2
      ).withRef(props.upperPipeRef),
      Image(
        image = pipeImg,
        x = props.pipe.x,
        y = props.pipe.y + props.holeSize,
        scaleX = 0.5,
        scaleY = 0.5
      ).withRef(props.lowerPipeRef),
      if (props.debug) {
        Group(
          Rect(
            x = debugPipe1Rect.x.toInt,
            y = debugPipe1Rect.y.toInt,
            width = debugPipe1Rect.width.toInt,
            height = debugPipe1Rect.height.toInt,
            stroke = "green"
          ),
          Rect(
            x = debugPipe2Rect.x.toInt,
            y = debugPipe2Rect.y.toInt,
            width = debugPipe2Rect.width.toInt,
            height = debugPipe2Rect.height.toInt,
            stroke = "green"
          )
        )
      } else Group()
    )
  }
}
