package example.modules

import diode.data.PotState.PotReady
import scalajs.js
import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.core.ReactComponentClass
import slinky.reactrouter.Redirect
import slinky.reactrouter.Route
import slinky.reactrouter.Switch

import example.bridges.PathToRegexp
import example.bridges.reactrouter.RouteProps
import example.services.AppCircuit
import example.services.ReactDiode

@react object MainRouter {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (auth, _) = ReactDiode.useDiode(AppCircuit.zoom(_.auth))

    def securedRoute(path: String, component: ReactComponentClass[_]) = {
      val securedComp: ReactComponentClass[_] = auth.state match {
        case PotReady => component
        case _        => FunctionalComponent[Unit](_ => Redirect(to = Loc.signIn))
      }
      Route(exact = true, path = path, component = securedComp)
    }

    val routerSwitch = Switch(
      Route(exact = true, path = Loc.home, component = Home.component),
      Route(exact = true, path = Loc.simpleExamples, component = SimpleExamples.component),
      Route(exact = true, path = Loc.dynPage, component = DynamicPage.component),
      Route(exact = true, path = Loc.chat, component = Chat.component),
      Route(exact = true, path = Loc.todos, component = Todos.component),
      Route(exact = true, path = Loc.flappy, component = Flappy.component),
      securedRoute(path = Loc.secured, component = Secured.component),
      Route(exact = true, path = Loc.signIn, component = SignIn.component),
      Route(exact = true, path = Loc.register, component = Register.component),
      Route(exact = true, path = Loc.about, component = About.component)
    )
    ReactDiode.diodeContext.Provider(AppCircuit)(
      Layout(routerSwitch)
    )
  }

  sealed trait MenuItemType
  case class RegularMenuItem(idx: String, label: String, location: String)              extends MenuItemType
  case class DropDownMenuItems(idx: String, label: String, items: Seq[RegularMenuItem]) extends MenuItemType

  object Loc {
    val home           = "/"
    val simpleExamples = "/simple-examples"
    val dynPage        = "/dyn/:foo(\\d+)/:bar(.*)"
    val chat           = "/chat"
    val todos          = "/todos"
    val flappy         = "/flappy"
    val secured        = "/secured"
    val about          = "/about"
    val signIn         = "/sign-in"
    val register       = "/register"
  }
  val menuItems: Seq[MenuItemType] = Seq(
    DropDownMenuItems(
      "100",
      "Databases",
      Seq(
        RegularMenuItem("101", "MongoDB todos", Loc.todos),
        RegularMenuItem("102", "Postgres flappy", Loc.flappy)
      )
    ),
    DropDownMenuItems(
      "200",
      "Auth",
      Seq(
        RegularMenuItem("201", "Secured page", Loc.secured),
        RegularMenuItem("202", "Sign in", Loc.signIn),
        RegularMenuItem("203", "Register", Loc.register)
      )
    ),
    DropDownMenuItems(
      "300",
      "Other",
      Seq(
        RegularMenuItem("301", "Simple examples", Loc.simpleExamples),
        RegularMenuItem("302", "Dynamic page", pathToDynPage(678, "a/b/c")),
        RegularMenuItem("303", "Chat", Loc.chat)
      )
    ),
    RegularMenuItem("1000", "About", Loc.about)
  )

  def pathToDynPage(foo: Int, bar: String): String = {
    val compiled = PathToRegexp.compile(Loc.dynPage)
    compiled(
      js.Dynamic
        .literal(
          foo = foo,
          bar = bar
        )
        .asInstanceOf[PathToRegexp.ToPathData]
    )
  }
}
