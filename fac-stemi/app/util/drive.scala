package util

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.util.Arrays
import java.io.{BufferedReader, InputStreamReader}

trait DrivePersistence {
	val CLIENT_ID = "676482138281.apps.googleusercontent.com"
  	val CLIENT_SECRET = "3Y4JopR-FmyCLqoVvpID05Rw"
	val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
  
  def createClient() = {
    val httpTransport = new NetHttpTransport()
    val jsonFactory = new JacksonFactory()
    val flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
        .setAccessType("online")
        .setApprovalPrompt("auto").build()
    
    val url = flow.newAuthorizationUrl().build()
    println("Please open the following URL in your browser then type the authorization code:")
    println("  " + url)
    val br = new BufferedReader(new InputStreamReader(System.in))
    val code = br.readLine()
    
    val response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute()
    val credential = new GoogleCredential().setFromTokenResponse(response)
    
    //Create a new authorized API client
    val service = new Drive.Builder(httpTransport, jsonFactory, credential).build();

    //Insert a file  
    val body = new File()
    body.setTitle("My document")
    body.setDescription("A test document")
    body.setMimeType("text/plain")
    
    val fileContent = new java.io.File("document.txt")
    val mediaContent = new FileContent("text/plain", fileContent)
    val file = service.files().insert(body, mediaContent).execute()
    println("File ID: " + file.getId())
  }

}