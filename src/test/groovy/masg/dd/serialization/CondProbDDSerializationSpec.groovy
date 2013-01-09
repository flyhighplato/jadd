package masg.dd.serialization

import masg.problem.tag.TagProblem
import spock.lang.Specification
import spock.lang.Shared;

class CondProbDDSerializationSpec extends Specification {
	@Shared
	TagProblem problem = new TagProblem()
	
	def "conditional probability dd can be written"() {
		when:
			CondProbDDWriter writer = new CondProbDDWriter(problem.getPOMDP().getInitialBelief().indepFns[0])
			BufferedWriter w = new BufferedWriter(new PrintWriter(System.out))
			
		then:
			writer.write(w);
			w.flush();
	}
	
	def "conditonal probability dd can be read"() {
		when:
			CondProbDDWriter writer = new CondProbDDWriter(problem.getPOMDP().getInitialBelief().indepFns[0])
			StringWriter sw = new StringWriter()
			BufferedWriter w = new BufferedWriter(sw)
			writer.write(w)
			w.flush();
			BufferedReader r = new BufferedReader(new StringReader(sw.toString()))
			CondProbDDReader reader = new CondProbDDReader(r);
		then:
			println reader.read();
	}
}
