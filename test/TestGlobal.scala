import play.Logger
import play.api.Application
import play.modules.reactivemongo.ReactiveMongoPlugin
import com.softwaremill.macwire.MacwireMacros._
import play.modules.reactivemongo.json.collection.JSONCollection
import repository.Repository
import scala.concurrent.ExecutionContext.Implicits.global

object TestGlobal extends Global with Repository {

  val wired = wiredInModule(new TestModule)

  override def onStart(app: Application) = {
    initDatabase(app)
    super.onStart(app)
    initApp(wired)
  }

  def initDatabase(app: Application) {
    Logger.info("Initiating database for tests...")
    val db = ReactiveMongoPlugin.db(app)

    // remove database to start on a clean database
    db.collection[JSONCollection](invoiceNumberCollectionName).drop()
  }

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    wired.lookupSingleOrThrow(controllerClass)
  }
}
