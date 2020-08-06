package example.services

import caliban.client._
import caliban.client.FieldBuilder._
import caliban.client.Operations._
import caliban.client.SelectionBuilder._

object GraphQLClientData {

  type Feature
  object Feature {
    def name: SelectionBuilder[Feature, String]        = Field("name", Scalar())
    def value: SelectionBuilder[Feature, Int]          = Field("value", Scalar())
    def description: SelectionBuilder[Feature, String] = Field("description", Scalar())
  }

  type Item
  object Item {
    def name: SelectionBuilder[Item, String] = Field("name", Scalar())
    def amount: SelectionBuilder[Item, Int]  = Field("amount", Scalar())
    def features[A](innerSelection: SelectionBuilder[Feature, A]): SelectionBuilder[Item, List[A]] =
      Field("features", ListOf(Obj(innerSelection)))
  }

  type Queries = RootQuery
  object Queries {
    def items[A](innerSelection: SelectionBuilder[Item, A]): SelectionBuilder[RootQuery, List[A]] =
      Field("items", ListOf(Obj(innerSelection)))
    def item[A](name: String)(innerSelection: SelectionBuilder[Item, A]): SelectionBuilder[RootQuery, Option[A]] =
      Field("item", OptionOf(Obj(innerSelection)), arguments = List(Argument("name", name)))
  }
}
