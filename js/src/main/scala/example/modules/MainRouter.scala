package example.modules

import example.bridges.PathToRegexp
//import example.services.ReactDiode
//import example.services.AppCircuit
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
      Route(exact = true, path = Loc.about, component = About.component),
      Route(exact = true, path = Loc.page3, component = DynamicPage.component)
    )
    //ReactDiode.diodeContext.Provider(AppCircuit)(
      Layout(routerSwitch)
    //)
  }

  case class MenuItem(idx: String, label: String, location: String)
  object Loc {
    val home = "/"
    val about = "/about"
    val page3 = "/dyn/:foo(\\d+)/:bar(.*)"
  }
  val menuItems = Seq(
    MenuItem("0", "Home", Loc.home),
    MenuItem("1", "About", Loc.about),
    MenuItem("2", "Dynamic page", pathToPage3(678, "a/b/c"))
  )

  def pathToPage3(foo: Int, bar: String): String = {
    val compiled = PathToRegexp.compile(Loc.page3)
    compiled(
      js.Dynamic.literal(
        foo = foo,
        bar = bar
      ).asInstanceOf[PathToRegexp.ToPathData]
    )
  }
}
