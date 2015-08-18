/**
 * iSIGHT Partners, Inc. Proprietary
 */

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/7/15
 */
trait ScenarioPayload {

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
