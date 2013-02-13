package masg.dd.representation

import masg.dd.pomdp.agent.belief.BeliefRegion
import masg.dd.pomdp.agent.belief.POMDPBelief
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.problem.tag.TagProblemPOMDP;
import spock.lang.Specification


class DDCollectionSpec extends Specification {
	def "collection can be built"() {
		when:
			int numSamples = 500;
			int numSteps = 100;
			TagProblemPOMDP problem = new TagProblemPOMDP()
			Policy pol = new RandomPolicy(problem.getPOMDP())
			BeliefRegion belReg = new BeliefRegion(numSamples, numSteps, problem.getPOMDP(), pol)
			
			DDCollection ddColl = null;
			belReg.beliefSamples.each{ POMDPBelief b ->
				if(ddColl==null) {
					ddColl = new DDCollection(b.beliefFn.toProbabilityDD().function.ruleCollection);
				}
				else {
					ddColl.merge(b.beliefFn.toProbabilityDD().function.ruleCollection)
				}
				
				println "# Nodes: ${ddColl.getNodes().size()}"
				println "# Leaves: ${ddColl.getLeaves().size()}"
				println "# Roots: ${ddColl.getRoots().size()}"
				println()
			}
			belReg = null;
		then:
			//println ddColl.toString()
			println "Done"
	}
}
