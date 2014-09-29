package domain

sealed trait Way
case object Plus extends Way
case object Minus extends Way

abstract class AccountOperation(way: Way)

// une facture/un revenu se fait subdivisé entre le membre LT, la structure etc..
case class Affectation(incomeOperation: AccountOperation,
                       account: Account,
                       value: Double)
