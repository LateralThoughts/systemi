package domain

import julienrf.variants.Variants
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID


case class AffectationRequest(value: Double, account: Account)

sealed trait Affectation

// une facture/un revenu se fait subdivisé entre le membre LT, la structure etc..
case class IncomeAffectation(account: Account,
                             value: Double,
                             invoiceId: Option[BSONObjectID] = None
                              ) extends Affectation

trait AffectationSerializer extends AccountSerializer {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val affectationFormatter = Json.format[IncomeAffectation]
}

trait AffectationReqSerializer extends AccountSerializer {
  implicit val affectationReqFormatter = Json.format[AffectationRequest]
}


