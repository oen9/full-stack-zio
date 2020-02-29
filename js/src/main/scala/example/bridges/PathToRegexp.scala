package example.bridges

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("path-to-regexp", JSImport.Default)
object PathToRegexp extends js.Object {
  @js.native
  trait ToPathData extends js.Object
  def compile(str: String): js.Function1[ToPathData, String] = js.native
}
