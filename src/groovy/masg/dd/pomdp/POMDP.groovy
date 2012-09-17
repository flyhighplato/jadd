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

import masg.agent.pomdp.POMDPUtils

class POMDP{
	protected List<DDVariable> obs;
	protected List<DDVariable> act
	protected List<DDVariable> states
	
	protected CondProbFunction transFn;
	protected CondProbFunction obsFn;
	
	protected CondProbFunction rewFn;
	protected CondProbFunction initBeliefFn;
	
	protected CondProbFunction currBeliefFn;
	
	private POMDP() {
	}
	
	public POMDP(List<DDVariable> Obs, List<DDVariable> S, List<DDVariable> A, CondProbFunction initBelief, CondProbFunction R, CondProbFunction T, CondProbFunction O) {
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
		
		CondProbFunctionBuilder beliefBuilder = new CondProbFunctionBuilder()
		beliefBuilder.add([], states,initBeliefClosure)
		initBeliefFn = beliefBuilder.build()
		
		CondProbFunctionBuilder rewFnBuilder = new CondProbFunctionBuilder()
		rewFnBuilder.add([], states,rewFnClosure)
		rewFn = rewFnBuilder.build()
		
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
			
			initBeliefFn.getDDs().each{ CondProbADD dd ->
				CondProbDDContext cpCtxt = dd.context
				out.writeLine(cpCtxt.inputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				out.writeLine(cpCtxt.outputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				dd.rules.each{DecisionRule r ->
					out.writeLine(r.toString())
				}
				out.writeLine("+")
			}
			
			out.writeLine("")
			
			rewFn.getDDs().each{ CondProbADD dd ->
				CondProbDDContext cpCtxt = dd.context
				out.writeLine(cpCtxt.inputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				out.writeLine(cpCtxt.outputVarSpace.variables.collect{"${it.name}:${it.numValues}"}.join(","))
				dd.rules.each{DecisionRule r ->
					out.writeLine(r.toString())
				}
				out.writeLine("+")
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
		
		separator = rdr.readLine().trim()
		separator = rdr.readLine().trim()
		
		println "Reading initial belief function..."
		p.initBeliefFn = new CondProbFunction()
		while(separator!="+" && separator.length()>0) {
			println "separator:" + separator
			DDVariableSpace inVarSpace = new DDVariableSpace();
			
			DDVariableSpace outVarSpace = new DDVariableSpace();
			outVarSpace.addVariables(separator.trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
			
			CondProbDDContext transCtx = new CondProbDDContext(inVarSpace,outVarSpace)
			CondProbADD currDD = new CondProbADD(transCtx)
			
			while(separator!="+" && separator.length()>0) {
				separator=rdr.readLine().trim()
				
				if(separator!="+" && separator.length()>0)
					currDD.rules.add(new DecisionRule(separator))
			}
			
			p.initBeliefFn.appendDD(currDD)
			
			separator = rdr.readLine().trim()
			
			if(separator.length()==0)
				break;
		}
		
		println "Reading reward function..."
		separator = rdr.readLine().trim()
		separator = rdr.readLine().trim()
		
		p.rewFn = new CondProbFunction()
		while(separator!="+" && separator.length()>0) {
			println "separator:" + separator
			DDVariableSpace inVarSpace = new DDVariableSpace();
			
			DDVariableSpace outVarSpace = new DDVariableSpace();
			outVarSpace.addVariables(separator.trim().split(",").collect{new DDVariable(it.split(":")[0], Integer.parseInt(it.split(":")[1]))})
			
			CondProbDDContext transCtx = new CondProbDDContext(inVarSpace,outVarSpace)
			CondProbADD currDD = new CondProbADD(transCtx)
			
			while(separator!="+" && separator.length()>0) {
				separator=rdr.readLine().trim()
				
				if(separator!="+" && separator.length()>0)
					currDD.rules.add(new DecisionRule(separator))
				
			}
			
			p.rewFn.appendDD(currDD)
			
			separator = rdr.readLine()?.trim()
			
			if(!separator || separator.length()==0)
				break;
		}
		return p
	}
	
	public final CondProbFunction getTransFn() {
		return transFn;
	}
	
	public final CondProbFunction getObsFns() {
		return obsFn;
	}
	
	public final List<DDVariable> getActions() {
		return act;
	}
	
	public final List<DDVariable> getStates() {
		return states;
	}
	
	public final List<DDVariable> getObservations() {
		return obs;
	}
	
	public final CondProbFunction getInitialtBelief() {
		return initBeliefFn;
	}
	
	public final CondProbFunction getRewardFn() {
		return rewFn;
	}
	
	
	public final CondProbFunction getCurrentBelief() {
		return currBeliefFn;
	}
	
	public void updateBelief(HashMap<DDVariable,Integer> acts, HashMap<DDVariable,Integer> obs) {
		currBeliefFn = POMDPUtils.updateBelief(this, currBeliefFn?currBeliefFn:initBeliefFn ,acts,obs)
	}
}
