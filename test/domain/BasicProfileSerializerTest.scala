package domain

import org.scalatest.{Matchers, FunSuite}
import securesocial.core.{AuthenticationMethod, User}
import play.api.libs.json._

class UserSerializerTest extends FunSuite
                                 with Matchers
                                 with UserSerializer {

    val profile = new User(providerId = "google",
        userId = "u",
        firstName = Option("Olivier"),
        lastName = Option("Girardot"),
        fullName = Option("Olivier Girardot"),
        email = Option("ogirardot@mail.com"),
        avatarURL = Option("avatar.jpg"),
        authMethod = AuthenticationMethod.OAuth2
    )

    val dbProfile ="""{
        "providerId": "google",
        "userId": "u",
        "firstName": "Olivier",
        "lastName": "Girardot",
        "fullName": "Olivier Girardot",
        "email": "ogirardot@mail.com",
        "avatarUrl": "avatar.jpg",
        "authMethod": {"method":"oauth2"}
    }"""

    test("should serialize a User") {
        Json.parse(dbProfile).validate(userFormatter).get should be (profile)
    }

    test("should deserialize a User") {
        Json.toJson(profile) should be (Json.parse(dbProfile))
    }
}
