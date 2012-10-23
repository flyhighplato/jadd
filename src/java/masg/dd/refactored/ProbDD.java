package masg.dd.refactored;

import groovy.lang.Closure;

import java.util.ArrayList;

import masg.dd.rules.refactored.ImmutableHierarchicalDecisionRuleCollection;
import masg.dd.vars.DDVariable;

public class ProbDD extends AlgebraicDD {

	public ProbDD(ArrayList<DDVariable> vars, Closure<Double>... c) {
		super();
		ruleCollection = new ImmutableHierarchicalDecisionRuleCollection(HierarchicalDecisionRuleCollectionBuilder.buildProbability(vars, c));
	}
	
	protected ProbDD() {
		
	}
	
	static public ProbDD build(ArrayList<DDVariable> vars, Closure<Double>... c) {
		return new ProbDD(vars,c);
	}

}
