package example.services.handlers

import diode.ActionResult
import diode.data.Pot
import diode.data.PotAction
import diode.{ActionHandler, ModelRW}
import example.services.GetGQLItem
import example.services.GetGQLItems
import example.services.GraphQLClient
import example.services.GraphQLClient.ItemBaseView
import example.services.GraphQLClient.ItemFullView
import scala.concurrent.ExecutionContext.Implicits.global

object GraphQLHandlers {
  class ItemsHandler[M](modelRW: ModelRW[M, Pot[List[ItemBaseView]]]) extends ActionHandler(modelRW) {
    override protected def handle: PartialFunction[Any, ActionResult[M]] = {
      case action: GetGQLItems =>
        val updateF = action.effect(GraphQLClient.getItems())(identity _)
        action.handleWith(this, updateF)(PotAction.handler())
    }
  }

  class ItemHandler[M](modelRW: ModelRW[M, Pot[Option[ItemFullView]]]) extends ActionHandler(modelRW) {
    override protected def handle: PartialFunction[Any, ActionResult[M]] = {
      case action: GetGQLItem =>
        val updateF = action.effect(GraphQLClient.getItem(action.name))(identity _)
        action.handleWith(this, updateF)(PotAction.handler())
    }
  }
}
