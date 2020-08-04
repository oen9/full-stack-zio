package example.modules

import cats.implicits._
import example.components.GQLSwitch
import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.SetStateHookCallback
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._
import slinky.core.facade.ReactElement

@react object GraphQL {
  type Props = Unit

  case class Product(name: Option[String], amount: Option[Int], features: Vector[Feature] = Vector())
  case class Feature(name: Option[String], value: Option[Int], description: Option[String])
  val statProducts = Vector(
    Product(
      name = "expensive laptop".some,
      amount = 100.some,
      features = Vector(
        Feature(name = "speed".some, value = 100.some, description = "very fast".some),
        Feature(name = "color".some, value = 4.some, description = "blue".some)
      )
    ),
    Product(
      name = "cheap laptop".some,
      amount = 130.some,
      features = Vector(
        Feature(name = "speed".some, value = 20.some, description = "fast enough".some),
        Feature(name = "color".some, value = 1.some, description = "red".some)
      )
    ),
    Product(
      name = "chair".some,
      amount = 4.some,
      features = Vector(
        Feature(name = "material".some, value = 47.some, description = "wood".some),
        Feature(name = "pillows".some, value = 2.some, description = "soft chair".some)
      )
    )
  )

  val component = FunctionalComponent[Props] { _ =>
    val (productNameOpt, setProductNameOpt)               = useState(true)
    val (productAmountOpt, setProductAmountOpt)           = useState(false)
    val (productFeaturesOpt, setProductFeaturesOpt)       = useState(true)
    val (featureNameOpt, setFeatureNameOpt)               = useState(true)
    val (featureValueOpt, setFeatureValueOpt)             = useState(false)
    val (featureDescriptionOpt, setFeatureDescriptionOpt) = useState(true)
    val (fakeProducts, setFakeProducts)                   = useState(Vector[Product]())

    def handleSwitch(sshc: SetStateHookCallback[Boolean])(e: SyntheticEvent[html.Input, Event]): Unit =
      sshc(e.currentTarget.checked)

    def handleSubmit(e: SyntheticEvent[html.Form, Event]): Unit = {
      e.preventDefault()
      println(s"hello, world! $productNameOpt")
      setFakeProducts(statProducts)
    }

    def gqlForm() = form(
      onSubmit := (handleSubmit(_)),
      GQLSwitch(productNameOpt, setProductNameOpt, "product name"),
      GQLSwitch(productAmountOpt, setProductAmountOpt, "product amount"),
      GQLSwitch(productFeaturesOpt, setProductFeaturesOpt, "product features"),
      div(
        className := "collapse",
        className := ("show").some.filter(_ => productFeaturesOpt),
        id := "collapseFeatures",
        div(
          className := "card card-body",
          GQLSwitch(featureNameOpt, setFeatureNameOpt, "feature name"),
          GQLSwitch(featureValueOpt, setFeatureValueOpt, "feature value"),
          GQLSwitch(featureDescriptionOpt, setFeatureDescriptionOpt, "feature description")
        )
      ),
      button(`type` := "submit", className := "btn btn-secondary", "execute")
    )

    def parseFeatures(fs: Vector[Feature]) =
      fs.map { f =>
        Vector(
          f.name.map(n => s"name: $n").toVector,
          f.value.map(v => s"value: $v").toVector,
          f.description.map(d => s"description: $d").toVector
        ).flatten.mkString(", ")
      }.map(div(_))
        .foldLeft(Vector[ReactElement]()) { (res, b) =>
          res match {
            case Vector() => res :+ b
            case _        => res ++ Vector(hr(), b)
          }
        }

    def results(products: Vector[Product]) = div(
      products.headOption.map { first =>
        table(
          className := "table",
          thead(
            tr(
              th("#", key := "th-#"),
              first.name.map(_ => th("Product name", key := "th-name")),
              first.amount.map(_ => th("Product amount", key := "th-amount")),
              first.features.headOption.map(_ => th("Product features", key := "th-features"))
            )
          ),
          tbody(
            products.zipWithIndex.map {
              case (p, i) =>
                tr(
                  key := i.toString(),
                  th(scope := "row", i),
                  td(p.name),
                  td(p.amount),
                  td(parseFeatures(p.features))
                )
            }
          )
        )
      }
    )

    div(
      className := "card",
      div(
        className := "card-header",
        div(
          className := "row",
          div(className := "col", div("GraphQL in progress"))
        )
      ),
      div(
        className := "card-body",
        h5(className := "card-title", "Query data"),
        gqlForm(),
        results(fakeProducts)
      )
    )
  }
}
