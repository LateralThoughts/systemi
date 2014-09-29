package domain

import org.scalatest.FunSuite
import org.scalatest.Matchers
import play.api.libs.json._

class UserSerializerTest extends FunSuite
                                   with Matchers
                                   with UserSerializer {

    test("should deserialize user from json") {

        val userJson = """{
            "userId": "1u",
            "firstName": "Olivier",
            "lastName": "Girardot",
            "email": "o.girardot@mail.com",
            "avatarUrl": "no.url.com/avatar.jpg"
        }"""

        val user: User = User(userId = "1u",
            firstName = "Olivier",
            lastName = "Girardot",
            email = "o.girardot@mail.com",
            avatarUrl = "no.url.com/avatar.jpg"
        )

        Json.parse(userJson).validate(userFormatter).get should be(user)
    }

}