package masg.agent.pomdp.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.RealValueFunctionBuilder;
import masg.dd.function.CondProbFunction;
import masg.dd.function.RealValueFunction;
import masg.dd.pomdp.POMDP;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class PolicyBuilder {
	POMDP p;
	public PolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	public void computePureStrategies() throws Exception {
		CondProbFunction transFn = p.getTransFn();
		RealValueFunction rewFn = p.getRewardFn();
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		
		double discFactor = 0.9f;
		double tolerance = 0.001f;
		
		HashMap<HashMap<DDVariable,Integer>,RealValueFunction> pureAlphas = new HashMap<HashMap<DDVariable,Integer>,RealValueFunction>();
		
		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			
			RealValueFunction currAlpha = RealValueFunctionBuilder.build(p.getStates(),0.0f);
			
			CondProbFunction fixedTransFn = transFn.restrict(actSpacePt);
			RealValueFunction fixedRewFn = rewFn.restrict(actSpacePt);
			
			double bellmanError = 0.0f;
			
			for(int i=0;i<50;i++) {
				
				System.out.println("Iteration #" + i);
				currAlpha.primeAllContexts();
				RealValueFunction discTransFn = fixedTransFn.timesAndSumOut(currAlpha,currAlpha.getDD().getContext().getVariableSpace().getVariables());
				discTransFn = discTransFn.times(discFactor);

				RealValueFunction newAlpha = discTransFn.plus(fixedRewFn);
				
				currAlpha.unprimeAllContexts();
				bellmanError = newAlpha.maxDiff(currAlpha);
				System.out.println("Bellman error:" + bellmanError);
				if(bellmanError<tolerance) {
					break;
				}
				currAlpha = newAlpha;
			}
			
			boolean isDominated = false;
			for(RealValueFunction otherAlpha:pureAlphas.values()) {
				if(otherAlpha.dominates(currAlpha, tolerance)) {
					isDominated = true;
					break;
				}
			}
			if(!isDominated) {
				System.out.println("Adding alpha vector for " + actSpacePt);
				pureAlphas.put(actSpacePt, currAlpha);
			}
			else {
				System.out.println("Not adding alpha vector for " + actSpacePt + " (dominated)");
			}
		}
			
			
		
	}
}
