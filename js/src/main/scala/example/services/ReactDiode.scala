package example.services

import diode.Circuit
import diode.Dispatcher
import diode.ModelR
import slinky.core.facade.Hooks._
import slinky.core.facade.React
import slinky.core.facade.ReactContext

object ReactDiode {
  val diodeContext = React.createContext[Circuit[RootModel]](AppCircuit)

  def useDiode[T](selector: ModelR[RootModel, T]): (T, Dispatcher) = {
    useDiode(diodeContext, selector)
  }

  def useDiode[M <: AnyRef, T, S <: Circuit[M]](context: ReactContext[S], selector: ModelR[M, T]): (T, Dispatcher) = {
    val circuit = useContext(context)
    val (state, setState) = useState[T](default = selector())

    useEffect(() => {
      val unsubscription = circuit.subscribe(selector)(state => setState(state.value))
      () => unsubscription()
    })

    (state, circuit)
  }
}
