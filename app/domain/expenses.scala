package domain

import org.joda.time.DateTime

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
