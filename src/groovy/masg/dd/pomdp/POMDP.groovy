package masg.dd.pomdp

import masg.dd.AlgebraicDecisionDiagram
import masg.dd.DecisionDiagramContext
import masg.dd.DecisionRule
import masg.dd.DecisionRuleCollection
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
		
		DecisionDiagramContext beliefCtxt = new DecisionDiagramContext();
		beliefCtxt.getVariableSpace().addVariables(states)
		
		initBeliefDD = new AlgebraicDecisionDiagram(beliefCtxt);
		
		populateDD(initBeliefDD,initBeliefClosure)
		
		assert transFnStates.size() == transFnClosures.size()
		
		transFn = new DDTransitionFunction();
		
		transFnClosures.eachWithIndex { Closure<Double> transFnClosure, fnIx ->
			DDVariableSpace inVarSpace = new DDVariableSpace();
			transFnStates[fnIx].each{ DDVariable var -> inVarSpace.addVariable(var)}
			
			inVarSpace.addVariable(act)
			
			DDVariableSpace outVarSpace = new DDVariableSpace();
			transFnStates[fnIx].each{DDVariable oldVar -> outVarSpace.addVariable(new DDVariable(oldVar.name + "'",oldVar.numValues))}
			
			CondProbDDContext transCtx = new CondProbDDContext(inVarSpace,outVarSpace)
			CondProbADD currDD = new CondProbADD(transCtx)
			
			populateDD(currDD,transFnClosure)
			
			transFn.appendDD(currDD);
			
		}
		
		
	}
	
	/*private List getUnusedRuleSpace(DDVariableSpace varSpace) {
		List rules = []
		
		List<DDVariable> vars = []
		List<Integer> varValues = []
		
		Map<DDVariable, Integer> maxValues = [:]
		Map<DDVariable, Integer> minValues = [:]
		varSpace.getVariables().each { DDVariable v ->
			
			int totalValuesPossible = (int)Math.pow(2.0f, v.getBitCount())-1
			int firstUnusedValue = totalValuesPossible - v.getValueCount()
			
			minValues[v] = firstUnusedValue
			maxValues[v] = totalValuesPossible
			vars << v
			varValues << firstUnusedValue
		}
		
		boolean overflow = false
		
		while(!overflow) {
			for(int ix=varValues.size()-1;ix>=0;ix--) {
				if(varValues[ix] < maxValues[vars[ix]]) {
					varValues[ix]++
					break;
				}
				else {
					varValues[ix]=minValues[vars[ix]];
					
					if(ix==0) {
						overflow = true;
					}
				}
			}
			
			if(!overflow) {
				HashMap<DDVariable,Integer> varSpacePoint = [:]
				
				for(int ix=0;ix<vars.size();ix++)
					varSpacePoint[vars[ix]]=varValues[ix]
				
				DecisionRule r = varSpace.generateRule(varSpacePoint, Double.NaN)
				rules << r
			}
		}
		
		DecisionRuleCollection negativeRuleCollection = new DecisionRuleCollection(varSpace.getBitCount())
		negativeRuleCollection.addAll(rules);
		negativeRuleCollection.compress();
		
		return new ArrayList<DecisionRule>(negativeRuleCollection.rules)
	}*/
	
	private void populateDD(AlgebraicDecisionDiagram dd, Closure c) {
		
		int numRules=0;
		DDVariableSpace varSpace = dd.getContext().getVariableSpace();
		
		
		DecisionRuleCollection rules = new DecisionRuleCollection(varSpace.getBitCount())
		
		
		
		
		println "Current number of rules:" + dd.getRules().size();
		
		varSpace.each{ HashMap<DDVariable,Integer> varSpacePoint ->
			double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
			
			DecisionRule r = varSpace.generateRule(varSpacePoint,val)
			
			numRules++;
			rules << r
			
			if(rules.size()>100) {
				rules.compress()
			}
			
			if(rules.size()>100) {
				dd.addRules(new ArrayList(rules))
				rules = new DecisionRuleCollection(varSpace.getBitCount())
				println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
			}
			
		}
		
		dd.addRules(new ArrayList(rules))
		println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
		
		println "Adding negative space..."
		
		
		for(int varIx = varSpace.getVariables().size()-1;varIx>=0;varIx--) {
			rules = new DecisionRuleCollection(varSpace.getBitCount())
			HashMap<DDVariable,Range> unusedValues = new HashMap<DDVariable,Range>();
			
			DDVariable currVar = varSpace.getVariables().get(varIx)
			
			int totalValuesPossible = (int)Math.pow(2.0f, currVar.getBitCount())-1
			int firstUnusedValue = currVar.getValueCount()
			unusedValues[currVar] = (firstUnusedValue..totalValuesPossible)
			
			println "$currVar has unused range ${unusedValues[currVar]}"
			
			varSpace.each{ HashMap<DDVariable,Integer> varSpacePoint ->
				double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				
				unusedValues.each{ DDVariable var, Range valRange ->
					HashMap<DDVariable,Integer> varSpacePointCopy = varSpacePoint.clone()
					
					for(int laterVarIx=varIx;laterVarIx<varSpace.getVariables().size();laterVarIx++) {
						varSpacePointCopy.remove(varSpace.getVariables().get(laterVarIx))
					}
					valRange.each{ int varVal ->
						
						varSpacePointCopy[var] = varVal;
						DecisionRule r = varSpace.generateRule(varSpacePointCopy,val)
						numRules++;
						rules << r
					}
				}
				
				if(rules.size()>100) {
					rules.compress()
				}
				
				if(rules.size()>100) {
					dd.addRules(new ArrayList(rules))
					rules = new DecisionRuleCollection(varSpace.getBitCount())
					println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
				}
				
				/*if(varIx == varSpace.getVariables().size()-2) {
					dd.getRules().each{ DecisionRule r ->
						println r
					}
					println("Done")
				}*/
			}
			
			dd.addRules(new ArrayList(rules))
			println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
		}
		
		dd.getRules().each{ DecisionRule r ->
			println r
		}
		println()
		
	}
	public final DDTransitionFunction getTransFns() {
		return transFn;
	}
}
