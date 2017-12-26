# Scala integration tests with sbt and Docker. Example

We are trying here to create a project that would illustrate how to create and run integration tests for an HTTP REST API service using sbt and Docker

# Structure

There are two sub projects: `app` and `tester`. The first one is the service itself. If you run it you will get a service that is listening on port 8080 on localhost and could respond with a simple JSON response when queried GET localhost:8080/zorro

The second one `tester` is an application that is trying to test the functionality of the `app`.

# Goal

The goal is to run both `app` and `tester` in separate Docker containers, let the tester do its tests, then shutdown and remove the containers and print the results. We are not there yet.