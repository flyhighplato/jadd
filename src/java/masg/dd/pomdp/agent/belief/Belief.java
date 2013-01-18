package masg.dd.pomdp.agent.belief;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;

public class Belief {
	final POMDP p;
	final FactoredCondProbDD beliefFn;
	
	HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> immRewardFns = new HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>();
	HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> obsProbFns = new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>();
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFns = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> >();
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbs = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	
	Random random = new Random();
	
	public Belief(POMDP p, FactoredCondProbDD fn) {
		this.p = p;
		this.beliefFn = fn;
		
		
		for(HashMap<DDVariable,Integer> actSpacePt:p.getActionSpace()) {
			immRewardFns.put(actSpacePt, beliefFn.multiply(p.getRewardFunction(actSpacePt)) );
			
			FactoredCondProbDD tempRestrTransFn = p.getTransitionFunction(actSpacePt).multiply(beliefFn);
			tempRestrTransFn = tempRestrTransFn.sumOut(p.getStates());
			tempRestrTransFn = tempRestrTransFn.normalize();
			
			FactoredCondProbDD obsProbFn = p.getObservationFunction(actSpacePt).multiply(tempRestrTransFn);
			obsProbFn = obsProbFn.sumOut(p.getStatesPrime());
			obsProbFn = obsProbFn.normalize();
			
			obsProbFns.put(actSpacePt, obsProbFn);
			
			nextBeliefFns.put(actSpacePt, new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>());
			obsProbs.put(actSpacePt, new HashMap<HashMap<DDVariable,Integer>, Double>());
			
			for(HashMap<DDVariable,Integer> obsSpacePt:p.getObservationSpace()) {
				double obsProb = obsProbFn.getValue(obsSpacePt);
				
				obsProbs.get(actSpacePt).put(obsSpacePt, obsProb);
				
				if(obsProb>0.0f) {
					
					FactoredCondProbDD tempRestrObsFn =  p.getObservationFunction(actSpacePt);
					tempRestrObsFn = tempRestrObsFn.restrict(obsSpacePt);
					
					FactoredCondProbDD nextBelief =  p.getTransitionFunction(actSpacePt);
					nextBelief = nextBelief.multiply(beliefFn);
					nextBelief = nextBelief.multiply(tempRestrObsFn);
					
					nextBelief = nextBelief.normalize();
					nextBelief = nextBelief.unprime();
					
					
					nextBeliefFns.get(actSpacePt).put(obsSpacePt, nextBelief);
				}
			}
		}
	}
	
	public BeliefAlphaVector pickBestAlpha(List<BeliefAlphaVector> alphas) {
		return pickBestAlpha(alphas,beliefFn);
	}
	
	public static BeliefAlphaVector pickBestAlpha(List<BeliefAlphaVector> alphas, FactoredCondProbDD beliefFn) {
		double bestVal = -Double.MAX_VALUE;
		BeliefAlphaVector bestAlpha = null;
		
		for(BeliefAlphaVector alpha:alphas) {
			double tempVal;
			AlgebraicDD valFn = beliefFn.multiply(alpha.getValueFunction());
			tempVal = valFn.getTotalWeight();
			
			
			if(tempVal>=bestVal) {
				bestVal = tempVal;
				bestAlpha = alpha;
			}
		}
		
		return bestAlpha;
	}
	
	public List<BeliefAlphaVector> pickUsefulAlphas(List<BeliefAlphaVector> alphas) {
		ArrayList<BeliefAlphaVector> usefulAlphas = new ArrayList<BeliefAlphaVector>();
		usefulAlphas.add(pickBestAlpha(alphas));
		
		for(HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> actBeliefs: nextBeliefFns.values()) {
			for(FactoredCondProbDD nextBeliefFn:actBeliefs.values()) {
				BeliefAlphaVector bestAlpha = null;
				double bestVal = -Double.MAX_VALUE;
				for(BeliefAlphaVector alpha:alphas) {
					double tempVal;
		
					AlgebraicDD valFn = nextBeliefFn.multiply(alpha.getValueFunction());
					tempVal = valFn.getTotalWeight();
		
					if(tempVal>=bestVal) {
						bestVal = tempVal;
						bestAlpha = alpha;
					}
				}
				if(bestAlpha!=null) {
					usefulAlphas.add(bestAlpha);
				}
			}
		}
		
		return usefulAlphas;
	}
	
	public FactoredCondProbDD getBeliefFunction() {
		return beliefFn;
	}
	
	public FactoredCondProbDD getNextBeliefFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return nextBeliefFns.get(actSpacePt).get(obsSpacePt);
	}
	
	public Belief getNextBelief(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		if(!nextBeliefFns.containsKey(actSpacePt)) {
			System.out.println("Action " + actSpacePt +" not possible!");
			for(HashMap<DDVariable,Integer> pt:nextBeliefFns.keySet()) {
				System.out.println("  " + pt);
			}
		}
		else if(!nextBeliefFns.get(actSpacePt).containsKey(obsSpacePt)) {
			System.out.println("Observation " + obsSpacePt +" not possible!");
			for(HashMap<DDVariable,Integer> pt:nextBeliefFns.get(actSpacePt).keySet()) {
				System.out.println("  " + pt);
			}
		}
		return new Belief(p,nextBeliefFns.get(actSpacePt).get(obsSpacePt));
	}
	
	public HashMap<HashMap<DDVariable,Integer>, Double> getObservationProbabilities(HashMap<DDVariable,Integer> actSpacePt) {
		return obsProbs.get(actSpacePt);
	}
	
	public AlgebraicDD getImmediateValueFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return immRewardFns.get(actSpacePt);
	}
	
	public FactoredCondProbDD getObservationProbabilityFunction(HashMap<DDVariable,Integer> actSpacePt) {
		return obsProbFns.get(actSpacePt);
	}
	
	public HashMap<DDVariable,Integer> sampleNextObservation(HashMap<DDVariable,Integer> actSpacePt) {
		return sampleSpacePoint(p.getObservations(),getObservationProbabilityFunction(actSpacePt));
	}
	
	public HashMap<DDVariable,Integer> sampleCurrentState() {
		return sampleSpacePoint(p.getStates(),beliefFn);
	}
	
	public HashMap<DDVariable,Integer> sampleSpacePoint(ArrayList<DDVariable> variables, FactoredCondProbDD probFn) {
		HashMap<DDVariable,Integer> point = new HashMap<DDVariable,Integer>();
		
		for(DDVariable variable:variables) {
			ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(variables);
			sumOutVars.remove(variable);
			
			ProbDD probTempFn = probFn.sumOut(sumOutVars).toProbabilityDD();
			
			double thresh = random.nextDouble();
			double weight = 0.0f;
			
			for(int i=0;i<variable.getValueCount();i++){
				HashMap<DDVariable,Integer> tempPt = new HashMap<DDVariable,Integer>();
				tempPt.put(variable,i);
				weight += probTempFn.getValue(tempPt);
				if(weight>thresh) {
					point.put(variable,i);
					break;
				}
			}
		}
		return point;
	}
}
