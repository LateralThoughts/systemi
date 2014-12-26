package controllers.api

import repository.{AllocationRepository, InvoiceRepository}

trait ApiController {

  import com.softwaremill.macwire.MacwireMacros._

  lazy val invoiceRepository = wire[InvoiceRepository]
  lazy val allocationRepository = wire[AllocationRepository]

}
