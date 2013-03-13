package masg.problem.builder;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.context.DDContext;
import masg.dd.pomdp.IPOMDP;
import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.policy.Policy;
import masg.dd.variables.DDVariable;

public class IPOMDPProblemBuilder extends AbstractMDPProblemBuilder {
	protected HashMap<String, POMDPAgentType> otherAgents = new HashMap<String, POMDPAgentType>();
	
	void setRewardFunction(List<String> stateVars, List<String> actVars, Map<String,List<String>> actOtherVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(toStateVariableList(stateVars));
		vars.addAll(toActionVariableList(actVars));
		
		for(Entry<String, List<String>> e:actOtherVars.entrySet()) {
			vars.addAll( otherAgents.get(e.getKey()).builder.toActionVariableList(e.getValue()) ) ;
		}
		
		rFnParameters = vars;
		rFunction = c;
	}
	
	protected HashSet<Integer> usedScopes = new HashSet<Integer>();
	void addAgentType(String name, POMDPProblemBuilder builder, Policy pol) throws Exception {
		
		if(builder.getScope()==0 || usedScopes.contains(builder.getScope()) ) {
			throw new Exception("Scope conflict between agent definitions.  Scope #" + builder.getScope() + " already defined.");
		}
		
		usedScopes.add(builder.getScope());
		otherAgents.put(name.toString(), new POMDPAgentType(builder, pol));
	}
	
	void addTransitionFunction(List<String> stateVars, List<String> actVars, Map<String,List<String>> actOtherVars, List<String> statePrimeVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> varsConditional = new ArrayList<DDVariable>(toStateVariableList(stateVars));
		varsConditional.addAll(toActionVariableList(actVars));
		
		for(Entry<String, List<String>> e:actOtherVars.entrySet()) {
			String key = e.getKey().toString();
			POMDPAgentType agentOther = otherAgents.get(key);
			POMDPProblemBuilder builder = agentOther.builder;
			
			varsConditional.addAll( builder.toActionVariableList(e.getValue()) ) ;
		}
		
		ArrayList<DDVariable> varsDependent = new ArrayList<DDVariable>(toStatePrimeVariableList(statePrimeVars));
		
		ArrayList<ArrayList<DDVariable>> fnParameters = new ArrayList<ArrayList<DDVariable>>();
		fnParameters.add(varsConditional);
		fnParameters.add(varsDependent);
		
		tFnParameters.add(fnParameters);
		tFunctions.add(c);
	}
	
	void addObservationFunction(List<String> statePrimeVars, List<String> actVars, Map<String,List<String>> actOtherVars, List<String> obsVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> varsConditional = new ArrayList<DDVariable>(toStatePrimeVariableList(statePrimeVars));
		varsConditional.addAll(toActionVariableList(actVars));
		
		for(Entry<String, List<String>> e:actOtherVars.entrySet()) {
			varsConditional.addAll( otherAgents.get(e.getKey()).builder.toActionVariableList(e.getValue()) ) ;
		}
		
		ArrayList<DDVariable> varsDependent = new ArrayList<DDVariable>(toObservationVariableList(obsVars));
		
		ArrayList<ArrayList<DDVariable>> fnParameters = new ArrayList<ArrayList<DDVariable>>();
		fnParameters.add(varsConditional);
		fnParameters.add(varsDependent);
		
		oFnParameters.add(fnParameters);
		oFunctions.add(c);
	}
	
	IPOMDP buildIPOMDP() {
		ArrayList<POMDP> otherAgentPOMDPs = new ArrayList<POMDP>();
		
		
		for(Entry<String, POMDPAgentType> e1: otherAgents.entrySet() ) {
			otherAgentPOMDPs.add(e1.getValue().builder.buildPOMDP());
		}
		
		ArrayList<DDVariable> canVars = new ArrayList<DDVariable>(actVariables.values());
		
		for(Entry<String, POMDPAgentType> e1: otherAgents.entrySet() ) {
			
			canVars.addAll(e1.getValue().builder.obsVariables.values());
			canVars.addAll(e1.getValue().builder.stateVariables.values());
			canVars.addAll(e1.getValue().builder.statePrimeVariables.values());
			canVars.addAll(e1.getValue().builder.actVariables.values());
			
		}
		
		canVars.addAll(obsVariables.values());
		canVars.addAll(stateVariables.values());
		canVars.addAll(statePrimeVariables.values());
		
		DDContext.setCanonicalVariableOrdering(canVars);
		
		FactoredCondProbDD tFn = new FactoredCondProbDD(tFnParameters,scope,tFunctions);
		FactoredCondProbDD oFn = new FactoredCondProbDD(oFnParameters,scope,oFunctions);
		AlgebraicDD rFn = new AlgebraicDD(rFnParameters,scope,rFunction,false);
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(stateVariables.values());
		
		ArrayList<CondProbDD> beliefFns = new ArrayList<CondProbDD>();
		beliefFns.add(new ProbDD(vars, scope, initialBeliefFn));
		
		FactoredCondProbDD initBelief = new FactoredCondProbDD(beliefFns);
		
		return new IPOMDP(initBelief,rFn,tFn,oFn,vars,new ArrayList<DDVariable>(obsVariables.values()),new ArrayList<DDVariable>(actVariables.values()),otherAgentPOMDPs);
	}
}
