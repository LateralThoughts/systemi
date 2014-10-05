package domain

import org.scalatest.{Matchers, FunSuite}
import securesocial.core.OAuth2Info
import securesocial.core.AuthenticationMethod
import securesocial.core.BasicProfile
import play.api.libs.json.Json

class MemberSerializerTest extends FunSuite with Matchers with MemberSerializer {

  test("should serialize to Json") {
    val human = Human(BasicProfile("google", "107118519041904062585", Option("Vincent"), Option("Doba"), Option("Vincent Doba"), Option("vincent.doba@gmail.com"), Option("https://lh3.googleusercontent.com/-4zmtKZIF5Gc/AAAAAAAAAAI/AAAAAAAAAIY/Y1e7O64ELhc/photo.jpg?sz=50"), new AuthenticationMethod("oauth2"), Option.empty, Option(new OAuth2Info("ya29.lQCpUdcQiDexRqH7ezU3986hQBWLrlWpxQnqNyFli44hC9k1RbjT1Pbi", Option("Bearer"), Option(3599), Option.empty)), Option.empty))
    Json.toJson(human)
  }

}
