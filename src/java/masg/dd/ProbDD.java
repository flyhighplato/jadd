package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;

import masg.dd.variables.DDVariable;

public class ProbDD extends CondProbDD {

	public ProbDD(ArrayList<DDVariable> vars, int defaultScopeId,  Closure<Double> c) {
		super(new ArrayList<DDVariable>(), vars, defaultScopeId, c);
	}
	
	public ProbDD(AlgebraicDD fn) {
		super(new ArrayList<DDVariable>(), fn.getVariables(), fn);
	}
	

}
