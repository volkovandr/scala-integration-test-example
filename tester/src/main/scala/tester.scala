object Tester {
	def main(args: Array[String]): Unit = {
		println("The tester is not supposed to be run directly.")
		println("You should comile the application, assemble a fatJar which will include the Runner for scalatest")
		println("and then execute the tests from command line by invoking")
		println()
		println("scala -classpath <fatjar_name.jar> org.scalatest.tools.Runner -R classes -o")
		println()
		println("Note, that you are supposed to have the proper scala version be installed")
		println("Nevertherless we will execute the tests now...")

		(new SimpleWebServiceSpecs).execute()		
	}
}