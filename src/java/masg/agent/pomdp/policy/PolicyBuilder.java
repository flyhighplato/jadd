package masg.agent.pomdp.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import masg.agent.pomdp.POMDPUtils;
import masg.dd.RealValueFunctionBuilder;
import masg.dd.alphavector.AlphaVector;
import masg.dd.function.CondProbFunction;
import masg.dd.function.RealValueFunction;
import masg.dd.pomdp.POMDP;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class PolicyBuilder {
	double discFactor = 0.9f;
	double tolerance = 0.00001f;
	
	POMDP p;
	public PolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	public void computePureStrategies() throws Exception {
		CondProbFunction transFn = p.getTransFn();
		RealValueFunction rewFn = p.getRewardFn();
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		ArrayList<AlphaVector> pureAlphas = new ArrayList<AlphaVector>();
		
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			
			RealValueFunction currAlphaFn = RealValueFunctionBuilder.build(p.getStates(),0.0f);
			
			CondProbFunction fixedTransFn = transFn.restrict(actSpacePt);
			RealValueFunction fixedRewFn = rewFn.restrict(actSpacePt);
			
			System.out.println("Num reward fn rules:" + fixedRewFn.getDD().getRules().size());
			
			double bellmanError = 0.0f;
			
			for(int i=0;i<10;i++) {
				
				long milliStart = new Date().getTime();
				System.out.println("Iteration #" + i);
				currAlphaFn.primeAllContexts();
				RealValueFunction discTransFn = fixedTransFn.timesAndSumOut(currAlphaFn,currAlphaFn.getDD().getContext().getVariableSpace().getVariables());
				discTransFn = discTransFn.times(discFactor);

				RealValueFunction newAlphaFn = discTransFn.plus(fixedRewFn);
				
				currAlphaFn.unprimeAllContexts();
				bellmanError = newAlphaFn.maxDiff(currAlphaFn);
				System.out.println(" Bellman error:" + bellmanError);
				if(bellmanError<tolerance) {
					break;
				}
				
				long milliTook = new Date().getTime() - milliStart;
				System.out.println(" Iteration took " + milliTook + " milliseconds");
				currAlphaFn = newAlphaFn;
			}
			
			boolean isDominated = false;
			for(AlphaVector otherAlpha:pureAlphas) {
				if(otherAlpha.getFn().dominates(currAlphaFn, tolerance)) {
					isDominated = true;
					break;
				}
			}
			if(!isDominated) {
				System.out.println("Adding alpha vector for " + actSpacePt);
				System.out.println("  Alpha vector has " + currAlphaFn.getDD().getRules().size() + " rules");
				
				currAlphaFn.getDD().compress();
				AlphaVector newAlpha = new AlphaVector(actSpacePt,currAlphaFn);
				
				DDVariableSpace obsSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getObservations()));
				for(HashMap<DDVariable,Integer> obsSpacePt:obsSpace) {
					newAlpha.setCondPlanForObs(obsSpacePt, newAlpha);
				}
				
				pureAlphas.add(newAlpha);
			}
			else {
				System.out.println("Not adding alpha vector for " + actSpacePt + " (dominated)");
			}
		}
		
		dpBackup(p.getInitialtBelief(),pureAlphas);
	}
	
	public void dpBackup(CondProbFunction belief, ArrayList<AlphaVector> alphaVectors) throws Exception {
		
		CondProbFunction transFn = p.getTransFn();
		CondProbFunction obsFn = p.getObsFns();
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		DDVariableSpace obsSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getObservations()));
		
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			long milliStart = new Date().getTime();
			//Get reward for this action
			RealValueFunction rewFn = p.getRewardFn().restrict(actSpacePt);
			double immediateActionValue = belief.timesAndSumOut(rewFn, rewFn.getDD().getContext().getVariableSpace().getVariables()).getDD().getRules().getRuleValueSum();
			
			double actionValue = immediateActionValue;
			CondProbFunction obsProbs = POMDPUtils.getObservationProbs(p, belief, actSpacePt);
			
			System.out.println("Action:" + actSpacePt);
			
			
			//Get best reward for each observable outcome multiplied by the outcome's probability
			HashMap<HashMap<DDVariable,Integer>,AlphaVector> obsBestPlan = new HashMap<HashMap<DDVariable,Integer>,AlphaVector>();
			
			for(HashMap<DDVariable,Integer> obsSpacePt:obsSpace) {
				double obsProb = obsProbs.getValue(obsSpacePt);
				
				if(obsProb>0.0f) {
					System.out.println("   Observation:" + obsSpacePt);
					
					CondProbFunction nextBelief = POMDPUtils.updateBelief(p, belief, actSpacePt, obsSpacePt);
					
					double maxCondPlanValue = Double.NEGATIVE_INFINITY;
					AlphaVector maxCondPlanAlphaVector = null;
					
					for(AlphaVector alphaVector: alphaVectors) {
						RealValueFunction valBeliefFn = nextBelief.timesAndSumOut(alphaVector.getFn(), alphaVector.getFn().getDD().getContext().getVariableSpace().getVariables());
						
						double valBelief = valBeliefFn.getDD().getRules().getRuleValueSum();
						valBelief = obsProb * valBelief;
						
						if(maxCondPlanValue<valBelief) {
							maxCondPlanValue = valBelief;
							maxCondPlanAlphaVector = alphaVector;
						}
					}
					
					obsBestPlan.put(obsSpacePt, maxCondPlanAlphaVector);
					actionValue += maxCondPlanValue;
				}
			}
			
			System.out.println("Value:" + actionValue);
			System.out.println();
			
			//Calculate value function for this action
			RealValueFunction nextValFn = null;
			for(HashMap<DDVariable,Integer> obsSpacePt:obsBestPlan.keySet()) {
				AlphaVector bestAlpha = obsBestPlan.get(obsSpacePt);
				
				if(nextValFn == null)
					nextValFn = bestAlpha.getFn().times(discFactor);
				else {
					RealValueFunction temp = bestAlpha.getFn().times(discFactor);
					nextValFn = nextValFn.plus(temp);
				}

			}
			
			//Calculate value of this action
			nextValFn.primeAllContexts();
			
			CondProbFunction fixedTransFn = transFn.restrict(actSpacePt);
			CondProbFunction fixedObsFn = obsFn.restrict(actSpacePt);
			
			CondProbFunction tempObsTransFn = fixedTransFn.times(fixedObsFn);
			ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(p.getObservations());
			for(DDVariable s:p.getStates()) {
				sumOutVars.add(new DDVariable(s.getName()+"'",s.getValueCount()));
			}
			RealValueFunction nextAlphaVectorFn = tempObsTransFn.timesAndSumOut(nextValFn,sumOutVars);
			nextAlphaVectorFn = nextAlphaVectorFn.plus(rewFn);
			
			AlphaVector nextAlphaVector = new AlphaVector(actSpacePt,nextAlphaVectorFn,obsBestPlan);
			
			boolean isDominated = false;
			for(AlphaVector otherAlpha:alphaVectors) {
				if(otherAlpha.getFn().dominates(nextAlphaVector.getFn(), tolerance)) {
					isDominated = true;
					break;
				}
			}
			if(!isDominated) {
				System.out.println("Adding alpha vector for " + actSpacePt);
				System.out.println("  Alpha vector has " + nextAlphaVectorFn.getDD().getRules().size() + " rules");
				nextAlphaVectorFn.getDD().compress();
				System.out.println("  After compression, alpha vector has " + nextAlphaVectorFn.getDD().getRules().size() + " rules");
				nextAlphaVector.getFn().unprimeAllContexts();
				alphaVectors.add(nextAlphaVector);
			}
			else {
				System.out.println("Not adding alpha vector for " + actSpacePt + " (dominated)");
			}
			long milliTook = new Date().getTime() - milliStart;
			System.out.println("  Took " + milliTook + " milliseconds");
			System.out.println();
		}
		
		
	}
}
