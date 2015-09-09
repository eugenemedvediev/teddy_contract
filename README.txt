
Local environment:
 - install sbt (http://www.scala-sbt.org/0.13/tutorial/Installing-sbt-on-Mac.html)
 - change key "elastic.home" in src/main/resources/application.conf
 - $ sbt
 - > container:start
 - Go to http://localhost:8080

Make war file:
 - install sbt
 - $ sbt package
 - war file is in /target/scala-2.10/ folder

Load configuration:
 - install sbt
 - ensure that server you want to load is started
 - sbt "run loadServer http://host:port/stub server_name configurations/ws.json"
   Example: sbt "run loadServer http://10.102.50.24:8080/stub Angel configurations/ws.json"

one-jar
 - install sbt
 - sbt one-jar
 - java -jar target/scala-2.10/teddy_contract_2.10-0.0.1-one-jar.jar ~/github/teddy_contract/configurations/dummy.json

