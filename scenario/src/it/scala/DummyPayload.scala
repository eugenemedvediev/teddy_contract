/**
 * iSIGHT Partners, Inc. Proprietary
 */

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/7/15
 */
trait DummyPayload {

  val DUMMY_CONFIGURATION =
    """
      |{
      |  "description": "Wires Submission + Search",
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
      |        },
      |        {
      |          "name": "bad request",
      |          "request": {
      |            "headers": {
      |              "Content-Type": "application/json",
      |              "Accept": "application/json",
      |              "Authorization": "Token blablabla"
      |            },
      |            "body": {
      |              "description": "Angel Server Description"
      |            }
      |          },
      |          "response": {
      |            "headers": {
      |              "Content-Type": "application/json"
      |            },
      |            "body": {
      |              "error": "Field 'server' is required"
      |            },
      |            "code": 400
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
      |        },
      |        {
      |          "name": "bad request",
      |          "request": {
      |            "headers": {
      |              "Content-Type": "application/json",
      |              "Accept": "application/json",
      |              "Authorization": "Token blablabla"
      |            },
      |            "body": {
      |              "content": "google today"
      |            }
      |          },
      |          "response": {
      |            "headers": {
      |              "Content-Type": "application/json"
      |            },
      |            "body": {
      |              "error": "Field 'title' is required"
      |            },
      |            "code": 400
      |          }
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin

  val DUMMY_FULL_CONFIGURATION =
    """{
      |  "description": "Dummy Test Configuration",
      |  "api": [
      |  {
      |    "method": "POST",
      |    "path": "/wires/servers",
      |    "scenarios": [
      |      {
      |        "name": "ok",
      |        "request": {
      |          "headers": {
      |            "Content-Type": "application/json",
      |            "Accept": "application/json",
      |            "Authorization": "Token blablabla"
      |          },
      |          "body": {
      |            "server": "Angel",
      |            "description": "Angel Server Description"
      |          }
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "id": 1,
      |            "server": "Angel",
      |            "description": "Angel Server Description",
      |            "port": 8090
      |          },
      |          "code": 200
      |        }
      |      },
      |      {
      |        "name": "bad request",
      |        "request": {
      |          "headers": {
      |            "Content-Type": "application/json",
      |            "Accept": "application/json",
      |            "Authorization": "Token blablabla"
      |          },
      |          "body": {
      |            "description": "Angel Server Description"
      |          }
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "error": "Field 'server' is required"
      |          },
      |          "code": 400
      |        }
      |      }
      |    ]
      |  },
      |  {
      |    "method": "POST",
      |    "path": "/wires/savedsearches",
      |    "scenarios": [
      |      {
      |        "name": "ok",
      |        "request": {
      |          "headers": {
      |            "Content-Type": "application/json",
      |            "Accept": "application/json",
      |            "Authorization": "Token blablabla"
      |          },
      |          "body": {
      |            "title": "google",
      |            "content": "google today"
      |          }
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "id": 1,
      |            "title": "google",
      |            "content": "google today"
      |          },
      |          "code": 200
      |        }
      |      },
      |      {
      |        "name": "bad request",
      |        "request": {
      |          "headers": {
      |            "Content-Type": "application/json",
      |            "Accept": "application/json",
      |            "Authorization": "Token blablabla"
      |          },
      |          "body": {
      |            "content": "google today"
      |          }
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "error": "Field 'title' is required"
      |          },
      |          "code": 400
      |        }
      |      }
      |    ]
      |  }
      |]}
    """.stripMargin

  val DUMMY_NO_HEADER_CONFIGURATION =
    """{
      |  "description": "Dummy Test Configuration",
      |  "api": [
      |  {
      |    "method": "POST",
      |    "path": "/wires/servers",
      |    "scenarios": [
      |      {
      |        "name": "ok",
      |        "request": {
      |          "headers": {},
      |          "body": {
      |            "server": "Angel",
      |            "description": "Angel Server Description"
      |          }
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "id": 1,
      |            "server": "Angel",
      |            "description": "Angel Server Description",
      |            "port": 8090
      |          },
      |          "code": 200
      |        }
      |      },
      |      {
      |        "name": "bad request",
      |        "request": {
      |          "headers": {
      |            "Content-Type": "application/json",
      |            "Accept": "application/json",
      |            "Authorization": "Token blablabla"
      |          },
      |          "body": {
      |            "description": "Angel Server Description"
      |          }
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "error": "Field 'server' is required"
      |          },
      |          "code": 400
      |        }
      |      }
      |    ]
      |  }
      |]}
    """.stripMargin


  val DUMMY_NO_BODY_CONFIGURATION =
    """{
      |  "description": "Dummy Test Configuration",
      |  "api": [
      |  {
      |    "method": "POST",
      |    "path": "/wires/servers",
      |    "scenarios": [
      |      {
      |        "name": "ok",
      |        "request": {
      |        "headers": {
      |            "Content-Type": "application/json",
      |            "Accept": "application/json",
      |            "Authorization": "Token blablabla"
      |          },
      |          "body": {}
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "id": 1,
      |            "server": "Angel",
      |            "description": "Angel Server Description",
      |            "port": 8090
      |          },
      |          "code": 200
      |        }
      |      },
      |      {
      |        "name": "bad request",
      |        "request": {
      |          "headers": {
      |            "Content-Type": "application/json",
      |            "Accept": "application/json",
      |            "Authorization": "Token blablabla"
      |          },
      |          "body": {
      |            "description": "Angel Server Description"
      |          }
      |        },
      |        "response": {
      |          "headers": {
      |            "Content-Type": "application/json"
      |          },
      |          "body": {
      |            "error": "Field 'server' is required"
      |          },
      |          "code": 400
      |        }
      |      }
      |    ]
      |  }
      |]}
    """.stripMargin

}
