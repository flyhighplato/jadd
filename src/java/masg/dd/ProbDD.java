package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;

import masg.dd.variables.DDVariable;

public class ProbDD extends CondProbDD {

	public ProbDD(ArrayList<DDVariable> vars, Closure<Double> c) {
		super(new ArrayList<DDVariable>(), vars, c);
	}
	
	protected ProbDD(AlgebraicDD fn) {
		super(new ArrayList<DDVariable>(), fn.getVariables(), fn);
	}

}
