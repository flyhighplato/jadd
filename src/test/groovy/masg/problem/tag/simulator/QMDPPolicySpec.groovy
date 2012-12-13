package masg.problem.tag.simulator

import masg.dd.pomdp.agent.policy.QMDPPolicy
import masg.dd.pomdp.agent.policy.QMDPPolicyBuilder
import masg.problem.tag.TagProblem;
import spock.lang.Shared;
import spock.lang.Specification

class QMDPPolicySpec extends Specification {
	@Shared
	TagProblem problem = new TagProblem()
	
	def "QMDP policy is generated"() {
		when:
			QMDPPolicyBuilder builder = new QMDPPolicyBuilder(problem.getPOMDP());
			QMDPPolicy policy = builder.build()
		then:
			println policy
			println "Done"
	}
}
