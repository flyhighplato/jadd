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

class BeliefRegion {
	POMDP p
	ArrayList<POMDPBelief> beliefSamples = []
	
	public final List<POMDPBelief> getBeliefSamples() {
		return beliefSamples;
	}
	
	public BeliefRegion(int numSamples, int episodeLength, POMDP p, Policy policy, List<POMDPBelief> startingBeliefs = []) {
		this.p = p
		
		if(!startingBeliefs) {
			sampleStartingWith(new POMDPBelief(p, p.getInitialBelief()),numSamples,episodeLength, p, policy);
		}
		else {
			
			startingBeliefs.each{
				sampleStartingWith(it,(int)Math.ceil(numSamples/startingBeliefs.size()),episodeLength, p, policy);
			}
		}
	}
	
	public sampleStartingWith(POMDPBelief initBelief,int numSamples, int episodeLength, POMDP p, Policy policy){
			beliefSamples << initBelief

			POMDPBelief belief = initBelief
			
			int samplesTaken = 1;
			int episodeStep = 0;
			while(samplesTaken<numSamples) {
				
				
				if(beliefSamples.size()%10==0) {
					println "Sampled ${beliefSamples.size()}"
				}
				
				if(episodeStep>=episodeLength) {
					belief = initBelief
					episodeStep = 0;
				}
				
				HashMap<DDVariable,Integer> actPoint = policy.getAction(belief)
				//println "Policy gives action $actPoint"
				HashMap<DDVariable,Integer> obsPt = belief.sampleNextObservation(actPoint);
				//println "Sample observation $obsPt"
				
				
				belief = belief.getNextBelief(actPoint, obsPt)
				
				ProbDD beliefProbDD = belief.beliefFn.toProbabilityDD()
				boolean goodSample = true;
				for(int i=0;i<beliefSamples.size();i++) {
					POMDPBelief beliefOther = beliefSamples.get(i)
					
					AlgebraicDD absDiffDD = beliefOther.beliefFn.toProbabilityDD().getFunction().absDiff(beliefProbDD.getFunction())
					absDiffDD = absDiffDD.multiply(absDiffDD);
					
					double l2Dist = Math.sqrt(absDiffDD.getTotalWeight());
					
					if(l2Dist<0.001f) {
						println "L2 distance too small: $l2Dist"
						goodSample = false;
						break;
					}
					
					
					
				}
				
				if(goodSample) {
					samplesTaken++;
					beliefSamples << belief
				}
				
				episodeStep++;
			}
			
			println "Sampled ${beliefSamples.size()}"
	}
}
