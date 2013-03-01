package masg.util

import masg.dd.pomdp.agent.policy.FSCPolicy
import masg.problem.tag.simulator.TagProblemSimulator;
import spock.lang.Specification

class AlessandroFileFormatReaderSpec extends Specification {
	def "file is read"() {
		when:
			TagProblemSimulator simulator = new TagProblemSimulator();
			AlessandroFileFormatReader r = new AlessandroFileFormatReader()
			def nodes = r.readFile("data/FSC_new.txt")
			
			int maxNode = 0
			int maxScore = 0
			
			def nodeScores = []
			//nodes.eachWithIndex { n, ix ->
				FSCPolicy pol1 = new FSCPolicy(nodes[53])
				FSCPolicy pol2 = new FSCPolicy(nodes[53])
				
				int score = simulator.simulate(r.problem, pol1, pol2, 1000, 100);
				
				/*nodeScores << score
				if(score>maxScore) {
					maxNode = ix;
					maxScore = score;
				}*/
			//}
		then:
			println pol1
			//println pol1
			//println pol2
			/*println "max start node: ${maxNode}  has score ${maxScore}"
			
			nodeScores.eachWithIndex { val, ix ->
				println "$ix = $val"
			}*/
	}
}
