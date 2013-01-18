package masg.problem.tag.simulator

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.alphavector.BeliefAlphaVector
import masg.dd.pomdp.agent.belief.Belief
import masg.dd.pomdp.agent.belief.BeliefRegion
import masg.dd.pomdp.agent.policy.AlphaVectorPolicy
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.AlphaVectorPolicyBuilder
import masg.dd.pomdp.agent.policy.QMDPPolicyBuilder
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyReader
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyWriter
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable
import masg.problem.tag.TagProblem;
import masg.problem.tag.TagProblemSimulator
import spock.lang.Shared;
import spock.lang.Specification
import spock.lang.Ignore
class TagProblemSimulatorSpec extends Specification {

	@Shared
	TagProblem problem = new TagProblem()
	
	int numSamples = 1000
	int numIterations = 100
	
	int numSteps = 100;
	int numTrials = 1000;
	
	String fileName = "${numSamples}_${numIterations}_${numSteps}.policy"
	
	TagProblemSimulator simulator = new TagProblemSimulator();
	
	@Ignore
	def "agents are simulated correctly from generated policy"() {
		when:
		
			fileName = "1000_100_100.policy"
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			AlphaVectorPolicyReader policyReader = new AlphaVectorPolicyReader(reader);
			AlphaVectorPolicy avPol = policyReader.read()
			reader.close()
		
			//Policy pol = new QMDPPolicyBuilder(problem.getPOMDP()).build()
			BeliefRegion belReg = new BeliefRegion(numSamples, numSteps, problem.getPOMDP(), avPol)
			
			avPol.alphaVectors.each{
				belReg.beliefSamples << new Belief(problem.getPOMDP(),new FactoredCondProbDD(it.witnessPt))
			}
			
			AlphaVectorPolicyBuilder polBuilder = new AlphaVectorPolicyBuilder(problem.getPOMDP())
			
			polBuilder.bestAlphas.addAll(avPol.alphaVectors)
			
			Policy pol = polBuilder.build(belReg, numIterations)
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName,false));
			AlphaVectorPolicyWriter policyWriter = new AlphaVectorPolicyWriter(pol);
			policyWriter.write(writer);
			writer.flush();
			writer.close();
		then:
			simulator.simulate(problem, pol, pol, numTrials, numSteps);
			
	}
	
	//@Ignore
	def "agents are simulated correctly from policy file"() {
		when:
			fileName = "1000_100_100.policy"
			
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			AlphaVectorPolicyReader policyReader = new AlphaVectorPolicyReader(reader);
			Policy pol = policyReader.read()
			reader.close()
			
		then:
			simulator.simulate(problem, pol, pol, 1, 1000);
			
	}
	
	class BeliefNode {
		FactoredCondProbDD belief;
		BeliefAlphaVector alpha;
		
		HashMap<HashMap<DDVariable,Integer>,BeliefNode> obsPlanNodes=[:];
		
		public String toString() {
			String str = "${hashCode()} ${alpha.hashCode()} \n"
			obsPlanNodes.each{HashMap<DDVariable,Integer> observation, BeliefNode nextNode ->
				str += "  $observation : ${nextNode.hashCode()} ${nextNode.alpha.hashCode()}\n"
			}
			
			return str;
		}
	}
	
}
