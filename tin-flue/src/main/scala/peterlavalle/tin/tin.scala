package peterlavalle

import scala.io.BufferedSource

package object tin
	extends peterlavalle.TPackage {

	implicit class WrappedBufferedSource(bufferedSource: BufferedSource) {
		def MkString: String = {
			val string = bufferedSource.mkString
			bufferedSource.close()
			string
		}
	}

}
