## Project definition

This project is an attempt at a CurrencyFair development test.

## Running the code

The project is a Spring Boot application. 

The project exposes the following HTTP call:

 - Request to make a trade
	- HTTP POST to '/trade' with a JSON object
	- With Content-type: "application/json"
	- Example : {"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}	

The project also defines a results HTML page, served from the web root '/'

The Application executes from the command line and uses an embedded web server. There is no storage of data to a persistent store. 

## Dependencies
 - Java 1.8
 - Gradle 2.9

### Configuration 
Application settings are defined in the file 'src/main/resources/application.properies'

These include HTTP port, web server thread pool size, and also application specific items (allowed currencies, countries, input value ranges, etc.)

The Spring Boot web server will run on port 8080 by default.

### Building / testing the project

This is a stand alone Spring Boot application built using Gradle. 

 - build (do not run tests) ./gradlew build -x test
 - build (runs unit tests): ./gradlew clean build 
 - build (runs integration test)*: ./gradlew integrationTest
 - build (runs load test)*: ./gradlew loadTest

A build will generate a jar file in ./build/libs/currencyfair-test-ab-0.0.1.jar

This can be run with the Java command:

> java -jar ./build/libs/currencyfair-test-ab-0.0.1.jar

Once the server is running, it will accept API submissions to http://localhost:8080/trade 
Inputs into this API call are validated, with error information logged, but not returned to the API user. This was a design choice.   

Once the server is running, the user can also visit http://localhost:8080/ and view a simple HTML page. This page updates dynamically as valid trade requests are submitted to the service. 

Note: the integrationTest and loadTest tasks require an executing server to run against. This instance should be restarted for each test run so it will return clean results. 

## Future work
This application meets the requirements set out in the assignment. It will not scale (horizontally or vertically) without some changes and a bit of plumbing however:

 - One bottleneck is the web server thread capacity (server.tomcat.max-threads in application.properties). This sets number of concurrent {incoming trades, outgoing updates} API requests it can process.
 - The size of the internal event queue at the Processor is also a limit, but less so than the API thread count.
 - There is another bottleneck at the HTML event layer - this can support a maximum number of clients before performance will degrade.

To scale better, a few changes could be made to a 'full' application:

 - Create many "Consumer" API logic instance nodes. Round robin requests between then using a load balancer. Session affinity is not required.
 
 - Insert a network based messaging layer between the "Consumer" and "Processor" logic. There are *many* options here. Rabbit.MQ and Apache Camel, bare bones Erlang or TCP or UDP messages, Kafka & Spark, etc. Choice should depend on the reliability /failure models chosen for the service and any tradeoff between reliability ('delivery at most once', 'guaranteed delivery', etc.) and speed.
 
 - Create many Processor instance nodes, with {shared, one, or many} Processor instances per market type ({EUR,GBP}, {EUR,USD}), depending on traffic (Bonus points for being able to dynamically configure these pools!). Add routing to the messaging layer to accommodate this.
 
 - At a Processor, use an independent robust and fast in-memory service to store data between all relevant Processor instances. This shared memory will become the 'transactional' point for trading data. It could also be partitioned per market type to partially prevent a SPOF. In the current test version, the 'persistance' is provided by a pair of in-memory Java Maps. This is not robust or scalable!
 
 - The internal event queue is currently passing around a POJO representing the trade request. POJO serialization is not efficient. The object could be replaced by a more efficient binary representation (ProtocolBuffers, etc). The choice of technology for a messaging layer would affect this choice.
 
  - At the front end server teir, events can be routed to a set of event update nodes, hosted at different front end URLs / behind a load balancer. Users will subscribe to specific update events via a specific node/set of nodes. 
   
## Notes

Some notes and assumptions on this project...

I have used a library (the Spring reactor project - LMAX Ring Buffer) to implement the internal queue for processing events and feeds in this project.
As this is a programming test and not a real project, it might not be the right approach to just use a library for my non blocking event processor.
PLEASE LET ME KNOW IF THIS IS THE CASE, and I will write my own version of a non blocking event queue (although maybe not as comprehensively as the LMAX team!)

There is no user level security or account validation in this application. A login and subsequent per-API-request token could be required for each API call as a means of adding security against man in the middle or other spoofing attacks. 
This session key would be encrypted using a secret (or randomly generated rotating secrets) known on the server side only. Minor state information (UserId, Timestamp) could be stored inside this key to remove any requirement to store session state on a server node. Making a key single use or setting a low TTL on a key would mitigate against possible repeat attacks. Successful requests could have a new key provided in each response. The server could enforce this key approach and also only allow HTTPS traffic (and disallow older HTTPS 'versions', etc.) for API requests. 
  
The specification for the JSON trade request object gives an example for the date format for the 'timePlaced' field ('24-JAN-15 10:27:44'). 
It is not clear what time zone this time value in. I have assumed that for the purposes of this test, the server and all clients are in the same time zone.
  
Any rounding of amounts, rates, is done with a "rounding-half-up" rule.

I have not ordered the market names according to convention (https://en.wikipedia.org/wiki/Currency_pair).  

There is no non-volatile persistence in the service, and also there is no attempt to match trades.

I have used jQuery 2.2.4 for the web page part of this test. This means that older browsers (< IE 9, etc.) will not be able to use the updates HTML page. There is also no graceful failure in the web page if the browser does not support websockets, or if the browser does not support / has disabled JavaScript, or other possible error cases. A production version of this tool would have fallback functionality for older 'socket-like' approaches like long polling, non JS browsers, timeout and reconnect policies on error, etc.    

The integration and load tests could stand to be more robust and complete! They cover the basics but nothing else.

   