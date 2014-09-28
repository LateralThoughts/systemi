import play.api.GlobalSettings
import com.softwaremill.macwire.MacwireMacros._

object Global extends GlobalSettings {
  val wired = wiredInModule(new ProductionModule)

  override def getControllerInstance[A](controllerClass: Class[A]) =
    wired.lookupSingleOrThrow(controllerClass)
}
