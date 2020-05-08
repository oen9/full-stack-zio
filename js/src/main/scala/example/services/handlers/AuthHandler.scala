package example.services.handlers

import diode.Action
import diode.data.Empty
import diode.data.Pot
import diode.data.PotAction
import diode.NoAction
import diode.{ActionHandler, ModelRW}
import example.services.AjaxClient
import example.services.Auth
import example.services.SetGlobalName
import example.services.SignOut
import example.services.TryAuth
import example.services.TryRegister
import example.shared.Dto.AuthCredentials

class AuthHandler[M](modelRW: ModelRW[M, Pot[Auth]]) extends ActionHandler(modelRW) {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle = {
    case action: TryAuth =>
      val cred    = AuthCredentials(action.username, action.passwd)
      val updateF = action.effect(AjaxClient.postAuth(cred))(u => Auth(u.name, u.token))

      val onReady: PotAction[Auth, TryAuth] => Action =
        _.potResult.fold(NoAction: Action)(auth => SetGlobalName(auth.username))
      action.handleWith(this, updateF)(GenericHandlers.withOnReady(onReady))

    case SignOut =>
      updated(Empty)

    case action: TryRegister =>
      val cred    = AuthCredentials(action.username, action.passwd)
      val updateF = action.effect(AjaxClient.postAuthUser(cred))(u => Auth(u.name, u.token))

      val onReady: PotAction[Auth, TryRegister] => Action =
        _.potResult.fold(NoAction: Action)(auth => SetGlobalName(auth.username))
      action.handleWith(this, updateF)(GenericHandlers.withOnReady(onReady))
  }
}
