package example.modules

import diode.data.Pot
import diode.data.PotState.PotReady
import example.bridges.reactrouter.NavLink
import example.modules.MainRouter.Loc
import example.services.AppCircuit
import example.services.Auth
import example.services.ReactDiode
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.reactrouter.Link
import slinky.web.html._
import example.services.SignOut

@react object Layout {
  case class Props(content: ReactElement)

  def nav(props: Props, auth: Pot[Auth], onSignOut: () => Unit) =
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
          MainRouter.menuItems.map(item =>
            li(key := item.idx, className := "nav-item",
              NavLink(exact = true, to = item.location)(className := "nav-link", item.label)
            )
          )
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

    Fragment(
      nav(props, auth, () => dispatch(SignOut)),
      div(className := "container",
        div(className := "main-content mt-5", role := "main", contentBody(props))
      ),
      footer(props)
    )
  }
}
