import play.Logger
import play.api.Application
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import com.softwaremill.macwire.MacwireMacros._
import scala.concurrent.ExecutionContext.Implicits.global

object TestGlobal extends Global {

  val wired = wiredInModule(new TestModule)

  override def onStart(app: Application) = {
    initDatabase
    super.onStart(app)
    initApp(wired)
  }

  def initDatabase {
    Logger.info("Initiating database for tests...")
    val db = ReactiveMongoPlugin.db

    // remove database to start on a clean database
    // db.drop()
  }

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    wired.lookupSingleOrThrow(controllerClass)
  }
}
