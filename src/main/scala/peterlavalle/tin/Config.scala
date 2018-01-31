package peterlavalle.tin

import scala.beans.BeanProperty

class Config {
	@BeanProperty
	var iterations: Int = 34

	@BeanProperty
	var blockSplittingMax: Int = 14

	@BeanProperty
	var grouping: Int = 14
}
