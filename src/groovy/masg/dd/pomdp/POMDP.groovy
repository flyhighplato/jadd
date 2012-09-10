package masg.dd.pomdp

import masg.dd.AlgebraicDecisionDiagram
import masg.dd.AlgebraicDecisionDiagramBuilder
import masg.dd.CondProbADDBuilder
import masg.dd.DDTransitionFunctionBuilder
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
	protected HashMap<HashMap<DDVariable,Integer>, DDTransitionFunction> fixedTransFns = new HashMap<DDVariable, DDTransitionFunction>();
	
	protected DDTransitionFunction obsFn;
	protected HashMap<HashMap<DDVariable,Integer>, DDTransitionFunction> fixedObsFns = new HashMap<DDVariable, DDTransitionFunction>();
	
	protected AlgebraicDecisionDiagram rewFnDD;
	protected AlgebraicDecisionDiagram initBeliefDD;
	
	protected DDTransitionFunction currBeliefFn;
	
	private POMDP() {
	}
	
	public POMDP(List<DDVariable> Obs, List<DDVariable> S, List<DDVariable> A, AlgebraicDecisionDiagram initBelief, AlgebraicDecisionDiagram R, DDTransitionFunction T, DDTransitionFunction O) {
		obs = Obs
		states = S
		act = A
		initBeliefDD = initBelief
		rewFnDD = R
		transFn = T
		obsFn = O
		
		initFixed()
	}
	
	public POMDP(List<DDVariable> obs, List<DDVariable> act, List<DDVariable> states, Closure<Double> initBeliefClosure, List<List<DDVariable>> transFnStates, List<Closure<Double>> transFnClosures, List<List<DDVariable>> obsFnVars, List<Closure<Double>> obsFnClosures, Closure<Double> rewFnClosure) {
		this.obs = obs
		this.act = act
		this.states = states
		
		initBeliefDD = AlgebraicDecisionDiagramBuilder.build(states,initBeliefClosure)
		rewFnDD = AlgebraicDecisionDiagramBuilder.build(states,rewFnClosure)
		
		assert transFnStates.size() == transFnClosures.size()
		
		DDTransitionFunctionBuilder transBuilder = new DDTransitionFunctionBuilder()
		transFnClosures.eachWithIndex { Closure<Double> fnClosure, fnIx ->
			transBuilder.add(transFnStates[fnIx][0],transFnStates[fnIx][1],fnClosure)
		}
		transFn = transBuilder.build()
		
		
		
		DDTransitionFunctionBuilder obsBuilder = new DDTransitionFunctionBuilder()
		obsFnClosures.eachWithIndex { Closure<Double> fnClosure, fnIx ->
			obsBuilder.add(obsFnVars[fnIx][0],obsFnVars[fnIx][1],fnClosure)
		}
		obsFn = obsBuilder.build()
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
	
	public final DDTransitionFunction getTransFns() {
		return transFn;
	}
	
	public final DDTransitionFunction getObsFns() {
		return obsFn;
	}
	
	public final List<DDVariable> getActions() {
		return act;
	}
	
	public final DDTransitionFunction getCurrentBelief() {
		return currBeliefFn;
	}
	
	public void updateBelief(HashMap<DDVariable,Integer> acts, HashMap<DDVariable,Integer> obs) {
		HashMap<DDVariable, Integer> fixAt = [:]
		acts.each{ DDVariable a, Integer val->
			fixAt[a]=val
		}
		obs.each{ DDVariable o, Integer val->
			fixAt[o]=val
		}
		
		DDTransitionFunction fixedObsFn
		if(fixedObsFns.containsKey(fixAt)) 
			fixedObsFn = fixedObsFns[fixAt]
		else {
			fixedObsFn = obsFns.restrict(fixAt);
			fixedObsFns[fixAt] = fixedObsFn
		}
		
		DDTransitionFunction fixedTransFn
		if(fixedTransFns.containsKey(fixAt))
			fixedTransFn = fixedObsFns[fixAt]
		else {
			fixedTransFn = transFns.restrict(fixAt);
			fixedObsFns[fixAt] = fixedTransFn
		}
		
		DDTransitionFunction temp
		if(currBeliefFn == null)
			temp = fixedTransFn.multiply(initBeliefDD)
		else
			temp = fixedTransFn.multiply(currBeliefFn)
			
		temp = temp.sumOut(states,false)
		currBeliefFn = temp.multiply(fixedObsFn)
		currBeliefFn.normalize()
		currBeliefFn.unprimeAllContexts();
	}
}
