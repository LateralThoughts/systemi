package domain

import org.joda.time.DateTime

case class User(email: String, username: String, avatarUrl: String, firstName: String, lastName: String)

sealed trait Member
case class Human(user: User) extends Member
case object LT extends Member

case class Account(name: String, user: Member)

sealed trait Way
case object Plus extends Way
case object Minus extends Way

abstract class AccountOperation(way: Way)

case class Invoice(invoice: InvoiceRequest) extends AccountOperation(Plus) // TODO add file

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