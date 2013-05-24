package masg.dd.dbn

import masg.dd.AlgebraicDD
import masg.dd.pomdp.POMDP
import masg.dd.variables.DDVariable
import masg.problem.tag.TagProblemPOMDP;
import spock.lang.Shared;
import spock.lang.Specification

class POMDPBayesianNetworkStepSpec extends Specification {
	
	@Shared
	TagProblemPOMDP problem = new TagProblemPOMDP()
	
	def "pomdp bn step can be constructed"() {
		when:
			POMDPBayesianNetworkStep step = new POMDPBayesianNetworkStep(problem.getPOMDP())
		then:
			println step.stationaryNetwork
	}
	
	def "pomdp bn step can create action distribution"() {
		when:
			POMDP p = problem.getPOMDP()
			POMDPBayesianNetworkStep step = new POMDPBayesianNetworkStep(p)
			
			HashMap postState = new HashMap()
			p.statesPrime.each { DDVariable var ->
				postState[var] = 0
			}
			
			AlgebraicDD postStateDist = new AlgebraicDD(p.statesPrime,0,postState)
			println postState
			println postStateDist
			3.times {
				step = step.revise([postStateDist])
				
			}
			
		then:
			//println step.sampleBelief()
			//println step.obsDist
			//println step.sampleObservation()
			/*step.getActDistChain().eachWithIndex { l, ix ->
				println "-- $ix"
				println l
				println()
			}*/
			100.times {
				println step.sampleBelief()
			}
	}
	
}
