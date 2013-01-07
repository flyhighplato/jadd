package masg.dd.pomdp.agent.belief

import java.util.List;

import masg.dd.AlgebraicDD
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.RandomPolicy;
import masg.dd.variables.DDVariable;

class BeliefRegion {
	POMDP p
	ArrayList<Belief> beliefSamples = []
	
	public final List<Belief> getBeliefSamples() {
		return beliefSamples;
	}
	
	public BeliefRegion(int numSamples, int episodeLength, POMDP p, Policy policy) {
		this.p = p
		
		beliefSamples << new Belief(p, p.getInitialBelief())

			Belief belief = new Belief(p, p.getInitialBelief())
			
			int episodeStep = 0;
			while(beliefSamples.size()<numSamples) {
				
				
				if(beliefSamples.size()%10==0) {
					println "Sampled ${beliefSamples.size()}"
				}
				
				if(episodeStep>=episodeLength) {
					belief = new Belief(p, p.getInitialBelief())
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
					Belief beliefOther = beliefSamples.get(i)
					
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
					beliefSamples << belief
				}
				
				episodeStep++;
			}
			
			println "Sampled ${beliefSamples.size()}"
	}
}
