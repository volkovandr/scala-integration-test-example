import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import sys.process._
import scala.util.matching.Regex

object Tester {
	def main(args: Array[String]): Unit = {
		val config = ConfigFactory.load()
		val composeFile = config.getString("compose-file")
		println(s"Compose file is $composeFile")

		startComposition(composeFile)
		makeSureContainersHealthy(composeFile, 10 seconds)
		shutdownComposition(composeFile)
	}

	def startComposition(composeFile: String) = {
		val up = (s"docker-compose -f $composeFile up -d" !)
		if (up != 0) sys.error("Failed to bring up the Docker composition")
	}

	def shutdownComposition(composeFile: String) = {
		val down = (s"docker-compose -f $composeFile down" !)
		if (down != 0) sys.error("Failed to shutdown the Docker composition")
	}

	def makeSureContainersHealthy(composeFile: String, timeout: Duration) = {
		val listCompose = (s"docker-compose -f $composeFile ps" !!)
		val containers = listCompose.split("\n").toList.drop(2).map(_.split(" ")(0))
		val parenthesis = ".+(\\()(.+)(\\)).+".r
		
		val start = System.nanoTime()
		while (("docker ps" !!).split("\n").toList.drop(1).filter(str => containers.exists(str contains _)).map(_ match {
			case parenthesis(before, health, after) => Some(health)
			case _ => None
		}).flatten.filter(_ != "healthy").isEmpty == false) {
			if((System.nanoTime() - start).nanoseconds > timeout) sys.error("Timeout waiting for containers to get healthy")
			Thread.sleep(1000)
		}
	}

}