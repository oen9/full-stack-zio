package example.services.handlers

import diode.data.Pot
import diode.data.PotAction
import diode.data.Ready
import diode.Effect
import diode.{ActionHandler, ModelRW}
import example.services.AddNewScore
import example.services.AjaxClient
import example.services.ClearScoreboard
import example.services.ScoreAdded
import example.services.ScoreboardCleared
import example.services.TryGetScoreboard
import example.shared.Dto.ScoreboardRecord

class ScoreboardHandler[M](modelRW: ModelRW[M, Pot[Vector[ScoreboardRecord]]]) extends ActionHandler(modelRW) {
  import scala.concurrent.ExecutionContext.Implicits.global
  override def handle = {
    case action: TryGetScoreboard =>
      val updateF = action.effect(AjaxClient.getScoreboard)(identity _)
      action.handleWith(this, updateF)(PotAction.handler())

    case AddNewScore(newScore) =>
      val addEffect = Effect(AjaxClient.postScore(newScore).map(ScoreAdded))
      effectOnly(addEffect)
    case ScoreAdded(newScore) =>
      val newValue = value.fold(value)(scores =>
        Ready(
          (scores :+ newScore)
            .sortBy(_.name)
            .sortBy(_.score)
            .reverse
        )
      )
      updated(newValue)

    case ClearScoreboard =>
      val deleteEffect = Effect(AjaxClient.deleteAllScores().map(_ => ScoreboardCleared))
      effectOnly(deleteEffect)
    case ScoreboardCleared =>
      val newValue = value.fold(value)(_ => Ready(Vector()))
      updated(newValue)
  }
}
