package example.model

import caliban.GraphQL.graphQL
import caliban.RootResolver

object GQLData {
  case class Item(name: String, amount: Int, features: Vector[Feature] = Vector())
  case class Feature(name: String, value: Int, description: String)

  def getItems                            = exampleItems
  def getItem(name: String): Option[Item] = exampleItems.find(_.name == name)

  val exampleItems = List(
    Item(
      name = "expensive laptop",
      amount = 100,
      features = Vector(
        Feature(name = "speed", value = 100, description = "very fast"),
        Feature(name = "color", value = 4, description = "blue")
      )
    ),
    Item(
      name = "cheap laptop",
      amount = 130,
      features = Vector(
        Feature(name = "speed", value = 20, description = "fast enough"),
        Feature(name = "color", value = 1, description = "red")
      )
    ),
    Item(
      name = "chair",
      amount = 4,
      features = Vector(
        Feature(name = "material", value = 47, description = "wood"),
        Feature(name = "pillows", value = 2, description = "soft chair")
      )
    )
  )

  case class ItemArgs(name: String)
  case class Queries(items: List[Item], item: ItemArgs => Option[Item])

  val queries = Queries(getItems, args => getItem(args.name))
  val api     = graphQL(RootResolver(queries))
}
