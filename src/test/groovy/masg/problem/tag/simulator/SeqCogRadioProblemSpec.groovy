package masg.problem.tag.simulator

import masg.dd.pomdp.POMDP
import masg.dd.pomdp.agent.belief.BeliefRegion
import masg.dd.pomdp.agent.belief.POMDPBelief
import masg.dd.pomdp.agent.policy.AlphaVectorPolicy
import masg.dd.pomdp.agent.policy.AlphaVectorPolicyBuilder
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.QMDPPolicyBuilder
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.problem.seqcog.SeqCogRadioProblemPOMDP
import masg.problem.seqcog.simulator.SeqCogRadioSimulator
import spock.lang.Specification

class SeqCogRadioProblemSpec extends Specification {
	int numSamples = 50
	int numTrials = 100
	int numSteps = 1000
	
	def "POMDP can be instantiated"() {
		when:
			SeqCogRadioProblemPOMDP scrProb = new SeqCogRadioProblemPOMDP()
			POMDP p = scrProb.getPOMDP()
			POMDPBelief initBelief = new POMDPBelief(p, p.getInitialBelief())
			
			Policy pol
			pol = new RandomPolicy(p)
			//pol = new QMDPPolicyBuilder(p).build()
			
			BeliefRegion belReg = new BeliefRegion(numSamples, numSteps, p, pol, [initBelief])
			AlphaVectorPolicyBuilder polBuilder = new AlphaVectorPolicyBuilder(p)
			pol = polBuilder.build(belReg, 100)
			
			SeqCogRadioSimulator simulator = new SeqCogRadioSimulator()
			
			simulator.simulate(scrProb,pol,numTrials,numSteps)
			
		then:
			println "Done"
	}
}
