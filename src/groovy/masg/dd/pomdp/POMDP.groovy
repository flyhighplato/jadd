package masg.dd.pomdp

import masg.dd.AlgebraicDecisionDiagram
import masg.dd.DecisionDiagramContext
import masg.dd.DecisionRule
import masg.dd.cpt.CondProbADD
import masg.dd.cpt.CondProbDDContext
import masg.dd.function.DDTransitionFunction

import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace

class POMDP {
	protected List<String> obs;
	protected List<String> act
	protected List<String> states
	
	protected DDTransitionFunction transFn;
	protected DDTransitionFunction obsFn;
	protected DDTransitionFunction rewFn;
	protected AlgebraicDecisionDiagram initBeliefDD;
	
	
	public POMDP(List<DDVariable> obs, List<DDVariable> act, List<DDVariable> states, Closure<Double> initBeliefClosure, List<List<DDVariable>> transFnStates, List<Closure<Double>> transFnClosures, List<Closure<Double>> obsFnClosures, List<Closure<Double>> rewFnClosures) {
		this.obs = obs
		this.act = act
		this.states = states
		
		int numRules = 0;
		
		DecisionDiagramContext beliefCtxt = new DecisionDiagramContext();
		beliefCtxt.getVariableSpace().addVariables(states)
		
		initBeliefDD = new AlgebraicDecisionDiagram(beliefCtxt);
		
		List<DecisionRule> rules = []
		beliefCtxt.getVariableSpace().each { HashMap<DDVariable,Integer> varSpacePoint ->
			double val = initBeliefClosure(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
			DecisionRule r = beliefCtxt.getVariableSpace().generateRule(varSpacePoint, val)
			
			rules << r
			numRules++
			if(rules.size()>100) {
				initBeliefDD.addRules(rules)
				rules = []
				println "Current number of rules in init belief:" + initBeliefDD.getRules().size();
			}
		}
		
		initBeliefDD.addRules(rules)
		rules = []
		
		println "Current number of rules in init belief:" + initBeliefDD.getRules().size();
		println "Number of rules submitted:" + numRules;
		
		System.out.println("RULES");
		for(DecisionRule r:initBeliefDD.getRules()) {
			System.out.println(r);
		}
		assert transFnStates.size() == transFnClosures.size()
		
		/*transFn = new DDTransitionFunction();
		
		transFnClosures.eachWithIndex { Closure<Double> transFnClosure, fnIx ->
			println "Computing DD for #$fnIx"
			
			DDVariableSpace inVarSpace = new DDVariableSpace();
			
			transFnStates[fnIx].each{ DDVariable var -> inVarSpace.addVariable(var)}
			inVarSpace.addVariable(act)
			
			DDVariableSpace outVarSpace = new DDVariableSpace();
			transFnStates[fnIx].each{DDVariable oldVar -> outVarSpace.addVariable(new DDVariable(oldVar.name + "'",oldVar.numValues))}
			
			CondProbDDContext transCtx = new CondProbDDContext(inVarSpace,outVarSpace)
			CondProbADD currDD = new CondProbADD(transCtx)
			
			
			rules = []
			//numRulesSubmitted = 0;
			
			int numBitsTotal = transCtx.getVariableSpace().getBitCount()
			println "Total bits: $numBitsTotal"
			
			numRules = 0;
			transCtx.getVariableSpace().each{ HashMap<DDVariable,Integer> varSpacePoint ->
				double val = transFnClosure(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				
				DecisionRule r = transCtx.getVariableSpace().generateRule(varSpacePoint,val)
				
				numRules++;
				rules << r
				
				if(rules.size()>100) {
					currDD.addRules(rules)
					rules = []
					println "Current number of rules:" + currDD.getRules().size();
				}
				
			}
			
			currDD.addRules(rules)
			rules = []
			println "Current number of rules:" + currDD.getRules().size();
			println "Number of rules submitted:" + numRules;
			transFn.appendDD(currDD);
			
		}
		
		*/
	}
	
	public final DDTransitionFunction getTransFns() {
		return transFn;
	}
}
