import controllers.ClientController
import com.softwaremill.macwire.MacwireMacros._


class ProductionModule {
  lazy val clientController = wire[ClientController]
}
