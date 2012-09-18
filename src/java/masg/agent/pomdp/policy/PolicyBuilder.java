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
		CondProbFunction initBelief = p.getInitialtBelief();
		CondProbFunction transFn = p.getTransFn();
		CondProbFunction rewFn = p.getRewardFn();
		CondProbFunction obsFn = p.getObsFns();
		
		DDVariableSpace actSpace = new DDVariableSpace(new ArrayList<DDVariable>(p.getActions()));
		
		double discFactor = 0.9f;
		
		
		HashMap<HashMap<DDVariable,Integer>,CondProbFunction> pureAlphas = new HashMap<HashMap<DDVariable,Integer>,CondProbFunction>();
		
		

		for(HashMap<DDVariable,Integer> actSpacePt:actSpace) {
			
			CondProbFunctionBuilder alphaFnBuilder = new CondProbFunctionBuilder();
			alphaFnBuilder.add(new ArrayList<DDVariable>(), p.getStates(),0.0f);
			CondProbFunction currAlpha = alphaFnBuilder.build();
			
			CondProbFunction fixedTransFn = transFn.restrict(actSpacePt);
			CondProbFunction fixedRewFn = rewFn.restrict(actSpacePt);
			
			for(int i=0;i<50;i++) {
				
				CondProbFunction discTransFn = fixedTransFn.times(currAlpha);
				discTransFn = discTransFn.sumOut(p.getStates(),false);
				discTransFn = discTransFn.times(discFactor);
				discTransFn.unprimeAllContexts();
				
				currAlpha = discTransFn.plus(fixedRewFn);
			}
			pureAlphas.put(actSpacePt, currAlpha);
		}
			
			
		
	}
}
