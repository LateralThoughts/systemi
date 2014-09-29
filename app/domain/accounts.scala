package domain

import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import play.api.libs.json.{Json, Format, JsResult, JsValue}

case class User(email: String, username: String, avatarUrl: String, firstName: String, lastName: String)

sealed trait Member
case class Human(user: User) extends Member
case object LT extends Member

case class Account(name: String, user: Member)

sealed trait Way
case object Plus extends Way
case object Minus extends Way

abstract class AccountOperation(way: Way)

case class Attachment(contentType: String,
                      stub: Boolean,
                      data: Array[Byte])

trait AttachmentSerializer {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val rds: Reads[Array[Byte]] = (__ \ "data").read[String].map{ arr: String => Base64.decode(arr) }
  implicit val wrs: Writes[Array[Byte]] = (__ \ "data").write[String].contramap{ (a: Array[Byte]) => new String(Base64.encode(a)) }
  implicit val fmt: Format[Array[Byte]] = Format(rds, wrs)
  implicit val attachmentFormatter = Json.format[Attachment]
}

case class Invoice(invoice: InvoiceRequest, pdfDocument: Attachment) extends AccountOperation(Plus) // TODO add file

// une facture/un revenu se fait subdivisé entre le membre LT, la structure etc..
case class Affectation(incomeOperation: AccountOperation,
                       account: Account,
                       value: Double)

abstract class Expense(value: Double) extends AccountOperation(Minus)

case class CommonExpenseType(name: String)

case class CommonExpense(name: String,
                         description: String,
                         createdAt: DateTime,
                         expenseType: CommonExpenseType,
                         value: Double) extends Expense(value) // TODO add file

sealed trait SalaryType
case object MonthlySalary extends SalaryType
case object VariableBonus extends SalaryType
case object PointBonus extends SalaryType
case object NonChargedSalary extends SalaryType

case class Salary(name: String,
                  description: String,
                  createdAt: DateTime,
                  recipient: Member,
                  value: Double,
                  salaryType: SalaryType) extends Expense(value) // TODO add file

case class MemberExpense(description: String,
                         createdAt: DateTime,
                         recipient: Member,
                         value: Double) extends Expense(value) // TODO add file