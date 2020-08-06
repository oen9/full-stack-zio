package example.services

import GraphQLClientData._
import sttp.client._
import scala.concurrent.ExecutionContext.Implicits.global
import caliban.client.CalibanClientError
import scala.concurrent.Future
import caliban.client.SelectionBuilder
import caliban.client.Operations

object GraphQLClient {
  lazy val sttpBackend = FetchBackend()
  val uri              = uri"${AjaxClient.baseUrl}/api/graphql"

  case class ItemBaseView(name: String)
  case class ItemFullView(name: String, amount: Int, features: List[FeatureFullView])
  case class FeatureFullView(name: String, value: Int, description: String)

  def getItemQuery(name: String) = Queries.item(name) {
    (Item.name
      ~ Item.amount
      ~ Item.features {
        (
          Feature.name
            ~ Feature.value
            ~ Feature.description
        ).mapN(FeatureFullView)
      }).mapN(ItemFullView)
  }

  val getItemsQuery = Queries.items {
    Item.name.map(ItemBaseView)
  }

  def getItems()            = runRequest(getItemsQuery)
  def getItem(name: String) = runRequest(getItemQuery(name))

  private def runRequest[A](query: SelectionBuilder[Operations.RootQuery, A]) =
    sttpBackend
      .send(query.toRequest(uri))
      .map(_.body)
      .flatMap(handleError)

  private def handleError[A](value: Either[CalibanClientError, A]): Future[A] = value match {
    case Left(error) => Future.failed(error)
    case Right(succ) => Future.successful(succ)
  }
}
