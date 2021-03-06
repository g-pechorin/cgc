package peterlavalle.tin

object ByeCoder {
	def apply(stream: Stream[Byte]): Stream[String] =
		stream.map(ByeCoder.apply)

	def apply(byte: Byte): String =
		s"((uint8_t)(0x${"%02X".format(byte)}))"
}
