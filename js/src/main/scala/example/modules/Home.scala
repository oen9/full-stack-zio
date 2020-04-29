package example.modules

import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object Home {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    div(
      className := "card",
      div(className := "card-header", "Home"),
      div(
        className := "card-body",
        h5(
          className := "card-title text-center mb-2",
          "Full stack app example with databases, api documentation and more."
        ),
        div(
          className := "row align-items-center",
          div(className := "col-12 col-sm-4", img(className := "img-fluid", src := "front-res/img/logos/docker.png")),
          div(className := "col-12 col-sm-4", img(className := "img-fluid", src := "front-res/img/logos/postgres.png")),
          div(className := "col-12 col-sm-4", img(className := "img-fluid", src := "front-res/img/logos/mongodb.png"))
        ),
        div(
          className := "row align-items-center",
          div(className := "col-12 col-sm-4", img(className := "img-fluid", src := "front-res/img/logos/scala-js.svg")),
          div(className := "col-12 col-sm-4", img(className := "img-fluid", src := "front-res/img/logos/react.png")),
          div(className := "col-12 col-sm-4", img(className := "img-fluid", src := "front-res/img/logos/bootstrap.png"))
        )
      )
    )
  }
}
