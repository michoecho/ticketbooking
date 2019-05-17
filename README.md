# ticketbooking
A movie ticket booking system.

This project is written in Spring with Kotlin, using Spring Boot, Spring MVC, Spring Data JPA, and H2.

To run the application, run
```
./gradlew bootRun
```
from the project root.

The application runs on port 8080 by default. This setting can by overriden by adding
```
server.port=<port_number>
```
to `src/main/resources/application.properties`.

# Demo

The H2 in-memory database was used for the purpose of demonstration.
The database is initialized with test data at application launch with `src/main/pl/touk/ticketbooking/DatabaseInitializer.kt`.
The demos assume this particular configuration (they use hard-coded entity IDs). 
The demos assume that the application is running on localhost:8080.

To run the use case demo (as described in the specification), launch the application first, then run
```
cd demo
./happy_path.sh
```
from the project root.

To run an extended demo, testing the violations of business rules, launch the application first, then run
```
cd demo
./unhappy_path.sh
```
from the project root.
