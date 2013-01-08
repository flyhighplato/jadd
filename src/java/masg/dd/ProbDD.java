package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;

import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public class ProbDD extends CondProbDD {

	public ProbDD(DDVariableSpace vars, Closure<Double> c) {
		super(new DDVariableSpace(new ArrayList<DDVariable>()), vars, c);
	}
	
	protected ProbDD(AlgebraicDD fn) {
		super(new DDVariableSpace(new ArrayList<DDVariable>()), fn.getVariables(), fn);
	}

}
