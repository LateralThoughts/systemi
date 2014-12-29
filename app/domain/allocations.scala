package domain

import julienrf.variants.Variants
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID


case class AllocationRequest(value: Double, account: Account)

sealed trait Allocation

// une facture/un revenu se fait subdiviser entre le membre LT, la structure etc..
case class IncomeAllocation(account: Account,
                             value: Double,
                             invoiceId: BSONObjectID) extends Allocation

trait AllocationSerializer extends AccountSerializer {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val allocationFormatter = Json.format[IncomeAllocation]
  implicit val allocationReqFormatter = Json.format[AllocationRequest]

}

