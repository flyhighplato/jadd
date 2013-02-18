package masg.dd.ipomdp

import masg.dd.variables.DDVariableSpace
import masg.problem.tag.TagProblemIPOMDP
import spock.lang.Shared;
import spock.lang.Specification

class IPOMDPSpec extends Specification {
	@Shared
	TagProblemIPOMDP problem = new TagProblemIPOMDP()
	
	def "POMDP initial belief is correct"() {
		when:
			DDVariableSpace currVarSpace = new DDVariableSpace(problem.p.getStates());
		then:
			println "Test"
		
	}
}
