package controllers.api

import java.io.{BufferedInputStream, FileInputStream, File}

import auth.WithDomain
import domain._
import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Result, MultipartFormData, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future
import scala.io.Source

class ExpenseApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with AccountSerializer
  with ExpenseSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def createExpense = SecuredAction(WithDomain()).async(parse.multipartFormData) { implicit request =>
    request.body.asFormUrlEncoded.get("accountId") match {
      case Some(accountList) =>
        val accountId = accountList.head
        db
          .collection[JSONCollection]("accounts")
          .find(Json.obj("_id" -> Json.obj("$oid" -> accountId)))
          .one[Account]
          .flatMap { mayBeAccount =>
          val mayBeFutureStatus = mayBeAccount flatMap(extractAndSaveExpenseIfPossible(request.body, _))
          mayBeFutureStatus getOrElse Future(BadRequest)
        }

      case None => Future(BadRequest)
    }

  }

  def findAll = SecuredAction(WithDomain()).async {
    db
      .collection[JSONCollection]("expenses")
      .find(Json.obj(), Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(expenses => Ok(Json.toJson(expenses)))
  }

  def getAttachment(oid: String) = SecuredAction(WithDomain()).async { implicit request =>
    db
      .collection[JSONCollection]("expenses")
      .find(Json.obj("_id" -> Json.obj("$oid" -> oid)))
      .one[JsObject]
      .map{
      case Some(docObj) =>
        val doc = (docObj \ "document" \ "data" \ "data").as[String]
        val contentType = (docObj \ "document" \ "contentType").as[String]
        Ok(Base64.decode(doc)).as(contentType)

      case None => BadRequest
    }
  }

  private def extractAndSaveExpenseIfPossible(body: MultipartFormData[TemporaryFile],
                                      account: Account): Option[Future[Result]] = {
    extractExpense(body, account) map {
      case expense =>
        db
          .collection[JSONCollection]("expenses")
          .save(Json.toJson(expense))
          .map {
          errors =>
            if (errors.inError)
              InternalServerError
            else
              Redirect(controllers.routes.ExpenseController.index.url)
        }
    }
  }

  private def extractExpense(body: MultipartFormData[TemporaryFile], account: Account) = {
    val form = body.asFormUrlEncoded
    for {
      expenseValue <- form.get("expenseValue")
      expenseType <- form.get("expenseType")
      document <- body.file("expenseDocument")
      description <- form.get("description")
      value = expenseValue.head.toDouble
      exType = expenseType.head
      openedFile = new BufferedInputStream(new FileInputStream(document.ref.file))
      data = Stream.continually(openedFile.read).takeWhile(-1 !=).map(_.toByte).toArray
      attachment = Attachment(document.contentType.getOrElse("application/pdf"), false, data)
      desc = description.head
    } yield Expense(value, exType, account, attachment, desc, DateTime.now())
  }
}
