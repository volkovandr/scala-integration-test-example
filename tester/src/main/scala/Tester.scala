import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import sys.process._
import java.io._
import scala.util.matching.Regex

object Tester {
	def main(args: Array[String]): Unit = {
		val config = ConfigFactory.load()
		val composeFile = config.getString("compose-file")
		println(s"Compose file is $composeFile")

		startComposition(composeFile) 
		try {
			makeSureContainersHealthy(composeFile, 10 seconds)
		} finally shutdownComposition(composeFile)
	}

	def startComposition(composeFile: String) = {
		print("Starting composition...")
		val up = runCommand(s"docker-compose -f $composeFile up -d".split(" "))._1
		if (up != 0) sys.error("Failed to bring up the Docker composition")
		println("started")
	}

	def shutdownComposition(composeFile: String) = {
		print("Stopping the composition...")
		val down = runCommand(s"docker-compose -f $composeFile down".split(" "))._1
		if (down != 0) sys.error("Failed to shutdown the Docker composition")
		println("stopped")
	}

	def makeSureContainersHealthy(composeFile: String, timeout: Duration) = {
		val dockerComposePsResult = runCommand(s"docker-compose -f $composeFile ps".split(" "))
		if(dockerComposePsResult._1 != 0) sys.error(s"`docker-compose ps` returned exit code ${dockerComposePsResult._1}")
		val containersInDockerCompose = dockerComposePsResult._2.split("\n").toList.drop(2).map(_.split(" ")(0))
		val parenthesisRegex = ".+(\\()(.+)(\\)).+".r
		
		val start = System.nanoTime()
		while ({
			val dockerPsResult = runCommand("docker ps".split(" "))
			if(dockerPsResult._1 != 0) sys.error(s"`docker ps` returned exit code ${dockerPsResult._1}")
			
			dockerPsResult._2.split("\n").toList.drop(1).
			filter(dockerPsLine => containersInDockerCompose.exists(dockerPsLine contains _)).
			map(_ match {case parenthesisRegex(_, health, _) => Some(health); case _ => None}).
			flatten.filter(_ != "healthy").isEmpty == false
		}) {
			if((System.nanoTime() - start).nanoseconds > timeout) sys.error("Timeout waiting for containers to get healthy")
			Thread.sleep(1000)
		}
	}

	def runCommand(cmd: Seq[String]): (Int, String, String) = {
  		val stdoutStream = new ByteArrayOutputStream
  		val stderrStream = new ByteArrayOutputStream
  		val stdoutWriter = new PrintWriter(stdoutStream)
  		val stderrWriter = new PrintWriter(stderrStream)
  		val exitValue = cmd.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
  		stdoutWriter.close()
  		stderrWriter.close()
  		(exitValue, stdoutStream.toString, stderrStream.toString)
	}
	
}