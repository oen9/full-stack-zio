package example

import zio.test._

object DummyTest extends DefaultRunnableSpec {
  def spec = suite("dummy suite")(
    test("dummy hello test") {
      assert("hello")(Assertion.equalTo("hello"))
    }
  )
}
