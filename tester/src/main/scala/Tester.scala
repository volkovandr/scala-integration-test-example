import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

object Tester extends ContainerOperations {
	def main(args: Array[String]): Unit = {
		val config = ConfigFactory.load()
		val composeFile = config.getString("compose-file")
		println(s"Compose file is $composeFile")

		startComposition(composeFile) 
		try {
			makeSureContainersHealthy(composeFile, 10 seconds)
		} finally shutdownComposition(composeFile)
	}

}