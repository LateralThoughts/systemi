package domain

import org.scalatest.{Matchers, FunSuite}
import securesocial.core.{AuthenticationMethod, BasicProfile}
import play.api.libs.json._

class BasicProfileSerializerTest extends FunSuite
                                 with Matchers
                                 with BasicProfileSerializer {

    val profile = new BasicProfile(providerId = "google",
        userId = "u",
        firstName = Option("Olivier"),
        lastName = Option("Girardot"),
        fullName = Option("Olivier Girardot"),
        email = Option("ogirardot@mail.com"),
        avatarUrl = Option("avatar.jpg"),
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

    test("should serialize a BasicProfile") {
        Json.parse(dbProfile).validate(basicProfileFormatter).get should be (profile)
    }

    test("should deserialize a BasicProfile") {
        Json.toJson(profile) should be (Json.parse(dbProfile))
    }
}
