package masg.agent.pomdp.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import masg.agent.pomdp.POMDPUtils;
import masg.dd.AlgebraicDDBuilder;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.alphavector.BeliefAlphaVectorCollection;
import masg.dd.function.CondProbFunction;
import masg.dd.function.RealValueFunction;
import masg.dd.pomdp.POMDP;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class PolicyBuilder {
	double discFactor = 0.9f;
	double tolerance = 0.00001f;
	
	BeliefAlphaVectorCollection bestAlphas = new BeliefAlphaVectorCollection();
	
	POMDP p;
	public PolicyBuilder(POMDP p) throws Exception {
		this.p = p;
	}
	
	public void dpBackup(CondProbFunction belief, BeliefAlphaVectorCollection newAlphas) throws Exception {
		long milliStart = new Date().getTime();
		
		RealValueFunction rewFn = p.getRewardFn();

		RealValueFunction immReward = belief.times(rewFn);
		
		double immRewardValue = immReward.getDD().getRules().getRuleValueSum();
		
		double bestVal = Double.NEGATIVE_INFINITY;
		HashMap<DDVariable,Integer> bestAct = null;

		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		DDVariableSpace obsSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getObservations()));
		
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			
			System.out.println(" Action: " + actSpacePt);
			double futureValue = 0.0f;
			
			CondProbFunction obsProbs = POMDPUtils.getObservationProbs(p, belief, actSpacePt);
			
			for(HashMap<DDVariable,Integer> obsSpacePt:obsSpace) {
				
				double obsProb = obsProbs.getValue(obsSpacePt);
				
				if(obsProb>0.0f) {
					//System.out.println("  Observation: " + obsSpacePt);
					CondProbFunction nextBelief = POMDPUtils.updateBelief(p, belief, actSpacePt, obsSpacePt);
					double val = bestAlphas.getBeliefValue(nextBelief);
					
					if(!Double.isNaN(val)) {
						futureValue += obsProb * val;
					}
				}
			}
			
			if(futureValue > bestVal) {
				bestVal = futureValue;
				bestAct = actSpacePt;
			}
		}
		
		//milliStart = new Date().getTime();
		bestVal = immRewardValue + discFactor * bestVal;
		newAlphas.add(new BeliefAlphaVector(bestAct,bestVal, belief));
		
		System.out.println("  Belief point value: " + bestVal);
		System.out.println("  There are now " + newAlphas.getAlphaVectors().size() + " alpha vectors");	
		System.out.println("  Update took " + (new Date().getTime() - milliStart) + " milliseconds");
		
		System.out.println();
		
		
	}
	
}
