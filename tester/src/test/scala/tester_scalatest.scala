import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.testcontainers.DockerClientFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
//import scala.util.{ Failure, Success }

import org.testcontainers.containers.{GenericContainer => OTCGenericContainer}



class SimpleWebServiceSpecs extends FlatSpec with ScalaFutures with Matchers with BeforeAndAfterAll {

  type OTCContainer = OTCGenericContainer[T] forSome {type T <: OTCGenericContainer[T]}

  var system: ActorSystem = _
  var materializer: ActorMaterializer = _
  var container: OTCContainer = _
  var port: Int = _

  override def beforeAll() {
    system = ActorSystem()
    implicit val sys = system
    materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    
    container = new OTCGenericContainer("scala-integrationtest-example-app:0.1").withExposedPorts(8080)
    
    container.start()
    port = container.getMappedPort(8080)
  }

  override def afterAll() {
    container.stop()
    DockerClientFactory.instance().client().close()
    system.terminate()
  }

  "The webservice" should "respond on localhost:8080" in {
    val query: Future[HttpResponse] = Http(system).singleRequest(HttpRequest(uri = s"http://localhost:$port"))
    val result = Await.result(query, 10.seconds)
    result shouldBe a [HttpResponse]
    Await.result(result.discardEntityBytes(materializer).future, 10.seconds)
  }

  it should "respond with status code 200 when /zorro is queried" in {
    val query: Future[HttpResponse] = Http(system).singleRequest(HttpRequest(uri = s"http://localhost:$port/zorro"))
    val result = Await.result(query, 10.seconds)
    result.status.intValue should equal(200)
    Await.result(result.discardEntityBytes(materializer).future, 10.seconds)
  }

  it should "return content type application/json at /zorro" in {
    val query: Future[HttpResponse] = Http(system).singleRequest(HttpRequest(uri = s"http://localhost:$port/zorro"))
    val result = Await.result(query, 10.seconds)
    result.entity.contentType.toString() should equal("application/json")
    Await.result(result.discardEntityBytes(materializer).future, 10.seconds)
  }

  it should "recover after system crash" in {
    container.stop()
    container.start()
    port = container.getMappedPort(8080)
    val query: Future[HttpResponse] = Http(system).singleRequest(HttpRequest(uri = s"http://localhost:$port"))
    val result = Await.result(query, 10.seconds)
    result shouldBe a [HttpResponse]
    Await.result(result.discardEntityBytes(materializer).future, 10.seconds)
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
