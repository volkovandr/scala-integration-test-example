import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import com.typesafe.config.ConfigFactory

object WebServer {
  def main(args: Array[String]) {

    val config  = ConfigFactory.load()

    val host = config.getString("SimpleServer.host")
    val port = config.getInt("SimpleServer.port")

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      concat(
        path("zorro") {
          get {
            complete(HttpEntity(ContentTypes.`application/json`, """{"name": "zorro"}"""))
          }
        }
      )

    val bindingFuture = Http().bindAndHandle(route, host, port)

    def shutdown() = {
      println("Shutting down")
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    }

    val shutdownHook = sys.addShutdownHook(shutdown)
  
    if(args.length > 0)
    {
      println(s"Server online at http://$host:$port/\nPress ENTER to stop...")
      StdIn.readLine() // let it run until user presses return
      shutdownHook.remove()
      shutdown()
    }
    else
      println(s"Server online at http://$host:$port/\nPress Ctrl+C to stop...")
  }
}