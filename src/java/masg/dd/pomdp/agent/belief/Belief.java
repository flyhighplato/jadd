package masg.dd.pomdp.agent.belief;

import java.util.HashMap;
import java.util.List;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;

public class Belief {
	final POMDP p;
	final CondProbDD beliefFn;
	
	HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> immRewardFns = new HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>();
	HashMap<HashMap<DDVariable,Integer>, CondProbDD> obsProbFns = new HashMap<HashMap<DDVariable,Integer>, CondProbDD>();
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, CondProbDD> > nextBeliefFns = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, CondProbDD> >();
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbs = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	
	public Belief(POMDP p, CondProbDD fn) {
		this.p = p;
		this.beliefFn = fn;
		
		for(HashMap<DDVariable,Integer> actSpacePt:p.getActionSpace()) {
			immRewardFns.put(actSpacePt, p.getRewardFunction(actSpacePt).multiply(beliefFn) );
			
			CondProbDD tempRestrTransFn = p.getTransitionFunction(actSpacePt).multiply(beliefFn);
			tempRestrTransFn = tempRestrTransFn.sumOut(p.getStates());
			
			CondProbDD obsProbFn = p.getObservationFunction(actSpacePt).multiply(tempRestrTransFn);
			obsProbFn = obsProbFn.sumOut(p.getStatesPrime());
			obsProbFn = obsProbFn.normalize();
			
			obsProbFns.put(actSpacePt, obsProbFn);
			
			nextBeliefFns.put(actSpacePt, new HashMap<HashMap<DDVariable,Integer>, CondProbDD>());
			obsProbs.put(actSpacePt, new HashMap<HashMap<DDVariable,Integer>, Double>());
			
			for(HashMap<DDVariable,Integer> obsSpacePt:p.getObservationSpace()) {
				double obsProb = obsProbFn.getValue(obsSpacePt);
				
				obsProbs.get(actSpacePt).put(obsSpacePt, obsProb);
				
				if(obsProb>0.0f) {
					
					CondProbDD tempRestrObsFn =  p.getObservationFunction(actSpacePt, obsSpacePt);
					CondProbDD nextBelief = tempRestrObsFn.multiply(tempRestrTransFn);
					nextBelief = nextBelief.normalize();
					nextBelief = nextBelief.unprime();
					
					
					nextBeliefFns.get(actSpacePt).put(obsSpacePt, nextBelief);
				}
			}
		}
	}
	
	public BeliefAlphaVector pickBestAlpha(List<BeliefAlphaVector> alphas) {
		double bestVal = -Double.MAX_VALUE;
		BeliefAlphaVector bestAlpha = null;
		
		for(BeliefAlphaVector alpha:alphas) {
			AlgebraicDD valFn = alpha.getValueFunction().multiply(beliefFn);
			double tempVal = valFn.getTotalWeight();
			
			if(tempVal>=bestVal) {
				bestVal = tempVal;
				bestAlpha = alpha;
			}
		}
		return bestAlpha;
	}
	
	public CondProbDD getBeliefFunction() {
		return beliefFn;
	}
	
	public CondProbDD getNextBeliefFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return nextBeliefFns.get(actSpacePt).get(obsSpacePt);
	}
	
	public HashMap<HashMap<DDVariable,Integer>, Double> getObservationProbabilities(HashMap<DDVariable,Integer> actSpacePt) {
		return obsProbs.get(actSpacePt);
	}
	
	public AlgebraicDD getImmediateValueFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return immRewardFns.get(actSpacePt);
	}
	
	public CondProbDD getObservationProbabilityFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return obsProbFns.get(actSpacePt);
	}
}
