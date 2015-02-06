package code.api.v1_3_0

import java.util.Date
import code.model._
import code.model.CardReplacementInfo
import code.model.PhysicalCard
import code.model.PinResetInfo

case class PhysicalCardsJSON(
  cards : List[PhysicalCardJSON])

case class PhysicalCardJSON(
   bank_card_number : String,
   name_on_card : String,
   issue_number : String,
   serial_number : String,
   valid_from_date : Date,
   expires_date : Date,
   enabled : Boolean,
   cancelled : Boolean,
   on_hot_list : Boolean,
   technology : String,
   networks : List[String],
   allows : List[String],
   account : code.api.v1_2_1.AccountJSON,
   replacement : ReplacementJSON,
   pin_reset : List[PinResetJSON],
   collected : Date,
   posted : Date)

case class ReplacementJSON(
  requested_date : Date,
  reason_requested : String)

case class PinResetJSON(
   requested_date : Date,
   reason_requested : String)

object JSONFactory1_3_0 {

  def stringOrNull(text : String) =
    if(text == null || text.isEmpty)
      null
    else
      text

  def createPinResetJson(resetInfo: PinResetInfo) : PinResetJSON = {
    PinResetJSON(
      requested_date = resetInfo.requestedDate,
      reason_requested = resetInfo.reasonRequested match {
        case FORGOT => "forgot"
        case GOOD_SECURITY_PRACTICE => "routine_security"
      }
    )
  }

  def createReplacementJson(replacementInfo: CardReplacementInfo) : ReplacementJSON = {
    ReplacementJSON(
      requested_date = replacementInfo.requestedDate,
      reason_requested = replacementInfo.reasonRequested match {
        case LOST => "lost"
        case STOLEN => "stolen"
        case RENEW => "renewal"
      }
    )
  }

  def cardActionsToString(action : CardAction) : String = {
    action match {
      case CREDIT => "credit"
      case DEBIT => "debit"
      case CASH_WITHDRAWAL => "cash_withdrawal"
    }
  }

  def createAccountJson(bankAccount : BankAccount, user : User) : code.api.v1_2_1.AccountJSON = {
    val views = bankAccount.views(user).getOrElse(Nil)
    val viewsJson = views.map(code.api.v1_2_1.JSONFactory.createViewJSON)
    code.api.v1_2_1.JSONFactory.createAccountJSON(bankAccount, viewsJson)
  }

  def createPhysicalCardsJSON(cards : Set[PhysicalCard], user : User) : PhysicalCardsJSON = {

    val cardJsons = cards.map(card => {

      PhysicalCardJSON(
        bank_card_number = stringOrNull(card.bankCardNumber),
        name_on_card = stringOrNull(card.nameOnCard),
        issue_number = stringOrNull(card.issueNumber),
        serial_number = stringOrNull(card.serialNumber),
        valid_from_date = card.validFrom,
        expires_date = card.expires,
        enabled = card.enabled,
        cancelled = card.cancelled,
        on_hot_list = card.onHotList,
        technology = stringOrNull(card.technology),
        networks = card.networks.toList,
        allows = card.allows.map(cardActionsToString).toList,
        account = card.account.map(createAccountJson(_, user)).getOrElse(null),
        replacement = card.replacement.map(createReplacementJson).getOrElse(null),
        pin_reset = card.pinResets.map(createPinResetJson),
        collected = card.collected.map(_.date).getOrElse(null),
        posted = card.posted.map(_.date).getOrElse(null)
      )

    })

    PhysicalCardsJSON(cardJsons.toList)
  }

}
