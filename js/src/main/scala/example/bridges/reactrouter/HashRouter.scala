package example.bridges.reactrouter

import slinky.core.ExternalComponent
import slinky.core.annotations.react

import scala.scalajs.js
import scala.scalajs.js.UndefOr

import slinky.reactrouter.ReactRouterDOM

@react object HashRouter extends ExternalComponent {
  case class Props(basename: UndefOr[String] = js.undefined,
                    getUserConfirmation: UndefOr[js.Function] = js.undefined,
                    hashType: UndefOr[String] = js.undefined)

  override val component = ReactRouterDOM.HashRouter
}
