package masg.dd.pomdp.agent.belief

import masg.problem.tag.TagProblemIPOMDP;
import spock.lang.Shared;
import spock.lang.Specification

class JointBeliefSpec extends Specification {
	
	@Shared
	TagProblemIPOMDP problem = new TagProblemIPOMDP()
	
	def "joint beliefs compute"() {
		when:
			JointBelief b = new JointBelief(problem.getIPOMDP(), problem.otherAgents.get(0),problem.getIPOMDP().initialBelief, problem.otherAgents.get(0).initialBelief)
		then:
			b.nextBeliefFnsMe.each { actUs, map ->
				map.each { obsMe, beliefMe ->
					println "Action: $actUs"
					println "Observation: $obsMe:"
					println "$beliefMe" 	
				}
			}
			println b
		
	}
}
