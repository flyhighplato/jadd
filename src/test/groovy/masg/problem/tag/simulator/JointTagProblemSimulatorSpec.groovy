package masg.problem.tag.simulator

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.alphavector.BeliefAlphaVector
import masg.dd.pomdp.agent.belief.POMDPBelief
import masg.dd.pomdp.agent.belief.BeliefRegion
import masg.dd.pomdp.agent.policy.BeliefAlphaVectorJointPolicyBuilder
import masg.dd.pomdp.agent.policy.BeliefAlphaVectorPolicy
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.BeliefAlphaVectorPolicyBuilder
import masg.dd.pomdp.agent.policy.QMDPPolicyBuilder
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyReader
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyWriter
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable
import masg.problem.tag.TagProblemJointPOMDPAgent1
import masg.problem.tag.TagProblemJointPOMDPAgent2
import masg.problem.tag.TagProblemPOMDP;
import spock.lang.Shared;
import spock.lang.Specification
import spock.lang.Ignore
class JointTagProblemSimulatorSpec extends Specification {

	@Shared
	TagProblemJointPOMDPAgent1 problemAgent1 = new TagProblemJointPOMDPAgent1()
	TagProblemJointPOMDPAgent2 problemAgent2 = new TagProblemJointPOMDPAgent2()
	
	int numSamples = 100
	int numIterations = 100
	
	int numSteps = 100;
	int numTrials = 100;
	
	String fileName = "joint_${numSamples}_${numIterations}_${numSteps}.policy"
	
	TagProblemSimulator simulator = new TagProblemSimulator();
	
	def "agents are simulated correctly from generated policy"() {
		when:
		
			fileName = "1000_100_100.policy"
			
			Policy policyMe = new RandomPolicy(problemAgent1.getPOMDP())
			Policy policyOther = new RandomPolicy(problemAgent2.getPOMDP())
			
			2.times {
				BeliefAlphaVectorJointPolicyBuilder polBuilder = new BeliefAlphaVectorJointPolicyBuilder(problemAgent1.getPOMDP(), problemAgent2.getPOMDP(), policyOther)
				policyMe = polBuilder.build(100);
				
				polBuilder = new BeliefAlphaVectorJointPolicyBuilder(problemAgent2.getPOMDP(), problemAgent1.getPOMDP(), policyMe)
				policyOther = polBuilder.build(100);
			}
			
			/*Policy pol = polBuilder.build(problemAgent1.getPOMDP(), numIterations)
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName,false));
			AlphaVectorPolicyWriter policyWriter = new AlphaVectorPolicyWriter(pol);
			policyWriter.write(writer);
			writer.flush();
			writer.close();*/
			
		then:
			simulator.simulate(problemAgent1, problemAgent2, policyMe, policyOther, numTrials, numSteps);
			true
			
	}
	
	
}
