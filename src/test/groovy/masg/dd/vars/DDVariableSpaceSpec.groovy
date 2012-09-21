package masg.dd.vars

import masg.dd.rules.DecisionRule;
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
	
	def "can return correct rule"() {
		when:
			Map varVals = [:] 
			varVals[new DDVariable("row",5)] = 0
			varVals[new DDVariable("act",4)] = 1
			varVals[new DDVariable("col",4)] = 3
			
			DDVariableSpace space = new DDVariableSpace()
			
			varVals.keySet().each{
				space.addVariable(it)
			}
			
		then:
			DecisionRule r = space.generateRule(varVals,1);
			assert r.toString() == "000100110:1.0"
	}
}
