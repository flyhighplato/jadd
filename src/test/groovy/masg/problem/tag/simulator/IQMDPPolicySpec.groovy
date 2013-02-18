package masg.problem.tag.simulator

import masg.dd.pomdp.agent.policy.IQMDPPolicyBuilder
import masg.dd.pomdp.agent.policy.Policy
import masg.problem.tag.TagProblemIPOMDP
import masg.problem.tag.TagProblemPOMDP;
import masg.problem.tag.TagProblemSimulator;
import spock.lang.Shared;
import spock.lang.Specification

class IQMDPPolicySpec extends Specification {
	@Shared
	TagProblemIPOMDP problem = new TagProblemIPOMDP()
	
	def "QMDP policy is generated"() {
		when:
			IQMDPPolicyBuilder builder = new IQMDPPolicyBuilder(problem.p);
			Policy pol = builder.build()
			TagProblemSimulator simulator = new TagProblemSimulator();
		then:
			simulator.simulate(problem, pol, pol, 1000, 100);
			println "Done"
	}
}
