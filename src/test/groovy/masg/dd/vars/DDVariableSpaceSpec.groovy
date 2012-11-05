package masg.dd.vars

import spock.lang.Specification

class DDVariableSpaceSpec extends Specification {
	def "can iterate over all variables in var space"() {
		when:
			DDVariableSpace space = new DDVariableSpace()
			space.addVariable(new DDVariable("row",5))
			space.addVariable(new DDVariable("act",4))
			space.addVariable(new DDVariable("col",5))
		then:
			List points = []
			space.each{ point ->
				points << point
			}
			assert points.size() == 100
	}
}
