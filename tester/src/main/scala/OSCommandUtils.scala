import sys.process._
import java.io._


trait OSCommandUtils {
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