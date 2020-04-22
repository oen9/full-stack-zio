package example.services.handlers

import diode.data.Empty
import diode.data.Pot
import diode.data.PotAction
import diode.{ActionHandler, ModelRW}
import example.services.AjaxClient
import example.services.Auth
import example.services.SignOut
import example.services.TryAuth
import example.services.TryRegister

class AuthHandler[M](modelRW: ModelRW[M, Pot[Auth]]) extends ActionHandler(modelRW) {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle = {
    case action: TryAuth =>
      val updateF = action.effect(AjaxClient.getScoreboard)(_ => Auth(username = action.username, token = "bar")) // TODO this is only fake auth
      action.handleWith(this, updateF)(PotAction.handler())

    case SignOut =>
      updated(Empty)

    case action: TryRegister =>
      val updateF = action.effect(AjaxClient.getScoreboard)(_ => Auth(username = action.username, token = "bar")) // TODO this is only fake register and auth
      action.handleWith(this, updateF)(PotAction.handler())
  }
}
