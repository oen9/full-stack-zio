package example.modules

import diode.data.Pot
import diode.data.PotState.PotReady
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.reactrouter.Link
import slinky.web.html._

import example.bridges.reactrouter.NavLink
import example.bridges.reactrouter.ReactRouterDOM
import example.modules.MainRouter.DropDownMenuItems
import example.modules.MainRouter.Loc
import example.modules.MainRouter.RegularMenuItem
import example.services.AppCircuit
import example.services.Auth
import example.services.ReactDiode
import example.services.SignOut

@react object Layout {
  case class Props(content: ReactElement)

  def createRegularMenuItem(idx: String, label: String, location: String) =
    li(key := idx, className := "nav-item",
      NavLink(exact = true, to = location)(className := "nav-link", label)
    )

  def createDropDownMenuItems(currentPath: String, idx: String, label: String, items: Seq[RegularMenuItem]) =
    li(key := idx,
      if (items.exists(_.location == currentPath)) className := "nav-item dropdown active"
      else className := "nav-item dropdown",
      a(className := "nav-link dropdown-toggle",
        href := "#", id := "navbarDropdown",
        role := "button", data-"toggle" := "dropdown",
        aria-"haspopup" := "true",
        aria-"expanded" := "false",
        label
      ),
      div(className := "dropdown-menu", aria-"labelledby" := "navbarDropdown",
        items.map(item =>
          NavLink(exact = true, to = item.location)(className := "dropdown-item", key := item.idx, item.label)
        )
      )
    )

  def nav(props: Props, currentPath: String, auth: Pot[Auth], onSignOut: () => Unit) =
    div(className := "navbar navbar-expand-md navbar-dark bg-dark",
      Link(to = Loc.home)(
        className := "navbar-brand",
        img(src := "front-res/img/logo-mini.png"),
        " full-stack-zio"
      ),
      button(className := "navbar-toggler", `type` := "button", data-"toggle" := "collapse", data-"target" := "#navbarNav", aria-"controls" := "navbarNav", aria-"expanded" := "false", aria-"label" := "Toggle navigation",
        span(className := "navbar-toggler-icon")
      ),
      div(className := "collapse navbar-collapse", id := "navbarNav",
        ul(className := "navbar-nav mr-auto",
          MainRouter.menuItems.map(_ match {
            case RegularMenuItem(idx, label, location) => createRegularMenuItem(idx, label, location)
            case DropDownMenuItems(idx, label, items) => createDropDownMenuItems(currentPath, idx, label, items)
          })
        ),
        auth.state match {
          case PotReady =>
            Fragment(
              span(className := "navbar-text mr-2", auth.get.username),
              button(className := "btn btn-secondary d-lg-inline-block", "Sign Out", onClick := onSignOut),
            )
          case _ =>
            NavLink(exact = true, to = MainRouter.Loc.signIn)(className := "btn btn-secondary d-lg-inline-block", "Sign In")
        }
      )
    )

  def contentBody(props: Props) = props.content

  def footer(props: Props) =
  div(className := "footer bg-dark text-white d-flex justify-content-center mt-auto py-3",
    "© 2020 oen"
  )

  val component = FunctionalComponent[Props] { props =>
    val (auth, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.auth))
    val location = ReactRouterDOM.useLocation()

    Fragment(
      nav(props, location.pathname, auth, () => dispatch(SignOut)),
      div(className := "container",
        div(className := "main-content mt-5", role := "main", contentBody(props))
      ),
      footer(props)
    )
  }
}
