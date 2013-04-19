package masg.dd.pomdp.agent.policy

import masg.dd.pomdp.agent.belief.JointBelief
import masg.dd.pomdp.agent.belief.JointBeliefRegion
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyReader
import masg.problem.tag.TagProblemIPOMDP;
import spock.lang.Shared;
import spock.lang.Specification

class BestResponseAlphaVectorPolicyBuilderSpec extends Specification {
	@Shared
	TagProblemIPOMDP problem = new TagProblemIPOMDP()
	
	def "policy can be generated"() {
		when:
		
			String fileName = "1000_100_100.policy"
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			AlphaVectorPolicyReader policyReader = new AlphaVectorPolicyReader(reader);
			BeliefAlphaVectorPolicy pol0 = policyReader.read(1)
			reader.close()
		
			JointBelief b = new JointBelief(problem.getIPOMDP(), problem.otherAgents.get(0),problem.getIPOMDP().initialBelief, problem.otherAgents.get(0).initialBelief)
			JointBeliefRegion reg = new JointBeliefRegion()
			reg.sampleStartingWith(b, 10, 100, problem.getIPOMDP(), problem.otherAgents.get(0), new RandomPolicy(problem.getIPOMDP()),  new RandomPolicy(problem.otherAgents.get(0)))
			
			BestResponseAlphaVectorPolicyBuilder policyBuilder = new BestResponseAlphaVectorPolicyBuilder(problem.getIPOMDP(),problem.otherAgents.get(0), pol0)
			
			policyBuilder.build(reg,10);
		then:
			println "Done"
		
	}
}
