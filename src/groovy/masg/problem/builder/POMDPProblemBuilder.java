package masg.problem.builder;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.context.DDContext;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;

public class POMDPProblemBuilder extends AbstractMDPProblemBuilder {
	
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
		
		FactoredCondProbDD tFn = new FactoredCondProbDD(tFnParameters,tFunctions);
		FactoredCondProbDD oFn = new FactoredCondProbDD(oFnParameters,oFunctions);
		AlgebraicDD rFn = new AlgebraicDD(rFnParameters,rFunction,false);
		
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(stateVariables.values());
		
		ArrayList<CondProbDD> beliefFns = new ArrayList<CondProbDD>();
		beliefFns.add(new ProbDD(vars, initialBeliefFn));
		
		FactoredCondProbDD initBelief = new FactoredCondProbDD(beliefFns);
		
		return new POMDP(initBelief,rFn,tFn,oFn,vars,new ArrayList<DDVariable>(obsVariables.values()),new ArrayList<DDVariable>(actVariables.values()));
	}
}