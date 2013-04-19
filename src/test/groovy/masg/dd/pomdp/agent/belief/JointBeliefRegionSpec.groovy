package masg.dd.pomdp.agent.belief

import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.problem.tag.TagProblemIPOMDP;
import spock.lang.Shared;
import spock.lang.Specification

class JointBeliefRegionSpec extends Specification {
	@Shared
	TagProblemIPOMDP problem = new TagProblemIPOMDP()
	
	def "joint belief region can be sampled"() {
		when:
			JointBelief b = new JointBelief(problem.getIPOMDP(), problem.otherAgents.get(0),problem.getIPOMDP().initialBelief, problem.otherAgents.get(0).initialBelief)
			JointBeliefRegion reg = new JointBeliefRegion()
			reg.sampleStartingWith(b, 500, 100, problem.getIPOMDP(), problem.otherAgents.get(0), new RandomPolicy(problem.getIPOMDP()),  new RandomPolicy(problem.otherAgents.get(0)))
		then:
			println "Done"
		
	}
}
