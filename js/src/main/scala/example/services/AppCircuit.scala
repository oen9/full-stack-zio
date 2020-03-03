package example.services

import diode.{Action, ActionHandler, Circuit, ModelRW}
import example.shared.Dto.Foo
import diode.data.Pot
import diode.data.Empty
import diode.data.PotAction

case class Clicks(count: Int)
case class RootModel(clicks: Clicks, randomNumber: Pot[Foo] = Empty)

case object IncreaseClicks extends Action
case class TryGetRandom(potResult: Pot[Foo] = Empty) extends PotAction[Foo, TryGetRandom] {
  def next(newResult: Pot[Foo]) = copy(potResult = newResult)
}

class ClicksHandler[M](modelRW: ModelRW[M, Clicks]) extends ActionHandler(modelRW) {
  override def handle = {
    case IncreaseClicks => updated(value.copy(count = value.count + 1))
  }
}

class RandomNumberHandler[M](modelRW: ModelRW[M, Pot[Foo]]) extends ActionHandler(modelRW) {
  import scala.concurrent.ExecutionContext.Implicits.global
  override def handle = {
    case action: TryGetRandom =>
      val updateF = action.effect(AjaxClient.getRandom)(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

object AppCircuit extends Circuit[RootModel] {
  override protected def initialModel: RootModel = RootModel(Clicks(0))

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new ClicksHandler(zoomTo(_.clicks)),
    new RandomNumberHandler(zoomTo(_.randomNumber))
  )
}
