package example.services.handlers

import diode.data.Pot
import diode.data.PotAction
import diode.{ActionHandler, ModelRW}
import example.shared.Dto.Foo
import example.services.Clicks
import example.services.TryGetRandom
import example.services.IncreaseClicks
import example.services.AjaxClient

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

