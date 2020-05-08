package example.services.handlers

import diode.ActionHandler
import diode.ActionResult
import diode.Effect
import diode.ModelRW
import example.services.ChangeMyChatName
import example.services.SetGlobalName
import scala.concurrent.ExecutionContext.Implicits.global

// flow:
// signin - sync all names
//  -> SetGloablName -> SetChatName
// change globalName - this result in different names (signin name != globalName)
//  -> SetGlobalName -> SetChatName
class GlobalNameHandler[M](modelRW: ModelRW[M, String]) extends ActionHandler(modelRW) {

  override def handle = {

    case SetGlobalName(newName) if newName.trim.nonEmpty =>
      updated(newName, Effect.action(ChangeMyChatName(newName)))

    case SetGlobalName(_) =>
      noChange
  }
}
