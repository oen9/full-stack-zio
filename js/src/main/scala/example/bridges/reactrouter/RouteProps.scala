package example.bridges.reactrouter

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

case class Match(params: js.Object, isExact: Boolean, path: String, url: String)
case class Location(key: UndefOr[String], pathname: UndefOr[String], search: UndefOr[String], hash: UndefOr[String])
case class History(length: Int,
                   action: String,
                   location: Location,
                   push: js.Function1[String,Unit] | js.Function2[String,js.Object,Unit],
                   replace:  js.Function1[String,Unit] | js.Function2[String,js.Object,Unit],
                   go: js.Function1[Int,Unit],
                   goBack: js.Function,
                   goForward: js.Function,
                   block: js.Function)
case class RouteProps(`match`:Match, location: Location, history: History)
