package masg.problem;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.ProbDD;
import masg.dd.context.DDContext;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;

public class POMDPProblemBuilder {
	private HashMap<String,DDVariable> stateVariables = new HashMap<String,DDVariable>();
	private HashMap<String,DDVariable> statePrimeVariables = new HashMap<String,DDVariable>();
	private HashMap<String,DDVariable> obsVariables = new HashMap<String,DDVariable>();
	private HashMap<String,DDVariable> actVariables = new HashMap<String,DDVariable>();
	
	private ArrayList<ArrayList<ArrayList<DDVariable>>> tFnParameters = new ArrayList<ArrayList<ArrayList<DDVariable>>>();
	private ArrayList<Closure<Double>> tFunctions = new ArrayList<Closure<Double>>();
	
	private ArrayList<ArrayList<ArrayList<DDVariable>>> oFnParameters = new ArrayList<ArrayList<ArrayList<DDVariable>>>();
	private ArrayList<Closure<Double>> oFunctions = new ArrayList<Closure<Double>>();
	
	private Closure<Double> initialBeliefFn = null;
	
	private ArrayList<DDVariable> rFnParameters = new ArrayList<DDVariable>();
	private Closure<Double> rFunction = null;
	
	boolean varExists(DDVariable v) {
		return stateVariables.containsValue(v) || obsVariables.containsValue(v) || actVariables.containsValue(v);
	}
	void addState(String name, int numValues) throws Exception {
		DDVariable v = new DDVariable(name,numValues);
		if(varExists(v)) {
			throw new Exception("Variable " + v + " already exists");
		}
		stateVariables.put(name, v);
		statePrimeVariables.put(v.getPrimed().getName(), v.getPrimed());
	}
	
	void addObservation(String name, int numValues) throws Exception {
		DDVariable v = new DDVariable(name,numValues);
		if(varExists(v)) {
			throw new Exception("Variable " + v + " already exists");
		}
		obsVariables.put(name, v);
	}
	
	void addAction(String name, int numValues) throws Exception {
		DDVariable v = new DDVariable(name,numValues);
		if(varExists(v)) {
			throw new Exception("Variable " + v + " already exists");
		}
		actVariables.put(name, v);
	}
	
	private ArrayList<DDVariable> toVariableList(List<String> varNames, HashMap<String,DDVariable> varDefs) throws Exception {
		ArrayList<DDVariable> varsRes = new ArrayList<DDVariable>();
		
		for(String s:varNames) {
			if(!varDefs.containsKey(s)) {
				throw new Exception("Variable " + s + " is undefined");
			}
			varsRes.add(varDefs.get(s));
		}
		
		return varsRes;
	}
	
	private ArrayList<DDVariable> toStateVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,stateVariables);
		} catch (Exception e) {
			throw new Exception("One or more state variables are undefined");
		}
		
		return varsRes;
	}
	
	private ArrayList<DDVariable> toStatePrimeVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,statePrimeVariables);
		} catch (Exception e) {
			throw new Exception("One or more state prime variables are undefined");
		}
		
		return varsRes;
	}
	
	private ArrayList<DDVariable> toObservationVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,obsVariables);
		} catch (Exception e) {
			throw new Exception("One or more observation variables are undefined");
		}
		
		return varsRes;
	}
	
	private ArrayList<DDVariable> toActionVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,actVariables);
		} catch (Exception e) {
			throw new Exception("One or more action variables are undefined");
		}
		
		return varsRes;
	}
	
	void setInitialBelief(Closure<Double> c) throws Exception {
		initialBeliefFn = c;
	}
	
	void setRewardFunction(List<String> stateVars, List<String> actVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(toStateVariableList(stateVars));
		vars.addAll(toActionVariableList(actVars));
		
		rFnParameters = vars;
		rFunction = c;
	}
	
	void addTransition(List<String> stateVars, List<String> actVars, List<String> statePrimeVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> varsConditional = new ArrayList<DDVariable>(toStateVariableList(stateVars));
		varsConditional.addAll(toActionVariableList(actVars));
		
		ArrayList<DDVariable> varsDependent = new ArrayList<DDVariable>(toStatePrimeVariableList(statePrimeVars));
		
		ArrayList<ArrayList<DDVariable>> fnParameters = new ArrayList<ArrayList<DDVariable>>();
		fnParameters.add(varsConditional);
		fnParameters.add(varsDependent);
		
		tFnParameters.add(fnParameters);
		tFunctions.add(c);
	}
	
	void addObservation(List<String> statePrimeVars, List<String> actVars, List<String> obsVars, Closure<Double> c) throws Exception {
		ArrayList<DDVariable> varsConditional = new ArrayList<DDVariable>(toStatePrimeVariableList(statePrimeVars));
		varsConditional.addAll(toActionVariableList(actVars));
		
		ArrayList<DDVariable> varsDependent = new ArrayList<DDVariable>(toObservationVariableList(obsVars));
		
		ArrayList<ArrayList<DDVariable>> fnParameters = new ArrayList<ArrayList<DDVariable>>();
		fnParameters.add(varsConditional);
		fnParameters.add(varsDependent);
		
		oFnParameters.add(fnParameters);
		oFunctions.add(c);
	}
	
	POMDP buildPOMDP() {
		
		ArrayList<DDVariable> canVars = new ArrayList<DDVariable>(actVariables.values());
		canVars.addAll(obsVariables.values());
		canVars.addAll(stateVariables.values());
		canVars.addAll(statePrimeVariables.values());
		
		DDContext.canonicalVariableOrdering = canVars;
		
		CondProbDD tFn = new CondProbDD(tFnParameters,tFunctions);
		CondProbDD oFn = new CondProbDD(oFnParameters,oFunctions);
		AlgebraicDD rFn = new AlgebraicDD(rFnParameters,rFunction,false);
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(stateVariables.values());
		ProbDD initBelief = new ProbDD(vars, initialBeliefFn);
		
				
		return new POMDP(initBelief,rFn,tFn,oFn,vars,new ArrayList<DDVariable>(obsVariables.values()),new ArrayList<DDVariable>(actVariables.values()));
	}
}
