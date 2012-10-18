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
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		DDVariableSpace obsSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getObservations()));
		
		RealValueFunction rewFn = p.getRewardFn();
		RealValueFunction immRewardFn = belief.times(rewFn);
		
		RealValueFunction bestValFn = null;
		
		HashMap<DDVariable,Integer> bestAct = null;
		
		if(bestAlphas.getAlphaVectors().size()>0) {
			
			double bestVal = -Double.MAX_VALUE;
			for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
				
				System.out.println(" Action: " + actSpacePt);
				RealValueFunction futureValue = new RealValueFunction(AlgebraicDDBuilder.build(p.getStates(), 0.0f));
				
				CondProbFunction obsProbs = POMDPUtils.getObservationProbs(p, belief, actSpacePt);
				
				for(HashMap<DDVariable,Integer> obsSpacePt:obsSpace) {
					
					double obsProb = obsProbs.getValue(obsSpacePt);
					
					if(obsProb>0.0f) {
						System.out.println("  Observation: " + obsSpacePt);
						CondProbFunction nextBelief = POMDPUtils.updateBelief(p, belief, actSpacePt, obsSpacePt);
						BeliefAlphaVector val = bestAlphas.getBestAlphaVector(nextBelief);
	
						RealValueFunction temp = val.getValueFunction().times(obsProb);
						futureValue = futureValue.plus(temp);
						
					}
				}
				
				double totalValue = futureValue.getDD().getRules().getRuleValueSum();
				
				if(totalValue > bestVal) {
					bestValFn = futureValue;
					bestAct = actSpacePt;
					bestVal = totalValue;
				}
			}
			
			bestValFn = bestValFn.times(discFactor);
			
		}
		else {
			bestValFn = new RealValueFunction(AlgebraicDDBuilder.build(p.getStates(), 0.0f));
			for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
				bestAct = actSpacePt;
				break;
			}
		}
		
		bestValFn = bestValFn.plus(immRewardFn);
		
		newAlphas.add(new BeliefAlphaVector(bestAct, bestValFn, belief));
		
		System.out.println("  Belief point value: " + bestValFn.getDD().getRules().getRuleValueSum());
		System.out.println("  There are now " + newAlphas.getAlphaVectors().size() + " alpha vectors");	
		System.out.println("  Update took " + (new Date().getTime() - milliStart) + " milliseconds");
		
		System.out.println();
		
		
	}
	
}
