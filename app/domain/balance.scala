package domain

import julienrf.variants.Variants
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

sealed trait Way
case object Plus extends Way
case object Minus extends Way

sealed abstract class AccountOperation(way: Way)

case class Invoice(_id: BSONObjectID,
                   invoice: InvoiceRequest,
                   pdfDocument: Attachment,
                   statuses: List[Status],
                   lastStatus: Status,
                   paymentStatus: String = "unpaid",
                   canceled:Boolean = false,
                   affectationStatus: String = "unaffected") extends AccountOperation(Plus) {

  def totalHT = InvoiceLinesAnalyzer.computeTotalHT(invoice.invoice)
}

case class CommonExpenseType(name: String)

case class CommonExpense(name: String,
                         description: String,
                         createdAt: DateTime,
                         expenseType: CommonExpenseType,
                         value: Double) extends AccountOperation(Minus) // TODO add file

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
                  salaryType: SalaryType) extends AccountOperation(Minus) // TODO add file

case class MemberExpense(description: String,
                         createdAt: DateTime,
                         recipient: Member,
                         value: Double) extends AccountOperation(Minus) // TODO add file


trait MemberBasedExpenseSerializer extends MemberSerializer {
  implicit val salaryType = Variants.format[SalaryType]
  implicit val commonExpenseType = Json.format[CommonExpenseType]
  implicit val common = Json.format[CommonExpense]
  implicit val salary = Json.format[Salary]
  implicit val member = Json.format[MemberExpense]
}