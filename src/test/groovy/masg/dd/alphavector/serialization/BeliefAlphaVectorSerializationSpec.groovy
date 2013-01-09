package masg.dd.alphavector.serialization

import masg.dd.alphavector.BeliefAlphaVector
import masg.dd.variables.DDVariableSpace
import masg.problem.tag.TagProblem
import spock.lang.Shared;
import spock.lang.Specification

class BeliefAlphaVectorSerializationSpec extends Specification {
	@Shared
	TagProblem problem = new TagProblem()
	
	def "alpha vector can be written"() {
		when:
			BeliefAlphaVectorWriter writer = new BeliefAlphaVectorWriter(new BeliefAlphaVector(new DDVariableSpace(problem.getPOMDP().getActions()).iterator().next(), problem.getPOMDP().getRewardFunction(),problem.getPOMDP().getInitialBelief().toProbabilityDD()))
			BufferedWriter w = new BufferedWriter(new PrintWriter(System.out))
			
		then:
			writer.write(w);
			w.flush();
	}
	
	def "alpha vector can be read"() {
		when:
			BeliefAlphaVectorWriter writer = new BeliefAlphaVectorWriter(new BeliefAlphaVector(new DDVariableSpace(problem.getPOMDP().getActions()).iterator().next(), problem.getPOMDP().getRewardFunction(),problem.getPOMDP().getInitialBelief().toProbabilityDD()))
			StringWriter sw = new StringWriter()
			BufferedWriter w = new BufferedWriter(sw)
			writer.write(w)
			w.flush();
			BufferedReader r = new BufferedReader(new StringReader(sw.toString()))
			BeliefAlphaVectorReader reader = new BeliefAlphaVectorReader(r);
		then:
			BeliefAlphaVector readAlphaVector = reader.read();
			println readAlphaVector.getAction()
			println readAlphaVector.getValueFunction()
			println readAlphaVector.getWitnessPoint()
	}
}
