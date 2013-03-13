package masg.dd.pomdp

import masg.dd.variables.DDVariable
import masg.dd.variables.DDVariableSpace
import masg.problem.tag.TagProblemIPOMDP
import spock.lang.Shared;
import spock.lang.Specification

class IPOMDPSpec extends Specification {

	@Shared
	TagProblemIPOMDP problem = new TagProblemIPOMDP()
	
	def "POMDP initial belief is correct"() {
		when:
			DDVariableSpace currVarSpace = new DDVariableSpace(problem.getIPOMDP().getStates());
		then:
			println problem.getIPOMDP().getTransitionFunction()
		
	}
}
