package masg.agent.pomdp.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.CondProbFunctionBuilder;
import masg.dd.function.CondProbFunction;
import masg.dd.pomdp.POMDP;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class PolicyBuilder {
	POMDP p;
	public PolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	public void computePureStrategies() throws Exception {
		CondProbFunction currBelief = p.getInitialtBelief();
		CondProbFunction transFn = p.getTransFn();
		CondProbFunction rewFn = p.getRewardFn();
		CondProbFunction obsFn = p.getObsFns();
		
		/*CondProbFunctionBuilder valFnBuilder = new CondProbFunctionBuilder();
		valFnBuilder.add(new ArrayList<DDVariable>(), p.getStates(),0.0f);
		CondProbFunction valFn = valFnBuilder.build();
		valFn.primeAllContexts();*/
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		
		double discFactor = 0.9f;
		
		boolean valFnFirstInit = false;
		
		
		HashMap<HashMap<DDVariable,Integer>,CondProbFunction> pureAlphas = new HashMap<HashMap<DDVariable,Integer>,CondProbFunction>();
		
		for(int i=0;i<5;++i) {
			for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
				CondProbFunction fixedTransFn = transFn.restrict(actSpacePt);
				CondProbFunction fixedObsFn = obsFn.restrict(actSpacePt);
				CondProbFunction fixedRewFn = rewFn.restrict(actSpacePt);
				
				//Next observable states
				currBelief.primeAllContexts();
				CondProbFunction tempCurrObs = fixedObsFn.times(currBelief);
				tempCurrObs = tempCurrObs.sumOut(p.getObservations(), false);
				currBelief.unprimeAllContexts();
					
				//Next transition states
				CondProbFunction tempTrans = fixedTransFn.times(currBelief);
				tempTrans = tempTrans.sumOut(p.getStates(),false);
				
				//Next observable states that could be transitioned to
				CondProbFunction nextStates = tempTrans.times(tempCurrObs);
				nextStates.normalize();
				
				
				CondProbFunction currRew = fixedRewFn.times(currBelief);
				currRew.primeAllContexts();
				
				if(!pureAlphas.containsKey(actSpacePt)) {
					pureAlphas.put(actSpacePt,currRew);
				}
				else {
					CondProbFunction valFn = pureAlphas.get(actSpacePt);
					//Value of possible next transitioned/observable states
					CondProbFunction nextValFn = valFn.times(nextStates);
					
					//Discount it
					nextValFn = nextValFn.times(discFactor);
					
					//Add current reward
					nextValFn = nextValFn.plus(currRew);
					
					pureAlphas.put(actSpacePt, nextValFn);
				}
				
			}
			
			System.out.println("Iteration #" + i);
		}
		
	}
}
