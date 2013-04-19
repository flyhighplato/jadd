package masg.dd.pomdp.agent.belief

import masg.dd.CondProbDD
import masg.dd.FactoredCondProbDD
import masg.dd.pomdp.AbstractPOMDP
import masg.dd.pomdp.IPOMDP
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.problem.tag.TagProblemIPOMDP;
import spock.lang.Specification

class BeliefExpectationSamplerSpec extends Specification {
	def "experimenting with belief expectation"() {
		when:
			TagProblemIPOMDP problem = new TagProblemIPOMDP()
			AbstractPOMDP pMe = problem.getIPOMDP();
			AbstractPOMDP pOther = problem.otherAgents.get(0);
			
			RandomPolicy polOther = new RandomPolicy(pOther);
			FactoredCondProbDD p_sj_si = new FactoredCondProbDD(new CondProbDD(pMe.getStates(),pOther.getStates(),{1.0d}).normalize()).multiply(pOther.initialBelief)
			
		then:
			println p_sj_si
		
		
		//FactoredCondProbDD prob_si = pMe.initialBelief;
		//JointBelief b = new JointBelief(pMe, pOther, pMe.initialBelief, pOther.initialBelief)
	}
}
