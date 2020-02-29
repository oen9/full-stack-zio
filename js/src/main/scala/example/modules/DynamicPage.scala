package example.modules
import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.web.html._
import example.bridges.reactrouter.ReactRouterDOM

@react object DynamicPage {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val params = ReactRouterDOM.useParams().toMap
    h4("Dynamic page params: " + params)
  }
}
