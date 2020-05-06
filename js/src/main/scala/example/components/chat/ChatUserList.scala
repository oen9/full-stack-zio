package example.components.chat

import example.services.AppCircuit
import example.services.ReactDiode
import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.React
import slinky.core.FunctionalComponent
import slinky.web.html._
import example.shared.Dto

@react object ChatUserList {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (users, _) = ReactDiode.useDiode(AppCircuit.zoomTo(_.chatConn.users))
    val (me, _)    = ReactDiode.useDiode(AppCircuit.zoomTo(_.chatConn.user))

    def prettyUser(u: Dto.ChatUser) = {
      val color = if (u.id == me.id) "primary" else "secondary"

      div(
        className := s"alert alert-$color",
        span(u.name),
        span(className := s"ml-2 badge badge-$color", u.id)
      )
    }

    div(
      className := "vh-50 overflow-auto mb-3 bg-light",
      users.value.zipWithIndex.map {
        case (user, idx) =>
          div(key := idx.toString, prettyUser(user))
      }
    )
  }
}
