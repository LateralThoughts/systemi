package domain


import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, SocialProfile}

/**
 * The user object.
 *
 * @param id The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName Maybe the last name of the authenticated user.
 * @param fullName Maybe the full name of the authenticated user.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 */
case class User(
                 id: UUID,
                 loginInfo: LoginInfo,
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 fullName: Option[String] = None,
                 email: Option[String] = None,
                 avatarURL: Option[String] = None,
                 oAuth2Info: Option[OAuth2Info] = None,
                 gender: Option[String] = None) extends SocialProfile with Identity