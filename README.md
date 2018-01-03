# Scala integration tests with sbt and Docker. Example

We are trying here to create a project that would illustrate how to create and run integration tests for an HTTP REST API service using sbt and Docker

# Structure

There are two sub projects: `app` and `tester`. The first one is the service itself. If you run it you will get a service that is listening on port 8080 on localhost and could respond with a simple JSON response when queried GET localhost:8080/zorro

The second one `tester` is an application that is trying to test the functionality of the `app`.

# Goal

The goal is to implement a task in sbt so that Jenkins could simply invoke the task and when the exit code is zero, publish the application.

The task is supposed to build a Docker image with `app` and run it, and then invoke tests which will connect to the `app` in a Docker container and test its functionality.