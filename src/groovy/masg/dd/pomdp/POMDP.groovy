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

class POMDP{
	protected List<DDVariable> obs;
	protected List<DDVariable> act
	protected List<DDVariable> states
	
	protected DDTransitionFunction transFn;
	protected DDTransitionFunction obsFn;
	protected AlgebraicDecisionDiagram rewFnDD;
	protected AlgebraicDecisionDiagram initBeliefDD;
	
	
	private POMDP() {
		
	}
	public POMDP(List<DDVariable> obs, List<DDVariable> act, List<DDVariable> states, Closure<Double> initBeliefClosure, List<List<DDVariable>> transFnStates, List<Closure<Double>> transFnClosures, List<List<DDVariable>> obsFnVars, List<Closure<Double>> obsFnClosures, Closure<Double> rewFnClosure) {
		this.obs = obs
		this.act = act
		this.states = states
		
		DecisionDiagramContext beliefCtxt = new DecisionDiagramContext();
		beliefCtxt.getVariableSpace().addVariables(states)
		
		initBeliefDD = new AlgebraicDecisionDiagram(beliefCtxt);
		populateDD(initBeliefDD,initBeliefClosure)
		
		
		DecisionDiagramContext rewCtxt = new DecisionDiagramContext();
		rewCtxt.getVariableSpace().addVariables(states)
		rewFnDD = new AlgebraicDecisionDiagram(rewCtxt)
		populateDD(rewFnDD,rewFnClosure)
		
		assert transFnStates.size() == transFnClosures.size()
		
		transFn = populateFn(transFnClosures,transFnStates)
		obsFn = populateFn(obsFnClosures,obsFnVars)
	}
	
	public writeOut(Writer out) {
			out.writeLine(obs.collect{"${it.name}:${it.numValues}"}.join(","))
			out.writeLine(act.collect{"${it.name}:${it.numValues}"}.join(","))
			out.writeLine(states.collect{"${it.name}:${it.numValues}"}.join(","))
			out.writeLine("")
			
			transFn.getDDs().each{ CondProbADD dd ->
				CondProbDDContext cpCtxt = dd.context
				out.writeLine(cpCtxt.inputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				out.writeLine(cpCtxt.outputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				dd.rules.each{DecisionRule r ->
					out.writeLine(r.toString())
				}
				out.writeLine("+")
			}
			
			out.writeLine("")
			
			obsFn.getDDs().each{ CondProbADD dd ->
				CondProbDDContext cpCtxt = dd.context
				out.writeLine(cpCtxt.inputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				out.writeLine(cpCtxt.outputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				dd.rules.each{DecisionRule r ->
					out.writeLine(r.toString())
				}
				out.writeLine("+")
			}
			
			out.writeLine("")
			
			out.writeLine(initBeliefDD.context.variableSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
			initBeliefDD.rules.each{ DecisionRule r->
				out.writeLine(r.toString())
			}
			
			out.writeLine("")
			out.writeLine(rewFnDD.context.variableSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
			rewFnDD.rules.each{ DecisionRule r->
				out.writeLine(r.toString())
			}
	}
	
	public static readIn(Reader rdr) {
		POMDP p = new POMDP()
		p.obs = rdr.readLine()?.trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))}
		p.act = rdr.readLine()?.trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))}
		p.states = rdr.readLine()?.trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))}
		
		rdr.readLine()
		
		
		println "Reading transition function..."
		p.transFn = new DDTransitionFunction()

		String separator = rdr.readLine().trim()
		
		while(separator!="+" && separator.length()>0) {
			DDVariableSpace inVarSpace = new DDVariableSpace();
			inVarSpace.addVariables(separator.trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
			
			DDVariableSpace outVarSpace = new DDVariableSpace();
			outVarSpace.addVariables(rdr.readLine().trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
			
			CondProbDDContext transCtx = new CondProbDDContext(inVarSpace,outVarSpace)
			CondProbADD currDD = new CondProbADD(transCtx)
			
			while(separator!="+") {
				separator=rdr.readLine().trim()
				
				if(separator!="+")
					currDD.rules.add(new DecisionRule(separator))
			}
			
			p.transFn.appendDD(currDD)
			
			separator = rdr.readLine().trim()
			
			if(separator.length()==0)
				break;
		}
		
		separator = rdr.readLine().trim()
		
		println "Reading observation function..."
		p.obsFn = new DDTransitionFunction()
		while(separator!="+" && separator.length()>0) {
			println "separator:" + separator
			DDVariableSpace inVarSpace = new DDVariableSpace();
			inVarSpace.addVariables(separator.trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
			
			DDVariableSpace outVarSpace = new DDVariableSpace();
			outVarSpace.addVariables(rdr.readLine().trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
			
			CondProbDDContext transCtx = new CondProbDDContext(inVarSpace,outVarSpace)
			CondProbADD currDD = new CondProbADD(transCtx)
			
			while(separator!="+" && separator.length()>0) {
				separator=rdr.readLine().trim()
				
				if(separator!="+" && separator.length()>0)
					currDD.rules.add(new DecisionRule(separator))
			}
			
			p.obsFn.appendDD(currDD)
			
			separator = rdr.readLine().trim()
			
			if(separator.length()==0)
				break;
		}
		
		println "Reading initial belief...."
		DecisionDiagramContext beliefCtxt = new DecisionDiagramContext();
		beliefCtxt.getVariableSpace().addVariables(rdr.readLine().trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
		
		p.initBeliefDD = new AlgebraicDecisionDiagram(beliefCtxt);
		
		separator = rdr.readLine().trim()
		while(separator?.length()>0) {
			p.initBeliefDD.rules.add(new DecisionRule(separator))
			separator = rdr.readLine()?.trim()
		}
		
		println "Reading reward function..."
		DecisionDiagramContext rewCtxt = new DecisionDiagramContext();
		rewCtxt.getVariableSpace().addVariables(rdr.readLine().trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
		p.rewFnDD = new AlgebraicDecisionDiagram(rewCtxt)
		
		separator = rdr.readLine().trim()
		while(separator?.length()>0) {
			p.rewFnDD.rules.add(new DecisionRule(separator))
			separator = rdr.readLine()?.trim()
		}
		
		return p
	}
	
	private DDTransitionFunction populateFn(List<Closure<Double>> fnClosures, List<List<DDVariable>> fnVars) {
		DDTransitionFunction fn = new DDTransitionFunction()

		fnClosures.eachWithIndex { Closure<Double> fnClosure, fnIx ->
			DDVariableSpace inVarSpace = new DDVariableSpace();
			inVarSpace.addVariables(fnVars[fnIx][0])
			
			DDVariableSpace outVarSpace = new DDVariableSpace();
			outVarSpace.addVariables(fnVars[fnIx][1])
			
			CondProbDDContext transCtx = new CondProbDDContext(inVarSpace,outVarSpace)
			CondProbADD currDD = new CondProbADD(transCtx)
			
			populateDD(currDD,fnClosure)
			
			fn.appendDD(currDD);
			
		}
		
		return fn
	}
	
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
				
			}
			
			println "Removing unused negative space..."
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
						if(dd.getRules().remove(r)) {
							numRules--
						}
					}
				}
			}
			
			dd.addRules(new ArrayList(rules))
			println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
		}
		
		if(dd.getRules().size()<10) {
			dd.getRules().each{ DecisionRule r ->
				println r
			}
		}
		println()
		
	}
	
	public final DDTransitionFunction getTransFns() {
		return transFn;
	}
	
	public final DDTransitionFunction getObsFns() {
		return obsFn;
	}
	
	public final List<DDVariable> getActions() {
		return act;
	}
}
