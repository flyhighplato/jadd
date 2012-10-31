package masg.agent.pomdp.policy.refactored;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import masg.agent.pomdp.belief.refactored.BeliefRegion;
import masg.dd.alphavector.refactored.BeliefAlphaVector;
import masg.dd.pomdp.refactored.POMDP;
import masg.dd.refactored.AlgebraicDD;
import masg.dd.refactored.CondProbDD;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;


public class PolicyBuilder {
	double discFactor = 0.9f;
	double tolerance = 0.00001f;
	
	ArrayList<BeliefAlphaVector> bestAlphas = new ArrayList<BeliefAlphaVector>();
	
	POMDP p;
	
	
	public PolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	
	public void build(BeliefRegion belRegion, int numIterations) {
		
		List<CondProbDD> beliefs = belRegion.getBeliefSamples();
		
		for(int i=0;i<numIterations;++i) {
			System.out.println("Iteration #" + i);
			
			ArrayList<BeliefAlphaVector> newAlphas = new ArrayList<BeliefAlphaVector>();
			
			for(int j=0;j<beliefs.size();++j) {
				System.out.println(" " + i + " > Sample #" + j);
				
				CondProbDD belief = beliefs.get(j);
				BeliefAlphaVector newAlpha = dpBackup(belief);
				
				if(newAlphas.size()>0) {
					double beliefValues[] = new double[beliefs.size()];
					
					for(int belIx=0;belIx<beliefs.size();++belIx) {
						beliefValues[belIx] = newAlpha.getValueFunction().multiply(beliefs.get(belIx)).getTotalWeight();
					}
					
					for(int belIx=0;belIx<beliefs.size();++belIx) {
						double maxBelVal = -Double.MAX_VALUE;
						
						for(BeliefAlphaVector alpha:newAlphas) {
							double val = alpha.getValueFunction().multiply(beliefs.get(belIx)).getTotalWeight();
							if(val>maxBelVal) {
								maxBelVal = val;
							}
						}
						
						if(maxBelVal<beliefValues[belIx]) {
							newAlphas.add(newAlpha);
							break;
						}
					}
				}
				else {
					newAlphas.add(newAlpha);
				}
				
			}
			
			System.out.println("Number of new alpha vectors:" + newAlphas.size());
			bestAlphas = newAlphas;
			
			System.out.println();
		}
		
	}
	
	
	public BeliefAlphaVector dpBackup(CondProbDD belief) {
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		DDVariableSpace obsSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getObservations()));
		
		double bestActionValue = -Double.MAX_VALUE;
		BeliefAlphaVector bestActionAlpha = null;
		
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			//System.out.println(" Action: " + actSpacePt);
			
			AlgebraicDD actionAlpha = p.getRewardFunction().restrict(actSpacePt).multiply(belief);
			
			CondProbDD restrTransFn = p.getTransitionFunction().restrict(actSpacePt);
			CondProbDD restrObsFn = p.getObservationFunction().restrict(actSpacePt);
			
			CondProbDD tempRestrTransFn = restrTransFn.multiply(belief);
			tempRestrTransFn = tempRestrTransFn.sumOut(p.getStates());
			
			CondProbDD obsProbFn = restrObsFn.multiply(tempRestrTransFn);
			obsProbFn = obsProbFn.sumOut(p.getStatesPrime());
			obsProbFn = obsProbFn.normalize();
			
			for(HashMap<DDVariable,Integer> obsSpacePt:obsSpace) {
				double obsProb = obsProbFn.getValue(obsSpacePt);
				
				if(obsProb>0.0f) {
					//System.out.println("  Observation: " + obsSpacePt);
					
					CondProbDD tempRestrObsFn = restrObsFn.restrict(obsSpacePt);
					CondProbDD nextBelief = tempRestrObsFn.multiply(tempRestrTransFn);
					nextBelief = nextBelief.normalize();
					nextBelief = nextBelief.unprime();
					
					double bestNextVal = -Double.MAX_VALUE;
					AlgebraicDD bestNextAlpha = null;
					
					for(BeliefAlphaVector alpha:bestAlphas) {
						AlgebraicDD tempNextAlpha = alpha.getValueFunction().multiply(nextBelief);
						double expectedValue = tempNextAlpha.getTotalWeight();
						
						if(expectedValue>=bestNextVal) {
							bestNextVal = expectedValue;
							bestNextAlpha = tempNextAlpha;
						}
					}
					
					if(bestNextAlpha!=null) {
						bestNextAlpha = bestNextAlpha.multiply(obsProb);
						bestNextAlpha = bestNextAlpha.multiply(discFactor);
						actionAlpha = actionAlpha.plus(bestNextAlpha);
					}
				}
			}
			
			double expectedActionValue = actionAlpha.getTotalWeight();
			
			if(expectedActionValue>=bestActionValue) {
				bestActionValue = expectedActionValue;
				bestActionAlpha = new BeliefAlphaVector(actSpacePt,actionAlpha,belief);
			}
		}
		
		
		return bestActionAlpha;
		
	}
}
