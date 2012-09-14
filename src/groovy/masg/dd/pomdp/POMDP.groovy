package masg.dd.pomdp

import masg.dd.AlgebraicDecisionDiagram
import masg.dd.AlgebraicDecisionDiagramBuilder
import masg.dd.CondProbADDBuilder
import masg.dd.CondProbFunctionBuilder
import masg.dd.DecisionDiagramContext
import masg.dd.DecisionRule
import masg.dd.DecisionRuleCollection
import masg.dd.cpt.CondProbADD
import masg.dd.cpt.CondProbDDContext
import masg.dd.function.CondProbFunction

import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace

class POMDP{
	protected List<DDVariable> obs;
	protected List<DDVariable> act
	protected List<DDVariable> states
	
	protected CondProbFunction transFn;
	protected HashMap<HashMap<DDVariable,Integer>, CondProbFunction> fixedTransFns = new HashMap<DDVariable, CondProbFunction>();
	
	protected CondProbFunction obsFn;
	protected HashMap<HashMap<DDVariable,Integer>, CondProbFunction> fixedObsFns = new HashMap<DDVariable, CondProbFunction>();
	
	protected AlgebraicDecisionDiagram rewFnDD;
	protected AlgebraicDecisionDiagram initBeliefDD;
	
	protected CondProbFunction currBeliefFn;
	
	private POMDP() {
	}
	
	public POMDP(List<DDVariable> Obs, List<DDVariable> S, List<DDVariable> A, AlgebraicDecisionDiagram initBelief, AlgebraicDecisionDiagram R, CondProbFunction T, CondProbFunction O) {
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
		
		CondProbFunctionBuilder transBuilder = new CondProbFunctionBuilder()
		transFnClosures.eachWithIndex { Closure<Double> fnClosure, fnIx ->
			transBuilder.add(transFnStates[fnIx][0],transFnStates[fnIx][1],fnClosure)
		}
		transFn = transBuilder.build()
		
		
		
		CondProbFunctionBuilder obsBuilder = new CondProbFunctionBuilder()
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
		p.transFn = new CondProbFunction()

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
		p.obsFn = new CondProbFunction()
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
	
	public final CondProbFunction getTransFns() {
		return transFn;
	}
	
	public final CondProbFunction getObsFns() {
		return obsFn;
	}
	
	public final List<DDVariable> getActions() {
		return act;
	}
	
	public final AlgebraicDecisionDiagram getInitialtBelief() {
		return initBeliefDD;
	}
	
	public final CondProbFunction getCurrentBelief() {
		return currBeliefFn;
	}
	
	public void updateBelief(HashMap<DDVariable,Integer> acts, HashMap<DDVariable,Integer> obs) {
		currBeliefFn = updateBelief(this,currBeliefFn?currBeliefFn:initBeliefDD,acts,obs)
	}
	
	static public CondProbFunction updateBelief(POMDP p, def belief, HashMap<DDVariable,Integer> acts, HashMap<DDVariable,Integer> obs) {
		HashMap<DDVariable, Integer> fixAt = [:]
		acts.each{ DDVariable a, Integer val->
			fixAt[a]=val
		}
		obs.each{ DDVariable o, Integer val->
			fixAt[o]=val
		}
		
		CondProbFunction fixedObsFn
		if(p.fixedObsFns.containsKey(fixAt)) 
			fixedObsFn = p.fixedObsFns[fixAt]
		else {
			fixedObsFn = p.obsFns.restrict(fixAt);
			p.fixedObsFns[fixAt] = fixedObsFn
		}
		
		CondProbFunction fixedTransFn
		if(p.fixedTransFns.containsKey(fixAt))
			p.fixedTransFn = p.fixedObsFns[fixAt]
		else {
			fixedTransFn = p.transFns.restrict(fixAt);
			p.fixedObsFns[fixAt] = fixedTransFn
		}
		
		CondProbFunction temp = fixedTransFn.multiply(belief)
		
			
		temp = temp.sumOut(p.states,false)
		CondProbFunction currBeliefFn = temp.multiply(fixedObsFn)
		currBeliefFn.normalize()
		currBeliefFn.unprimeAllContexts();
		
		return currBeliefFn;
	}
}
