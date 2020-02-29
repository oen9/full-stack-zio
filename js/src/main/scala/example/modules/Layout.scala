package example.modules

import slinky.core.annotations.react
import slinky.web.html._
import slinky.core.FunctionalComponent
import slinky.core.facade.Fragment
import slinky.reactrouter.Link
import slinky.core.facade.ReactElement
import example.bridges.reactrouter.NavLink
import example.modules.MainRouter.Loc

@react object Layout {
  case class Props(content: ReactElement)

  def nav(props: Props) =
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
        )
      )
    )

  def contentBody(props: Props) = props.content

  def footer(props: Props) =
  div(className := "footer bg-dark text-white d-flex justify-content-center mt-auto py-3",
    "© 2020 oen"
  )

  val component = FunctionalComponent[Props] { props =>
    Fragment(
      nav(props),
      div(className := "container",
        div(className := "main-content mt-5", role := "main", contentBody(props))
      ),
      footer(props)
    )
  }
}
