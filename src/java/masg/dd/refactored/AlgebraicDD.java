package masg.dd.refactored;

import groovy.lang.Closure;

import java.util.ArrayList;

import masg.dd.rules.refactored.ImmutableHierarchicalDecisionRuleCollection;
import masg.dd.vars.DDVariable;

public class AlgebraicDD {
	protected ImmutableHierarchicalDecisionRuleCollection ruleCollection;
	
	public AlgebraicDD(ArrayList<DDVariable> vars, Closure<Double> c) {
		ruleCollection = new ImmutableHierarchicalDecisionRuleCollection(HierarchicalDecisionRuleCollectionBuilder.build(vars, c, false));
	}
	
	protected AlgebraicDD() {
		
	}
	
	static public AlgebraicDD build(ArrayList<DDVariable> vars, Closure<Double> c) {
		return new AlgebraicDD(vars,c);
	}
}
