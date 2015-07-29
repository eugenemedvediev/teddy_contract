/**
 * iSIGHT Partners, Inc. Proprietary
 */

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/7/15
 */
trait Payload {

  val CONFIGURATION =
    """{
      |  "description": "Stub Test Configuration",
      |  "api": [
      |    {
      |      "method": "POST",
      |      "path": "/test",
      |      "scenarios": [
      |        {
      |         "name":"ok",
      |         "contentType": "application/json",
      |         "body": {},
      |         "code": 200,
      |         "headers":{}
      |        },
      |        {
      |         "name":"error",
      |         "contentType": "application/json",
      |         "body": {"error": "POST /test error message"},
      |         "code": 503,
      |         "headers":{}
      |        }
      |      ]
      |    },
      |    {
      |      "method": "GET",
      |      "path": "/test2",
      |      "scenarios": [
      |        {
      |         "contentType": "application/json",
      |         "body": {},
      |         "code": 200,
      |         "headers":{}
      |        },
      |        {
      |         "name":"error",
      |         "contentType": "application/json",
      |         "body": {"error": "GET /test error message"},
      |         "code": 503,
      |         "headers":{}
      |        },
      |        {
      |         "name":"validation",
      |         "contentType": "application/json",
      |         "body": {"error": "GET /test validation message"},
      |         "code": 503,
      |         "headers":{}
      |        }
      |      ]
      |    }
      |  ]
      |}
      | """.stripMargin

}
