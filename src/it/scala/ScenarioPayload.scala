/**
 * iSIGHT Partners, Inc. Proprietary
 */

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/7/15
 */
trait ScenarioPayload {

  val DUMMY_CONFIGURATION =
    """{
      |  "description": "Dummy Test Configuration",
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

  val SCENARIO_CONFIGURATION =
    """
      |{
      |  "description": "Scenario Test Configuration",
      |  "api": [
      |    {
      |      "method": "POST",
      |      "path": "/wires/servers",
      |      "scenarios": [
      |        {
      |          "name": "ok",
      |          "request": {
      |            "headers": {
      |              "Content-Type": "application/json",
      |              "Accept": "application/json",
      |              "Authorization": "Token blablabla"
      |            },
      |            "body": {
      |              "server": "Angel",
      |              "description": "Angel Server Description"
      |            }
      |          },
      |          "response": {
      |            "headers": {
      |              "Content-Type": "application/json"
      |            },
      |            "body": {
      |              "id": 1,
      |              "server": "Angel",
      |              "description": "Angel Server Description",
      |              "port": 8090
      |            },
      |            "code": 200
      |          }
      |        }
      |      ]
      |    },
      |    {
      |      "method": "POST",
      |      "path": "/wires/savedsearches",
      |      "scenarios": [
      |        {
      |          "name": "ok",
      |          "request": {
      |            "headers": {
      |              "Content-Type": "application/json",
      |              "Accept": "application/json",
      |              "Authorization": "Token blablabla"
      |            },
      |            "body": {
      |              "title": "google",
      |              "content": "google today"
      |            }
      |          },
      |          "response": {
      |            "headers": {
      |              "Content-Type": "application/json"
      |            },
      |            "body": {
      |              "id": 1,
      |              "title": "google",
      |              "content": "google today"
      |            },
      |            "code": 200
      |          }
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin


}
