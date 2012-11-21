package masg.dd.pomdp.agent.belief

import java.util.List;

import masg.dd.CondProbDD;
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
	
	public BeliefRegion(int numSamples, POMDP p, Policy policy) {
		this.p = p
		
		beliefSamples << new Belief(p, p.getInitialBelief().toConditionalProbabilityFn())
		1.times {
			Belief belief = new Belief(p, p.getInitialBelief().toConditionalProbabilityFn())
			numSamples.times{
				
				if(beliefSamples.size()%10==0) {
					println "Sampled ${beliefSamples.size()}"
				}
				HashMap<DDVariable,Integer> actPoint = policy.getAction(belief)
				//println "Policy gives action $actPoint"
				HashMap<DDVariable,Integer> obsPt = belief.sampleNextObservation(actPoint);
				//println "Sample observation $obsPt"
				belief = belief.getNextBelief(actPoint, obsPt)
				
				beliefSamples << belief
			}
		}
	}
}
