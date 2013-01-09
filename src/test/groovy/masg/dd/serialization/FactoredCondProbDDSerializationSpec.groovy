package masg.dd.serialization

import masg.problem.tag.TagProblem;
import spock.lang.Shared;
import spock.lang.Specification

class FactoredCondProbDDSerializationSpec extends Specification {
	@Shared
	TagProblem problem = new TagProblem()
	
	def "factored conditional probability dd can be written"() {
		when:
			FactoredCondProbDDWriter writer = new FactoredCondProbDDWriter(problem.getPOMDP().getTransitionFunction())
			BufferedWriter w = new BufferedWriter(new PrintWriter(System.out))
			
		then:
			writer.write(w);
			w.flush();
	}
	
	def "factored conditonal probability dd can be read"() {
		when:
			FactoredCondProbDDWriter writer = new FactoredCondProbDDWriter(problem.getPOMDP().getTransitionFunction())
			StringWriter sw = new StringWriter()
			BufferedWriter w = new BufferedWriter(sw)
			writer.write(w)
			w.flush();
			BufferedReader r = new BufferedReader(new StringReader(sw.toString()))
			FactoredCondProbDDReader reader = new FactoredCondProbDDReader(r);
		then:
			println reader.read();
	}
}
