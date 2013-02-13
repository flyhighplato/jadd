package masg.problem.builder;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import masg.dd.variables.DDVariable;

public abstract class AbstractMDPProblemBuilder {
	protected HashMap<String,DDVariable> stateVariables = new HashMap<String,DDVariable>();
	protected HashMap<String,DDVariable> statePrimeVariables = new HashMap<String,DDVariable>();
	protected HashMap<String,DDVariable> obsVariables = new HashMap<String,DDVariable>();
	protected HashMap<String,DDVariable> actVariables = new HashMap<String,DDVariable>();
	
	protected ArrayList<ArrayList<ArrayList<DDVariable>>> tFnParameters = new ArrayList<ArrayList<ArrayList<DDVariable>>>();
	protected ArrayList<Closure<Double>> tFunctions = new ArrayList<Closure<Double>>();
	
	protected ArrayList<ArrayList<ArrayList<DDVariable>>> oFnParameters = new ArrayList<ArrayList<ArrayList<DDVariable>>>();
	protected ArrayList<Closure<Double>> oFunctions = new ArrayList<Closure<Double>>();
	
	protected Closure<Double> initialBeliefFn = null;
	
	protected ArrayList<DDVariable> rFnParameters = new ArrayList<DDVariable>();
	protected Closure<Double> rFunction = null;
	

	void addAction(String name, int numValues) throws Exception {
		DDVariable v = new DDVariable(name,numValues);
		if(varExists(v)) {
			throw new Exception("Variable " + v + " already exists");
		}
		actVariables.put(name, v);
	}
	
	boolean varExists(DDVariable v) {
		return stateVariables.containsValue(v) || obsVariables.containsValue(v) || actVariables.containsValue(v);
	}
	
	protected ArrayList<DDVariable> toActionVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,actVariables);
		} catch (Exception e) {
			throw new Exception("One or more action variables are undefined");
		}
		
		return varsRes;
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
	
	
	protected ArrayList<DDVariable> toVariableList(List<String> varNames, HashMap<String,DDVariable> varDefs) throws Exception {
		ArrayList<DDVariable> varsRes = new ArrayList<DDVariable>();
		
		for(Object s:varNames) {
			String varStr = s.toString();
			if(!varDefs.containsKey(varStr)) {
				throw new Exception("Variable " + s + " is undefined");
			}
			varsRes.add(varDefs.get(varStr));
		}
		
		return varsRes;
	}
	
	protected ArrayList<DDVariable> toStateVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,stateVariables);
		} catch (Exception e) {
			throw new Exception("One or more state variables are undefined", e);
		}
		
		return varsRes;
	}
	
	protected ArrayList<DDVariable> toStatePrimeVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,statePrimeVariables);
		} catch (Exception e) {
			throw new Exception("One or more state prime variables are undefined", e);
		}
		
		return varsRes;
	}
	
	protected ArrayList<DDVariable> toObservationVariableList(List<String> varNames) throws Exception {
		ArrayList<DDVariable> varsRes = null;
		
		try {
			varsRes = toVariableList(varNames,obsVariables);
		} catch (Exception e) {
			throw new Exception("One or more observation variables are undefined", e);
		}
		
		return varsRes;
	}
	
}
