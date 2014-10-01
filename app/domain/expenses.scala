package domain

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Expense (expenseValue: Double,
                    expenseType: String,
                    account: Account,
                    document: Attachment,
                    description: String,
                    createdAt: DateTime)

trait ExpenseSerializer extends AccountSerializer with AttachmentSerializer {
  implicit val expenseFormatter = Json.format[Expense]
}