package domain

import org.joda.time.LocalDate
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.Json

class ActivitySerializerTest extends FunSuite
                     with Matchers
                     with ActivitySerializer {

  test("should deserialize activities properly from json") {
    val data =
      """
        {
          "days":[
            {
              "day":"2014-09-22T22:00:00.000Z",
              "halfUp":true,
              "halfDown":true,
              "state":0,
              "$$hashKey":"00C"
            },
            {
              "day":"2014-09-23T22:00:00.000Z",
              "halfUp":true,
              "halfDown":true,
              "state":0,
              "$$hashKey":"00D"
            },             {
              "day":"2014-09-24T22:00:00.000Z",
              "halfUp":true,
              "halfDown":true,
              "state":0,
              "$$hashKey":"00E"
            },
            {
              "day":"2014-09-25T22:00:00.000Z",
              "halfUp":true,
              "halfDown":true,
              "state":0,
              "$$hashKey":"00F"
            },
            {
              "day":"2014-09-26T22:00:00.000Z",
              "halfUp":false,
              "halfDown":false,
              "state":3,
              "$$hashKey":"00G"
            },
            {
              "day":"2014-09-27T22:00:00.000Z",
              "halfUp":false,
              "halfDown":false,
              "state":3,
              "$$hashKey":"00H"
            },
            {
              "day":"2014-09-28T22:00:00.000Z",
              "halfUp":true,
              "halfDown":true,
              "state":0,
              "$$hashKey":"011"
            }
        ],
        "tjm":450,
        "contractor":"Vincent Doba",
        "title":"Octobre 2014",
        "client":{
                "_id" : {
                  "$oid": "532afca061ce6a2db986839f"
                },
        				"name" : "VIDAL",
        				"address" : "27 rue camille desmoulins",
        				"postalCode" : "94550",
        				"city": "chevilly",
        				"country": "France"
        			},
        "numberOfDays":5
        }
      """

    val days = List(
      ActivityDay(new LocalDate(2014,9,22), true, true),
      ActivityDay(new LocalDate(2014,9,23), true, true),
      ActivityDay(new LocalDate(2014,9,24), true, true),
      ActivityDay(new LocalDate(2014,9,25), true, true),
      ActivityDay(new LocalDate(2014,9,26), false, false),
      ActivityDay(new LocalDate(2014,9,27), false, false),
      ActivityDay(new LocalDate(2014,9,28), true, true)
    )

    val activity = ActivityRequest(450.0, 5, ClientRequest("VIDAL", "27 rue camille desmoulins", "94550", "chevilly", "France"), "Vincent Doba", "Octobre 2014", days)

    Json.parse(data).validate(activityReqFormat).get should be (activity)

  }

}
