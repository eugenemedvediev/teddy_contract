one-jar
 - install sbt
 - sbt one-jar
 - java -jar target/scala-2.10/teddy_contract_2.10-0.0.1-one-jar.jar ~/github/teddy_contract/configurations/dummy.json

test
 - sbt test
 - sbt dummy/test
 - sbt scenario/test

it
 - sbt it:test
 - sbt dummy/it:test
 - sbt scenario/it:test

run
 - sbt "dummy/run /Users/ievgen/github/teddy_contract/configurations/dummy.json"
