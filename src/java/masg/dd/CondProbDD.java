package masg.dd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.context.CondProbDDContext;
import masg.dd.context.ProbDD;
import masg.dd.rules.DecisionRule;
import masg.dd.rules.DecisionRuleCollection;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class CondProbDD extends ProbDD {

	public CondProbDD(CondProbDDContext ctx) {
		super(ctx);
	}
	
	public double getValue(HashMap<DDVariable,Integer> varValues) throws Exception {
		CondProbDDContext cpContext = (CondProbDDContext) context;
		
		
		HashSet<DDVariable> inVariables = new HashSet<DDVariable>(cpContext.getInputVarSpace().getVariables());
		inVariables.retainAll(varValues.keySet());
		
		HashSet<DDVariable> outVariables = new HashSet<DDVariable>(cpContext.getOutputVarSpace().getVariables());
		outVariables.retainAll(varValues.keySet());
		
		if(!(outVariables.size()==0 && cpContext.getOutputVarSpace().getVariables().size()!=0) ) {
			@SuppressWarnings("unchecked")
			HashSet<DDVariable> opVariables = (HashSet<DDVariable>) inVariables.clone();
			
			opVariables.addAll(outVariables);
			
			
			AlgebraicDD dd = sumOutAllExcept(opVariables);
			
			HashMap<DDVariable,Integer> opVariableValues = new HashMap<DDVariable,Integer>();
			for(DDVariable var:opVariables) {
				opVariableValues.put(var, varValues.get(var));
			}
			
			DecisionRule r = dd.getContext().getVariableSpace().generateRule(opVariableValues, 1.0f);
			return dd.getValue(r);
		}
		
		
		return 1.0f;
	}
	
	public CondProbDD restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		AlgebraicDD summedOutDD = super.restrict(varInstances);
		CondProbDDContext cpContext = (CondProbDDContext) context;
		
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> inVars = (ArrayList<DDVariable>) cpContext.getInputVarSpace().getVariables().clone();
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> outVars = (ArrayList<DDVariable>) cpContext.getOutputVarSpace().getVariables().clone();
		
		inVars.removeAll(varInstances.keySet());
		outVars.removeAll(varInstances.keySet());
		
		DDVariableSpace inVarSpace = new DDVariableSpace();
		inVarSpace.addVariables(inVars);
		
		DDVariableSpace outVarSpace = new DDVariableSpace();
		outVarSpace.addVariables(outVars);
		
		CondProbDDContext cpContextNew = new CondProbDDContext(inVarSpace,outVarSpace);
		
		CondProbDD cpDDNew = new CondProbDD(cpContextNew);
		cpDDNew.rules = summedOutDD.getRules();
		
		return cpDDNew;
	}
	
	public CondProbDD times(double value) {
		CondProbDDContext cpContext = new CondProbDDContext((CondProbDDContext) context);
		
		DecisionRuleCollection newRules = new DecisionRuleCollection(cpContext.getVariableSpace().getBitCount());

		for(DecisionRule thisRule:getRules()) {
			DecisionRule resRule = new DecisionRule(thisRule);
			resRule.value = thisRule.value * value;
			newRules.add(resRule);
		}
		
		CondProbDD cpDDNew = new CondProbDD(cpContext);
		cpDDNew.rules = newRules;
		
		return cpDDNew;
	}
	
	
	public CondProbDD max(ProbDD add) throws Exception {
		CondProbDDContext cpContext = new CondProbDDContext((CondProbDDContext) context);
		
		add = add.sumOutAllExcept(cpContext.getVariableSpace().getVariables());
		
		DecisionRuleCollection newRules = new DecisionRuleCollection(cpContext.getVariableSpace().getBitCount());
		for(DecisionRule otherRule:add.getRules()) {
			DecisionRule translatedOtherRule = cpContext.getVariableSpace().translateRule(otherRule, add.getContext().getVariableSpace());
			
			for(DecisionRule thisRule:getRules()) {
				if(thisRule.matches(translatedOtherRule)) {
					DecisionRule resRule = DecisionRule.getIntersectionBitStringRule(thisRule, translatedOtherRule);
					resRule.value = Math.max(thisRule.value,otherRule.value);
					newRules.add(resRule);
				}
			}
		}
		
		CondProbDD cpDDNew = new CondProbDD(cpContext);
		cpDDNew.rules = newRules;
		
		return cpDDNew;
	}
	
	public CondProbDD plus(ProbDD add) throws Exception {
		CondProbDDContext cpContext = new CondProbDDContext((CondProbDDContext) context);
		
		add = add.sumOutAllExcept(cpContext.getVariableSpace().getVariables());
		
		DecisionRuleCollection newRules = new DecisionRuleCollection(cpContext.getVariableSpace().getBitCount());
		for(DecisionRule otherRule:add.getRules()) {
			DecisionRule translatedOtherRule = cpContext.getVariableSpace().translateRule(otherRule, add.getContext().getVariableSpace());
			
			for(DecisionRule thisRule:getRules()) {
				if(thisRule.matches(translatedOtherRule)) {
					DecisionRule resRule = DecisionRule.getIntersectionBitStringRule(thisRule, translatedOtherRule);
					resRule.value = thisRule.value + otherRule.value;
					newRules.add(resRule);
				}
			}
		}
		
		CondProbDD cpDDNew = new CondProbDD(cpContext);
		cpDDNew.rules = newRules;
		
		return cpDDNew;
	}
	
	public CondProbDD times(ProbDD add) throws Exception {
		CondProbDDContext cpContext = new CondProbDDContext((CondProbDDContext) context);
		
		add = add.sumOutAllExcept(cpContext.getVariableSpace().getVariables());
		
		DecisionRuleCollection newRules = new DecisionRuleCollection(cpContext.getVariableSpace().getBitCount());
		for(DecisionRule otherRule:add.getRules()) {
			DecisionRule translatedOtherRule = cpContext.getVariableSpace().translateRule(otherRule, add.getContext().getVariableSpace());
			
			for(DecisionRule thisRule:getRules()) {
				if(thisRule.matches(translatedOtherRule)) {
					DecisionRule resRule = DecisionRule.getIntersectionBitStringRule(thisRule, translatedOtherRule);
					resRule.value = thisRule.value * otherRule.value;
					newRules.add(resRule);
				}
			}
		}
		
		CondProbDD cpDDNew = new CondProbDD(cpContext);
		cpDDNew.rules = newRules;
		
		return cpDDNew;
	}
	
	
	
	public CondProbDD sumOut(Collection<DDVariable> sumOutVars, boolean normalize) throws Exception {
		AlgebraicDD summedOutDD = super.sumOut(sumOutVars, normalize);
		CondProbDDContext cpContext = (CondProbDDContext) context;
		
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> inVars = (ArrayList<DDVariable>) cpContext.getInputVarSpace().getVariables().clone();
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> outVars = (ArrayList<DDVariable>) cpContext.getOutputVarSpace().getVariables().clone();
		
		inVars.removeAll(sumOutVars);
		outVars.removeAll(sumOutVars);
		
		DDVariableSpace inVarSpace = new DDVariableSpace();
		inVarSpace.addVariables(inVars);
		
		DDVariableSpace outVarSpace = new DDVariableSpace();
		outVarSpace.addVariables(outVars);
		
		CondProbDDContext cpContextNew = new CondProbDDContext(inVarSpace,outVarSpace);
		
		CondProbDD cpDDNew = new CondProbDD(cpContextNew);
		cpDDNew.rules = summedOutDD.getRules();
		
		return cpDDNew;
	}
	
	public CondProbDD sumOutAllExcept(Collection<DDVariable> sumOutVars, boolean normalize) throws Exception {
		AlgebraicDD summedOutDD = super.sumOutAllExcept(sumOutVars, normalize);
		CondProbDDContext cpContext = (CondProbDDContext) context;
		
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> inVars = (ArrayList<DDVariable>) cpContext.getInputVarSpace().getVariables().clone();
		@SuppressWarnings("unchecked")
		ArrayList<DDVariable> outVars = (ArrayList<DDVariable>) cpContext.getOutputVarSpace().getVariables().clone();
		
		inVars.retainAll(sumOutVars);
		outVars.retainAll(sumOutVars);
		
		DDVariableSpace inVarSpace = new DDVariableSpace();
		inVarSpace.addVariables(inVars);
		
		DDVariableSpace outVarSpace = new DDVariableSpace();
		outVarSpace.addVariables(outVars);
		
		CondProbDDContext cpContextNew = new CondProbDDContext(inVarSpace,outVarSpace);
		
		CondProbDD cpDDNew = new CondProbDD(cpContextNew);
		cpDDNew.rules = summedOutDD.getRules();
		
		return cpDDNew;
	}

}
