import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.testcontainers.DockerClientFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import org.testcontainers.containers.{GenericContainer => OTCGenericContainer}
import com.github.dockerjava.api.model.Link


class SimpleWebServiceSpecs extends FlatSpec with ScalaFutures with Matchers with BeforeAndAfterAll {

  type OTCContainer = OTCGenericContainer[T] forSome {type T <: OTCGenericContainer[T]}

  var system: ActorSystem = _
  var materializer: ActorMaterializer = _
  var container: OTCContainer = _
  var port: Int = _
  var dbContainer: OTCContainer = _
  var dbPort: Int = _

  override def beforeAll() {
    system = ActorSystem()
    implicit val sys = system
    materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    
    container = new OTCGenericContainer("scala-integrationtest-example-app:0.1")
    container.withExposedPorts(8080)
    container.withCreateContainerCmdModifier(cmd => cmd.withHostName("app"))
    dbContainer = new OTCGenericContainer("postgres:10").withExposedPorts(5432)
    dbContainer.withCreateContainerCmdModifier(cmd => cmd.withHostName("database"))
    
    dbContainer.start()
    dbPort = dbContainer.getMappedPort(5432)
    val dbContainerName = dbContainer.getContainerName()
    container.withCreateContainerCmdModifier(cmd => cmd.withLinks(new Link(dbContainerName, "database")))
    container.start()
    port = container.getMappedPort(8080)
  }

  override def afterAll() {
    container.stop()
    dbContainer.stop()
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
