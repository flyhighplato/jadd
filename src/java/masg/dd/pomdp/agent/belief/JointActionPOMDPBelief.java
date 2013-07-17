package masg.dd.pomdp.agent.belief;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;

public class JointActionPOMDPBelief implements Belief {
	final POMDP pMe;
	final POMDP pOther;
	final FactoredCondProbDD beliefFn;
	
	HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> immRewardFns = new HashMap<HashMap<DDVariable,Integer>, AlgebraicDD>();
	HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> obsProbFns = new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>();
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFns = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> >();
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbs = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	
	Random random = new Random();
	
	public JointActionPOMDPBelief(POMDP pMe, POMDP pOther, FactoredCondProbDD fn) {
		this.pMe = pMe;
		this.pOther = pOther;
		this.beliefFn = fn;
		
		for(HashMap<DDVariable,Integer> actSpacePtOther:pOther.getActionSpace()) {
			for(HashMap<DDVariable,Integer> actSpacePtMe:pMe.getActionSpace()) {
				
				HashMap<DDVariable,Integer> actSpacePtJoint = new HashMap<DDVariable,Integer>();
				actSpacePtJoint.putAll(actSpacePtOther);
				actSpacePtJoint.putAll(actSpacePtMe);
				
				
				immRewardFns.put(actSpacePtMe, beliefFn.multiply(pMe.getRewardFunction().restrict(actSpacePtJoint)) );
				
				FactoredCondProbDD tempRestrTransFn = pMe.getTransitionFunction().restrict(actSpacePtJoint).multiply(beliefFn);
				tempRestrTransFn = tempRestrTransFn.sumOut(pMe.getStates());
				tempRestrTransFn = tempRestrTransFn.normalize();
				
				FactoredCondProbDD obsProbFn = pMe.getObservationFunction();
				obsProbFn = obsProbFn.multiply(tempRestrTransFn);
				obsProbFn = obsProbFn.restrict(actSpacePtJoint);
				obsProbFn = obsProbFn.sumOut(pMe.getStatesPrime());
				obsProbFn = obsProbFn.normalize();
				
				obsProbFns.put(actSpacePtJoint, obsProbFn);
				
				nextBeliefFns.put(actSpacePtJoint, new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>());
				obsProbs.put(actSpacePtJoint, new HashMap<HashMap<DDVariable,Integer>, Double>());
				
				for(HashMap<DDVariable,Integer> obsSpacePt:pMe.getObservationSpace()) {
					double obsProb = obsProbFn.getValue(obsSpacePt);
					
					obsProbs.get(actSpacePtJoint).put(obsSpacePt, obsProb);
					
					if(obsProb>0.0f) {
						
						FactoredCondProbDD tempRestrObsFn =  pMe.getObservationFunction();
						tempRestrObsFn = tempRestrObsFn.restrict(obsSpacePt);
						tempRestrObsFn = tempRestrObsFn.restrict(actSpacePtMe);
						tempRestrObsFn = tempRestrObsFn.restrict(actSpacePtOther);
						tempRestrObsFn = tempRestrObsFn.normalize();
						
						FactoredCondProbDD nextBelief =  pMe.getTransitionFunction(actSpacePtMe).restrict(actSpacePtOther);
						nextBelief = nextBelief.multiply(beliefFn);
						nextBelief = nextBelief.normalize();
						nextBelief = nextBelief.multiply(tempRestrObsFn);
						
						nextBelief = nextBelief.normalize();
						nextBelief = nextBelief.unprime();
						
						
						nextBeliefFns.get(actSpacePtJoint).put(obsSpacePt, nextBelief);
					}
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
			double tempVal = beliefFn.dotProduct(alpha.getValueFunction());
			
			if(tempVal>=bestVal) {
				bestVal = tempVal;
				bestAlpha = alpha;
			}
		}
		
		return bestAlpha;
	}
	
	public FactoredCondProbDD getBeliefFunction() {
		return beliefFn;
	}
	
	public FactoredCondProbDD getNextBeliefFunction(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
		return nextBeliefFns.get(actSpacePt).get(obsSpacePt);
	}
	
	public JointActionPOMDPBelief getNextBelief(HashMap<DDVariable,Integer> actSpacePt, HashMap<DDVariable,Integer> obsSpacePt) {
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
		return new JointActionPOMDPBelief(pMe,pOther,nextBeliefFns.get(actSpacePt).get(obsSpacePt));
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
		return sampleSpacePoint(pMe.getObservations(),getObservationProbabilityFunction(actSpacePt));
	}
	
	public HashMap<DDVariable,Integer> sampleCurrentState() {
		return sampleSpacePoint(pMe.getStates(),beliefFn);
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
