import play.api.{Play, Logger, GlobalSettings}
import com.softwaremill.macwire.MacwireMacros._


object Global extends GlobalSettings {
  val logger = Logger("Global")
  val wired = wiredInModule(new ProductionModule)

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    wired.lookupSingleOrThrow(controllerClass)
  }
}
