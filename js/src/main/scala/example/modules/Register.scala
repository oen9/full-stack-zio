package example.modules

import cats.implicits._
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import org.scalajs.dom.{Event, html}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._

import example.services.AppCircuit
import example.services.ReactDiode
import example.services.TryRegister
import example.services.Validator
import example.components.AuthLastError

@react object Register {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (auth, dispatch) = ReactDiode.useDiode(AppCircuit.zoom(_.auth))
    val (username, setUsername) = useState("")
    val (password, setPassword) = useState("")
    val (rePassword, setRePassword) = useState("")
    val (errorMsgs, setErrorMsgs) = useState(Vector[String]())

    def handleUsername(e: SyntheticEvent[html.Input, Event]): Unit = setUsername(e.target.value)
    def handlePassword(e: SyntheticEvent[html.Input, Event]): Unit = setPassword(e.target.value)
    def handleRePassword(e: SyntheticEvent[html.Input, Event]): Unit = setRePassword(e.target.value)

    def register(tryRegister: TryRegister) = {
      dispatch(tryRegister)
      setUsername("")
      setPassword("")
      setRePassword("")
      setErrorMsgs(Vector())
    }

    def handleRegister() = {
      Validator
        .validateTryRegister(username, password, rePassword)
        .fold(setErrorMsgs, register)
    }

    def registerForm() = Fragment(
      div(className := "input-group mb-3",
        div(className := "input-group-prepend",
          span(className := "input-group-text", "username", id := "form-username-label")
        ),
        input(`type` := "text",
          className := "form-control",
          placeholder := "Username",
          aria-"label" := "Username",
          aria-"describedby" := "form-username-label",
          value := username,
          onChange := (handleUsername(_))
        )
      ),
      div(className := "input-group mb-3",
        div(className := "input-group-prepend",
          span(className := "input-group-text", "password", id := "form-password-label")
        ),
        input(`type` := "password",
          className := "form-control",
          placeholder := "Password",
          aria-"label" := "Password",
          aria-"describedby" := "form-password-label",
          value := password,
          onChange := (handlePassword(_))
        )
      ),
      div(className := "input-group mb-3",
        div(className := "input-group-prepend",
          span(className := "input-group-text", "repeat password", id := "form-re-password-label")
        ),
        input(`type` := "password",
          className := "form-control",
          placeholder := "Repeat Password",
          aria-"label" := "Repeat password",
          aria-"describedby" := "form-re-password-label",
          value := rePassword,
          onChange := (handleRePassword(_))
        )
      ),
      errorMsgs.zipWithIndex.map { case (msg, idx) =>
        div(key := idx.toString,className := "alert alert-danger", role := "alert", msg)
      },
      button(className := "btn btn-secondary", "Register and sign-in instantly", onClick := handleRegister _),
    )

    div(className := "card",
      div(className := "card-header", "Register (simple and free)"),
      div(className := "card-body",
        h5(className := "card-title",
          auth.state match {
            case PotPending =>
              div(className := "spinner-border text-primary", role := "status",
                span(className := "sr-only", "Loading...")
              )
            case PotReady =>
              auth.fold("unknown error")(a => s"You are successfully registered and logged as ${a.username.toString}")
            case _ =>
              "Register"
          },
        ),

        AuthLastError(),

        auth.state match {
          case PotReady => none[ReactElement]
          case _ => registerForm().some
        },

      )
    )
  }
}
