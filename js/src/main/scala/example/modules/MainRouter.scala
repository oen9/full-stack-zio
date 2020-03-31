package example.modules

import example.bridges.PathToRegexp
import example.services.ReactDiode
import example.services.AppCircuit
import scalajs.js
import slinky.core.annotations.react
import slinky.reactrouter.Route
import slinky.reactrouter.Switch
import example.bridges.reactrouter.RouteProps
import slinky.core.FunctionalComponent

@react object MainRouter {
  type Props = RouteProps

  val component = FunctionalComponent[Props] { _ =>
    val routerSwitch = Switch(
      Route(exact = true, path = Loc.home, component = Home.component),
      Route(exact = true, path = Loc.dynPage, component = DynamicPage.component),
      Route(exact = true, path = Loc.todos, component = Todos.component),
      Route(exact = true, path = Loc.about, component = About.component),
    )
    ReactDiode.diodeContext.Provider(AppCircuit)(
      Layout(routerSwitch)
    )
  }

  case class MenuItem(idx: String, label: String, location: String)
  object Loc {
    val home = "/"
    val dynPage = "/dyn/:foo(\\d+)/:bar(.*)"
    val todos = "/todos"
    val about = "/about"
  }
  val menuItems = Seq(
    MenuItem("0", "Home", Loc.home),
    MenuItem("2", "Dynamic page", pathToDynPage(678, "a/b/c")),
    MenuItem("3", "MongoDB todos", Loc.todos),
    MenuItem("100", "About", Loc.about),
  )

  def pathToDynPage(foo: Int, bar: String): String = {
    val compiled = PathToRegexp.compile(Loc.dynPage)
    compiled(
      js.Dynamic.literal(
        foo = foo,
        bar = bar
      ).asInstanceOf[PathToRegexp.ToPathData]
    )
  }
}
