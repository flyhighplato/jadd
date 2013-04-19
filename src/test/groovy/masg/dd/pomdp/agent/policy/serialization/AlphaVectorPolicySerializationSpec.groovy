package masg.dd.pomdp.agent.policy.serialization

import masg.dd.alphavector.BeliefAlphaVector
import masg.dd.pomdp.agent.policy.BeliefAlphaVectorPolicy
import masg.dd.variables.DDVariableSpace
import masg.problem.tag.TagProblemPOMDP;
import spock.lang.Shared;
import spock.lang.Specification

class AlphaVectorPolicySerializationSpec extends Specification {
	@Shared
	TagProblemPOMDP problem = new TagProblemPOMDP()
	@Shared
	BeliefAlphaVector alpha = new BeliefAlphaVector(new DDVariableSpace(problem.getPOMDP().getActions()).iterator().next(), problem.getPOMDP().getRewardFunction(),problem.getPOMDP().getInitialBelief().toProbabilityDD())
	
	
	def "alpha vector policy can be written"() {
		when:
			BeliefAlphaVectorPolicy policy = new BeliefAlphaVectorPolicy([alpha,alpha,alpha])
			AlphaVectorPolicyWriter writer = new AlphaVectorPolicyWriter(policy);
			
			BufferedWriter w = new BufferedWriter(new PrintWriter(System.out))
			
		then:
			writer.write(w);
			w.flush();
	}
	
	def "alpha vector policy can be read"() {
		when:
			BeliefAlphaVectorPolicy policy = new BeliefAlphaVectorPolicy([alpha,alpha,alpha])
			AlphaVectorPolicyWriter writer = new AlphaVectorPolicyWriter(policy);
			StringWriter sw = new StringWriter()
			BufferedWriter w = new BufferedWriter(sw)
			writer.write(w)
			w.flush();
			BufferedReader r = new BufferedReader(new StringReader(sw.toString()))
			
			AlphaVectorPolicyReader reader = new AlphaVectorPolicyReader(r);
		then:
			BeliefAlphaVectorPolicy readPolicy = reader.read();
			readPolicy.getAlphaVectors().size() == 3
			readPolicy.getAlphaVectors().each { BeliefAlphaVector alphaVector ->
				assert alphaVector.action.equals(new DDVariableSpace(problem.getPOMDP().getActions()).iterator().next())
				assert alphaVector.getValueFunction()
				assert alphaVector.getWitnessPoint()
			}
	}
	
}
