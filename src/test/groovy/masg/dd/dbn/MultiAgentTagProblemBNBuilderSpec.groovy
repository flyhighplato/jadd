package masg.dd.dbn

import masg.dd.CondProbDD
import spock.lang.Specification

class MultiAgentTagProblemBNBuilderSpec extends Specification {
	def "bayesian networks get built"() {
		when:
			MultiAgentTagProblemBNBuilder builder = new MultiAgentTagProblemBNBuilder(2)
		then:
			true
	}
}
