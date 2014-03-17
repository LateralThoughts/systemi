package oauth 

import play.api.Play._
import dispatch._
import scala.util.parsing.json.JSON._

object GoogleOAuth {

  def getAuthCode(rq: play.api.mvc.Request[play.api.mvc.AnyContent]): Option[String] = {
    val err = rq.queryString.get("error").flatMap(_.headOption)
    val code = rq.queryString.get("code").flatMap(_.headOption)
    code.asInstanceOf[Option[String]]
  }

  def getGoogleAuthUrl: String = {
    val getauthurl = current.configuration.getString("googleapi.urlgetauth").getOrElse("")
    val authscope = current.configuration.getString("googleapi.scope").getOrElse("")
    val redirecturi = current.configuration.getString("googleapi.redirect_uri").getOrElse("")
    val clientid = current.configuration.getString("googleapi.client_id").getOrElse("")

    s"$getauthurl?scope=$authscope&redirect_uri=$redirecturi&response_type=code&client_id=$clientid"
  }

  def getAccessToken(authcode: String): String = {
    val gettokenurl = current.configuration.getString("googleapi.urlgettoken").getOrElse("")
    val clientid = current.configuration.getString("googleapi.client_id").getOrElse("")
    val clientsecret = current.configuration.getString("googleapi.client_secret").getOrElse("")
    val redirecturi = current.configuration.getString("googleapi.redirect_uri").getOrElse("")

    val postdata = "code=" + authcode + "&client_id=" + clientid + "&client_secret=" + clientsecret + "&redirect_uri=" + redirecturi + "&grant_type=authorization_code"
    val http = new dispatch.Http
    val access_token = http(url(gettokenurl) << (postdata, "application/x-www-form-urlencoded") >- {
      str =>
        {
          val json = parseFull(str).get.asInstanceOf[Map[String, Any]]
          json("access_token")
        }
    })
    http.shutdown
    access_token.toString
  }

}