package masg.dd.pomdp.agent.belief;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.ClosureBuilder;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.ipomdp.IPOMDP;
import masg.dd.variables.DDVariable;

public class IPOMDPBelief implements Belief {
	HashMap<IPOMDPAgentModel, Double> otherBeliefPolicyProbabilities;
	FactoredCondProbDD beliefFn;
	
	FactoredCondProbDD nextActionOtherDistributionFn;
	
	HashMap<IPOMDPAgentModel, HashMap<DDVariable,Integer>> modelNextAction = new HashMap<IPOMDPAgentModel, HashMap<DDVariable,Integer>>();
	
	IPOMDP p;
	
	public IPOMDPBelief(IPOMDP p, FactoredCondProbDD fn, HashMap< IPOMDPAgentModel , Double > otherBeliefPolicyProbabilities) {
		this.p = p;
		this.beliefFn = fn;
		this.otherBeliefPolicyProbabilities = otherBeliefPolicyProbabilities;
		
		HashMap< HashMap<DDVariable,Integer>, Double> nextActionOtherDistribution = new HashMap< HashMap<DDVariable,Integer>, Double>();
		
		for(Entry<IPOMDPAgentModel, Double> e1:otherBeliefPolicyProbabilities.entrySet()) {
			double prob = e1.getValue();
			
			IPOMDPAgentModel model = e1.getKey();
				
			HashMap<DDVariable,Integer> action = model.policy.getAction(model.currentBelief);
			
			modelNextAction.put(model, action);
			
			if(!nextActionOtherDistribution.containsKey(action)) {
				nextActionOtherDistribution.put(action, prob);
			}
			else {
				nextActionOtherDistribution.put(action, nextActionOtherDistribution.get(action) + prob);
			}
		}
		
		Closure<Double> c = ClosureBuilder.buildClosure(nextActionOtherDistribution);
		nextActionOtherDistributionFn = new FactoredCondProbDD(new ProbDD(p.getActionsOther(),c));
		
		for(HashMap<DDVariable,Integer> actSpacePt1:p.getActionSpace()) {
			
			//immRewardFns.put(actSpacePt, beliefFn.multiply(p.getRewardFunction(actSpacePt)) );
			
			FactoredCondProbDD tempRestrTransFn = p.getTransitionFunction().restrict(actSpacePt1).multiply(beliefFn);
			tempRestrTransFn = tempRestrTransFn.sumOut(p.getStates());
			tempRestrTransFn = tempRestrTransFn.normalize();
			
			FactoredCondProbDD obsProbFn = p.getObservationFunction().restrict(actSpacePt1).multiply(tempRestrTransFn);
			obsProbFn = obsProbFn.sumOut(p.getStatesPrime());
			obsProbFn = obsProbFn.normalize();
			
			//obsProbFns.put(actSpacePt, obsProbFn);
			
			//nextBeliefFns.put(actSpacePt, new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>());
			//obsProbs.put(actSpacePt, new HashMap<HashMap<DDVariable,Integer>, Double>());
				
			for(HashMap<DDVariable,Integer> obsSpacePt:p.getObservationSpace()) {
				//for(HashMap<DDVariable,Integer> actSpacePt2:p.getActionSpace()) {
					
					FactoredCondProbDD actionOtherProbFn = obsProbFn.restrict(obsSpacePt);
				
					double obsProb = obsProbFn.getValue(obsSpacePt);
					
					//obsProbs.get(actSpacePt).put(obsSpacePt, obsProb);
					
					if(obsProb>0.0f) {
						
						FactoredCondProbDD tempRestrObsFn =  p.getObservationFunction().restrict(actSpacePt1);
						tempRestrObsFn = tempRestrObsFn.restrict(obsSpacePt);
						
						FactoredCondProbDD nextBelief =  p.getTransitionFunction().restrict(actSpacePt1);
						nextBelief = nextBelief.multiply(beliefFn);
						nextBelief = nextBelief.multiply(tempRestrObsFn);
						
						nextBelief = nextBelief.normalize();
						nextBelief = nextBelief.unprime();
						
						
						//nextBeliefFns.get(actSpacePt).put(obsSpacePt, nextBelief);
					}
				//}
			}
		}
	}
	
	@Override
	public FactoredCondProbDD getBeliefFunction() {
		return beliefFn;
	}

	public Belief getNextBelief(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return null;
	}
	
	public HashMap<HashMap<DDVariable,Integer>, Double> getObservationProbabilities(HashMap<DDVariable,Integer> actSpacePt) {
		return null;
	}
}
