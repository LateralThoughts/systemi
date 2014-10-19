package service

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider, CommonSocialProfile}
import domain._
import play.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future


class MongoBasedUserService()
  extends IdentityService[User]
  with UserSerializer {

  def save(profile: CommonSocialProfile):Future[User] = {
    val user = User(
      profile.loginInfo,
      profile.firstName,
      profile.lastName,
      profile.fullName,
      profile.email,
      profile.avatarURL)
    save(user)
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * Finds a SocialUser that matches the specified id
   *
   * @param loginInfo object containing the provider id and the user id
   * @return an optional profile
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]]= {
    val db = ReactiveMongoPlugin.db

    db.collection[JSONCollection]("users")
      .find(Json.obj("providerId" -> loginInfo.providerID, "userId" -> loginInfo.providerKey))
      .one[User]
  }

  /**
   * Saves a profile.  This method gets called when a user logs in, registers or changes his password.
   * This is your chance to save the user information in your backing store.
   *
   * @param profile the user profile
   */
  def save(profile: User): Future[User] = {

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

}