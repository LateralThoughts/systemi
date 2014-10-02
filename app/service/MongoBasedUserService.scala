package service

import play.Logger
import play.api.Play.current
import reactivemongo.api.DefaultDB
import securesocial.core._
import securesocial.core.services._
import securesocial.core.providers.{MailToken}

import scala.concurrent.Future
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import domain._
import play.api.libs.json.Json


class MongoBasedUserService()
  extends UserService[BasicProfile]
  with BasicProfileSerializer {
  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * Finds a SocialUser that matches the specified id
   *
   * @param providerId the provider id
   * @param userId the user id
   * @return an optional profile
   */
  def find(providerId: String, userId: String): Future[Option[BasicProfile]]= {
    val db = ReactiveMongoPlugin.db

    db.collection[JSONCollection]("users")
      .find(Json.obj("providerId" -> providerId, "userId" -> userId))
      .one[BasicProfile]
  }

  /**
   * Finds a profile by email and provider
   *
   * @param email - the user email
   * @param providerId - the provider id
   * @return an optional profile
   */
  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]]= ???

  /**
   * Saves a profile.  This method gets called when a user logs in, registers or changes his password.
   * This is your chance to save the user information in your backing store.
   *
   * @param profile the user profile
   * @param mode a mode that tells you why the save method was called
   */
  def save(profile: BasicProfile, mode: SaveMode): Future[BasicProfile] = {

    val db = ReactiveMongoPlugin.db

    db.collection[JSONCollection]("users")
      .update(Json.obj("email" -> profile.email), Json.toJson(profile), upsert = true)
      .map {errors =>
      if (errors.inError)
        Logger.warn(s"Failed to save user '${profile.email} when logged")
      else {
        Logger.info(s"Successfully saved user '${profile.email}' as log in happened")
      }
    }

    Future(profile)
  }

  /**
   * Links the current user to another profile
   *
   * @param current The current user instance
   * @param to the profile that needs to be linked to
   */
  def link(current: BasicProfile, to: BasicProfile): Future[BasicProfile]= ???

  /**
   * Returns an optional PasswordInfo instance for a given user
   *
   * @param user a user instance
   * @return returns an optional PasswordInfo
   */
  def passwordInfoFor(user: BasicProfile): Future[Option[PasswordInfo]]= ???

  /**
   * Updates the PasswordInfo for a given user
   *
   * @param user a user instance
   * @param info the password info
   * @return
   */
  def updatePasswordInfo(user: BasicProfile, info: PasswordInfo): Future[Option[BasicProfile]]= ???

  /**
   * Saves a mail token.  This is needed for users that
   * are creating an account in the system or trying to reset a password
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token The token to save
   */
  def saveToken(token: MailToken): Future[MailToken] = ???

  /**
   * Finds a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token the token id
   * @return
   */
  def findToken(token: String): Future[Option[MailToken]]= ???

  /**
   * Deletes a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param uuid the token id
   */
  def deleteToken(uuid: String): Future[Option[MailToken]]= ???

  /**
   * Deletes all expired tokens
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   */
  def deleteExpiredTokens() {}

}