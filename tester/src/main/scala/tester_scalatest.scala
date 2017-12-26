import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
//import scala.util.{ Failure, Success }

class SimpleWebServiceSpecs extends FlatSpec with ScalaFutures with Matchers {

  var system: ActorSystem = _

  override def withFixture(test: NoArgTest) = {
    system = ActorSystem()
    implicit val sys = system
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher
    try {
      super.withFixture(test) 
    }
    finally {
      system.terminate()
    }
  }


  "The webservice" should "respond on localhost:8080" in {
    val query = Http(system).singleRequest(HttpRequest(uri = "http://localhost:8080"))
    val result = Await.result(query, 10.seconds)
    result shouldBe a [HttpResponse]
  }

  it should "respond with status code 200 when /zorro is queried" in {
    val query = Http(system).singleRequest(HttpRequest(uri = "http://localhost:8080/zorro"))
    val result = Await.result(query, 10.seconds)
    result.status.intValue should equal(200)
  }

  it should "return content type application/json at /zorro" in {
    val query = Http(system).singleRequest(HttpRequest(uri = "http://localhost:8080/zorro"))
    val result = Await.result(query, 10.seconds)
    result.entity.contentType.toString() should equal("application/json")
  }

}

/*import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuite, Matchers}
 
class HelloWorldSpecWithScalaFutures extends FunSuite with Matchers with ScalaFutures {
 
  test("A valid message should be returned to a valid name") {
    whenReady(HelloWorld.sayHelloTo("Harry")) { result =>
      result shouldBe Some("Hello Harry, welcome to the future world!")
    }
  }
 
  test("No message should be returned to the one who cannot be named") {
    whenReady(HelloWorld.sayHelloTo("Voldemort")) { result =>
      result shouldBe None
    }
  }
 
}*/