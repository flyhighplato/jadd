package masg.problem.builder;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.context.DDContext;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;

public class JointPOMDPProblemBuilder extends AbstractMDPProblemBuilder {
	protected HashMap<String,DDVariable> actOtherVariables = new HashMap<String,DDVariable>();
	
	void addActionOther(String name, int numValues) throws Exception {
		DDVariable v = new DDVariable(scope, name,numValues);
		if(varExists(v)) {
			throw new Exception("Variable " + v + " already exists");
		}
		actOtherVariables.put(name, v);
	}
	
	boolean varExists(DDVariable v) {
		return super.varExists(v) || actOtherVariables.containsValue(v);
	}
	
	protected ArrayList<DDVariable> toActionOtherVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,actOtherVariables);
		} catch (Exception e) {
			throw new Exception("One or more action variables are undefined");
		}
		
		return varsRes;
	}
	
	void setRewardFunction(List<String> stateVars, List<String> actVars, List<String> actOtherVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(toStateVariableList(stateVars));
		vars.addAll(toActionVariableList(actVars));
		vars.addAll(toActionOtherVariableList(actOtherVars));
		
		rFnParameters = vars;
		rFunction = c;
	}
	
	void addTransition(List<String> stateVars, List<String> actVars, List<String> actOtherVars, List<String> statePrimeVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> varsConditional = new ArrayList<DDVariable>(toStateVariableList(stateVars));
		varsConditional.addAll(toActionVariableList(actVars));
		varsConditional.addAll(toActionOtherVariableList(actOtherVars));
		
		ArrayList<DDVariable> varsDependent = new ArrayList<DDVariable>(toStatePrimeVariableList(statePrimeVars));
		
		ArrayList<ArrayList<DDVariable>> fnParameters = new ArrayList<ArrayList<DDVariable>>();
		fnParameters.add(varsConditional);
		fnParameters.add(varsDependent);
		
		tFnParameters.add(fnParameters);
		tFunctions.add(c);
	}
	
	void addObservation(List<String> statePrimeVars, List<String> actVars, List<String> actOtherVars, List<String> obsVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> varsConditional = new ArrayList<DDVariable>(toStatePrimeVariableList(statePrimeVars));
		varsConditional.addAll(toActionVariableList(actVars));
		varsConditional.addAll(toActionOtherVariableList(actOtherVars));
		
		ArrayList<DDVariable> varsDependent = new ArrayList<DDVariable>(toObservationVariableList(obsVars));
		
		ArrayList<ArrayList<DDVariable>> fnParameters = new ArrayList<ArrayList<DDVariable>>();
		fnParameters.add(varsConditional);
		fnParameters.add(varsDependent);
		
		oFnParameters.add(fnParameters);
		oFunctions.add(c);
	}
	
	POMDP buildPOMDP() {
		
		ArrayList<DDVariable> canVars = new ArrayList<DDVariable>(actVariables.values());
		canVars.addAll(actOtherVariables.values());
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
		
		return new POMDP(initBelief,rFn,tFn,oFn,vars,new ArrayList<DDVariable>(obsVariables.values()),new ArrayList<DDVariable>(actVariables.values()));
	}
}
