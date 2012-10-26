package masg.dd.refactored;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import masg.dd.rules.refactored.MutableDDElement;
import masg.dd.vars.DDVariable;

public class ProbDD {

	protected AlgebraicDD dd;
	protected ArrayList<DDVariable> variables;
	public ProbDD(ArrayList<DDVariable> vars, Closure<Double>... c) {
		variables = new ArrayList<DDVariable>(vars);
		MutableDDElement rulesTemp = MutableDDElementBuilder.buildProbability(vars, c);
		dd = new AlgebraicDD(rulesTemp);
	}
	
	protected ProbDD() {
		
	}
	
	public CondProbDD multiply(CondProbDD condProbDD) {
		return condProbDD.multiply(this);
	}
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		return getDD().getValue(varSpacePoint);
	}
	public final AlgebraicDD getDD() {
		return dd;
	}
	
	public final ArrayList<DDVariable> getVariables() {
		return variables;
	}
	
	static public ProbDD buildProbability(ArrayList<DDVariable> vars, Closure<Double>... c) {
		return new ProbDD(vars,c);
	}

}
