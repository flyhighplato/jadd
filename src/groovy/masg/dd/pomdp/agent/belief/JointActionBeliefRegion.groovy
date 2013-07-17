package masg.dd.pomdp.agent.belief

import java.util.List;

import masg.dd.AlgebraicDD
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.belief.POMDPBelief;
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.RandomPolicy;
import masg.dd.variables.DDVariable;

class JointActionBeliefRegion {
	POMDP pMe, pOther
	ArrayList<JointActionPOMDPBelief> beliefSamples = []
	
	public final List<JointActionPOMDPBelief> getBeliefSamples() {
		return beliefSamples;
	}
	
	public JointActionBeliefRegion(int numSamples, int episodeLength, POMDP pMe, POMDP pOther, Policy policyMe, Policy policyOther, List<POMDPBelief> startingBeliefs = []) {
		this.pMe = pMe
		this.pOther = pOther
		
		if(!startingBeliefs) {
			sampleStartingWith(new JointActionPOMDPBelief(pMe, pOther, pMe.getInitialBelief()), numSamples, episodeLength, pMe, policyMe, policyOther);
		}
		else {
			
			startingBeliefs.each{
				sampleStartingWith(it,(int)Math.ceil(numSamples/startingBeliefs.size()),episodeLength, pMe, policyMe, policyOther);
			}
		}
	}
	
	public sampleStartingWith(JointActionPOMDPBelief initBelief,int numSamples, int episodeLength, POMDP p, Policy policyMe, Policy policyOther){
			beliefSamples << initBelief

			JointActionPOMDPBelief initBeliefOther = new JointActionPOMDPBelief(pOther, pMe, pOther.getInitialBelief())
			JointActionPOMDPBelief beliefMe = initBelief
			JointActionPOMDPBelief beliefOther = initBeliefOther
			
			int samplesTaken = 1;
			int episodeStep = 0;
			while(samplesTaken<numSamples) {
				
				
				if(beliefSamples.size()%10==0) {
					println "Sampled ${beliefSamples.size()}"
				}
				
				if(episodeStep>=episodeLength) {
					beliefMe = initBelief
					beliefOther = initBeliefOther
					episodeStep = 0;
				}
				
				HashMap<DDVariable,Integer> actPointMe = policyMe.getAction(beliefMe)
				HashMap<DDVariable,Integer> actPointOther = policyOther.getAction(beliefOther)
				HashMap<DDVariable,Integer> actJoint = actPointMe + actPointOther
				
				
				HashMap<DDVariable,Integer> obsPtMe = beliefMe.sampleNextObservation(actJoint);
				HashMap<DDVariable,Integer> obsPtOther = beliefOther.sampleNextObservation(actJoint);
				
				
				beliefMe = beliefMe.getNextBelief(actJoint, obsPtMe)
				beliefOther = beliefOther.getNextBelief(actJoint, obsPtOther)
				
				
				samplesTaken++;
				beliefSamples << beliefMe
				
				
				episodeStep++;
			}
			
			println "Sampled ${beliefSamples.size()}"
	}
}
