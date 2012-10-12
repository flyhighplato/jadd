package masg.agent.pomdp.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import masg.dd.RealValueFunctionBuilder;
import masg.dd.alphavector.AlphaVector;
import masg.dd.alphavector.DominantAlphaVectorCollection;
import masg.dd.function.CondProbFunction;
import masg.dd.function.RealValueFunction;
import masg.dd.pomdp.POMDP;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class PolicyBuilder {
	double discFactor = 0.9f;
	double tolerance = 0.00001f;
	
	DominantAlphaVectorCollection bestAlphas = new DominantAlphaVectorCollection();
	
	POMDP p;
	public PolicyBuilder(POMDP p) throws Exception {
		this.p = p;
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		
		
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			bestAlphas.add(new AlphaVector(actSpacePt,p.getInitialtBelief().timesAndSumOut(p.getRewardFn(), new ArrayList<DDVariable>())));
		}
	}
	
	public void computePureStrategies(int maxIterations) throws Exception {
		CondProbFunction transFn = p.getTransFn();
		RealValueFunction rewFn = p.getRewardFn();
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			
			RealValueFunction currAlphaFn = RealValueFunctionBuilder.build(p.getStates(),0.0f);
			
			CondProbFunction fixedTransFn = transFn.restrict(actSpacePt);
			RealValueFunction fixedRewFn = rewFn.restrict(actSpacePt);
			
			System.out.println("Num reward fn rules:" + fixedRewFn.getDD().getRules().size());
			
			double bellmanError = 0.0f;
			
			for(int i=0;i<maxIterations;i++) {
				
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

			currAlphaFn.getDD().compress();
			
			AlphaVector newAlpha = new AlphaVector(actSpacePt,currAlphaFn);
			
			DDVariableSpace obsSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getObservations()));
			for(HashMap<DDVariable,Integer> obsSpacePt:obsSpace) {
				newAlpha.setCondPlanForObs(obsSpacePt, newAlpha);
			}
			
			if(bestAlphas.add(newAlpha)) {
				System.out.println("Adding alpha vector for " + actSpacePt);
				System.out.println("  Alpha vector has " + currAlphaFn.getDD().getRules().size() + " rules");
			}
			else {
				System.out.println("Not adding alpha vector for " + actSpacePt + " (dominated)");
			}
		}
		
	}
	
	public void dpBackup(CondProbFunction belief, DominantAlphaVectorCollection newAlphas) throws Exception {
		long milliStart = new Date().getTime();
		
		CondProbFunction transFn = p.getTransFn();
		CondProbFunction obsFn = p.getObsFns();
		RealValueFunction rewFn = p.getRewardFn();
		
		RealValueFunction immReward = belief.timesAndSumOut(rewFn, new ArrayList<DDVariable>());
		
		
		double value = immReward.getDD().getRules().getRuleValueSum();
		
		CondProbFunction temp = transFn.times(belief);
		temp = obsFn.times(temp);
		
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		
		
		RealValueFunction currValFn = bestAlphas.getValueFunction();
		currValFn.primeAllContexts();
		
		
		RealValueFunction nextValFn = null;
		double bestVal = Double.NEGATIVE_INFINITY;
		HashMap<DDVariable,Integer> bestAct = null;
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			CondProbFunction fixedActTemp = temp.restrict(actSpacePt);
			fixedActTemp = fixedActTemp.sumOut(p.getObservations(), false);
			fixedActTemp.normalize();
			
			RealValueFunction futureValFn = fixedActTemp.timesAndSumOut(currValFn, new ArrayList<DDVariable>());
			
			double futureValue = futureValFn.getDD().getRules().getRuleValueSum();
			
			if(futureValue > bestVal) {
				bestVal = futureValue;
				nextValFn = futureValFn;
				bestAct = actSpacePt;
			}
		}
		
		currValFn.unprimeAllContexts();
		nextValFn.unprimeAllContexts();
		
		
		nextValFn = nextValFn.times(discFactor);
		
		nextValFn = nextValFn.plus(immReward);
		nextValFn.getDD().compress();
		
		System.out.println("  Value: " + (value + bestVal));
		System.out.println("  DP took " + (new Date().getTime() - milliStart) + " milliseconds");
		
		milliStart = new Date().getTime();
		if(newAlphas.add(new AlphaVector(bestAct,nextValFn))) {
			System.out.println("  There are now " + newAlphas.getAlphaVectors().size() + " alpha vectors");
		}
		System.out.println("  Adding to alpha collection took " + (new Date().getTime() - milliStart) + " milliseconds");
		System.out.println();
	}
	
}
