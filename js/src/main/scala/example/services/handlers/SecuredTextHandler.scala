package example.services.handlers

import diode.data.Pot
import diode.data.PotAction
import diode.{ActionHandler, ModelRW}
import example.services.AjaxClient
import example.services.TryGetSecuredText

class SecuredTextHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle = {
    case action: TryGetSecuredText =>
      val updateF = action.effect(AjaxClient.getAuthSecured(action.token))(identity)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}
