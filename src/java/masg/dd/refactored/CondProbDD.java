package masg.dd.refactored;

import groovy.lang.Closure;

import java.util.ArrayList;

import masg.dd.rules.refactored.HierarchicalDecisionRuleCollection;
import masg.dd.rules.refactored.ImmutableHierarchicalDecisionRuleCollection;
import masg.dd.vars.DDVariable;

public class CondProbDD extends AlgebraicDD {
	public CondProbDD(ArrayList<DDVariable> conditionalVars, ArrayList<DDVariable> vars, Closure<Double>... c) {
		vars.removeAll(conditionalVars);
		
		//Conditional variables have to come first
		ArrayList<DDVariable> allVariables = new ArrayList<DDVariable>(conditionalVars);
		allVariables.addAll(vars);
		
		HierarchicalDecisionRuleCollection rulesTemp = HierarchicalDecisionRuleCollectionBuilder.buildProbability(allVariables, c);
		
		//This is only a measure given conditional variables are set
		rulesTemp.setIsMeasure(conditionalVars, false);
		
		ruleCollection = new ImmutableHierarchicalDecisionRuleCollection(rulesTemp);
	}
	
	static public CondProbDD build(ArrayList<DDVariable> conditionalVars, ArrayList<DDVariable> vars, Closure<Double>... c) {
		return new CondProbDD(conditionalVars,vars,c);
	}
}
