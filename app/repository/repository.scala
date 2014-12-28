package repository

import play.api.libs.json.Json

trait Repository {

  val invoicesCollectionName = "invoices"
  val allocationsCollectionName = "affectations"
  val activitiesCollectionName = "activities"

}

trait RequestBuilder {

  def idCriteria(id: String) = Json.obj("_id" -> Json.obj("$oid" -> id))

}
