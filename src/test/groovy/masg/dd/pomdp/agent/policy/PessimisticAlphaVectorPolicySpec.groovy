package masg.dd.pomdp.agent.policy

import masg.dd.pomdp.agent.belief.JointBelief
import masg.dd.pomdp.agent.belief.JointBeliefRegion
import masg.problem.tag.TagProblemIPOMDP;
import masg.problem.tag.TagProblemPOMDP;
import masg.problem.tag.simulator.TagProblemSimulator
import spock.lang.Shared;
import spock.lang.Specification

class PessimisticAlphaVectorPolicySpec extends Specification {
	@Shared
	TagProblemIPOMDP problem = new TagProblemIPOMDP()
	
	def "policy can be generated"() {
		when:
		
			TagProblemPOMDP problemPOMDP = new TagProblemPOMDP()
			TagProblemSimulator simulator = new TagProblemSimulator();
		
		
			JointBelief b = new JointBelief(problem.getIPOMDP(), problem.otherAgents.get(0),problem.getIPOMDP().initialBelief, problem.otherAgents.get(0).initialBelief)
			JointBeliefRegion reg = new JointBeliefRegion()
			reg.sampleStartingWith(b, 1000, 100, problem.getIPOMDP(), problem.otherAgents.get(0), new RandomPolicy(problem.getIPOMDP()),  new RandomPolicy(problem.otherAgents.get(0)))
			
			PessimisticAlphaVectorPolicyBuilder policyBuilder = new PessimisticAlphaVectorPolicyBuilder(problem.getIPOMDP(),problem.otherAgents.get(0))
			
			BeliefAlphaVectorPolicy pol = policyBuilder.build(reg,10);
			simulator.simulate(problemPOMDP, pol, pol, 100, 100);
			
		then:
			println "Done"
	}
}
