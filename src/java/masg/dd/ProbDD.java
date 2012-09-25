package masg.dd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.context.DecisionDiagramContext;
import masg.dd.rules.DecisionRule;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class ProbDD extends AlgebraicDD {

	public ProbDD(DecisionDiagramContext ctx) {
		super(ctx);
	}
	
	public ProbDD sumOutAllExcept(Collection<DDVariable> values) throws Exception {
		ArrayList<DDVariable> sumOutValues = new ArrayList<DDVariable>();
		
		ArrayList<DDVariable> currVariables = context.getVariableSpace().getVariables();
		for(DDVariable val:currVariables) {
			if(!values.contains(val)) {
				sumOutValues.add(val);
			}
		}
		
		return sumOut(sumOutValues);
	}
	
	public ProbDD sumOutAllExcept(Collection<DDVariable> values, boolean normalize) throws Exception {
		ArrayList<DDVariable> sumOutValues = new ArrayList<DDVariable>();
		
		ArrayList<DDVariable> currVariables = context.getVariableSpace().getVariables();
		for(DDVariable val:currVariables) {
			if(!values.contains(val)) {
				sumOutValues.add(val);
			}
		}
		
		return sumOut(sumOutValues, normalize);
	}
	
	public ProbDD sumOut(Collection<DDVariable> sumOutVars) throws Exception {
		return sumOut(sumOutVars,true);
	}
	
	public ProbDD sumOut(Collection<DDVariable> sumOutVars, boolean normalize) throws Exception {
		DDVariableSpace newVarSpace = new DDVariableSpace();
		
		DDVariableSpace ignoreVarSpace = new DDVariableSpace();
		
		ArrayList<DDVariable> currVariables = context.getVariableSpace().getVariables();
		for(DDVariable var:currVariables) {
			if(!sumOutVars.contains(var)) {
				newVarSpace.addVariable(var);
			}
			else {
				ignoreVarSpace.addVariable(var);
			}
		}
		
		if(ignoreVarSpace.getVariables().size()<=0)
			return this;
		
		DecisionDiagramContext newCtx = new DecisionDiagramContext(newVarSpace);
		ProbDD resultDD = new ProbDD(newCtx);
		
		HashMap<String,Double> newRuleNonnormValues = new HashMap<String,Double>();
		double totalSum = 0.0f;
		
		if(newVarSpace.getVariableCount()>0) {
			for(HashMap<DDVariable,Integer> newVarSpacePoint: newVarSpace){
				DecisionRule ruleNewInOldContext = context.getVariableSpace().generateRule(newVarSpacePoint, 0);
				
				for(DecisionRule ruleThis:rules) {
					if(ruleThis.matches(ruleNewInOldContext)) {
						DecisionRule ruleNewInNewContext = newVarSpace.generateRule(newVarSpacePoint, ruleThis.value);
						
						totalSum += ruleThis.value;
						
						String newRuleStr = ruleNewInNewContext.toBitString();
						if(newRuleNonnormValues.containsKey(newRuleStr)) {
							newRuleNonnormValues.put(newRuleStr, newRuleNonnormValues.get(newRuleStr) + ruleThis.value);
						}
						else {
							newRuleNonnormValues.put(newRuleStr, ruleThis.value);
						}
					}
				}
				
			}
		}
		else {
			for(DecisionRule ruleThis:rules) {

					DecisionRule ruleNewInNewContext = new DecisionRule(0, ruleThis.value);
					
					totalSum += ruleThis.value;
					
					String newRuleStr = ruleNewInNewContext.toBitString();
					if(newRuleNonnormValues.containsKey(newRuleStr)) {
						newRuleNonnormValues.put(newRuleStr, newRuleNonnormValues.get(newRuleStr) + ruleThis.value);
					}
					else {
						newRuleNonnormValues.put(newRuleStr, ruleThis.value);
					}
				
			}
		}
		
		
		ArrayList<DecisionRule> newRules = new ArrayList<DecisionRule>();
		
		for(Entry<String,Double> newRuleValueEntry:newRuleNonnormValues.entrySet()) {
			DecisionRule r;
			if(normalize && totalSum>0.0f) {
				r = new DecisionRule(newRuleValueEntry.getKey(),newRuleValueEntry.getValue()/totalSum);
			}
			else {
				r = new DecisionRule(newRuleValueEntry.getKey(),newRuleValueEntry.getValue());
			}
			newRules.add(r);
		}
		
		

		resultDD.addRules(newRules);

		return resultDD;
	}
	
	public ProbDD restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		
		boolean willChange = false;
		
		for(DDVariable var:context.getVariableSpace().getVariables()) {
			if(varInstances.containsKey(var)) {
				willChange = true;
				break;
			}
		}
		
		
		
		ProbDD addNew = new ProbDD(context);
		ArrayList<DecisionRule> fixedRules = new ArrayList<DecisionRule>();
		
		if(willChange) {
			DecisionRule r = context.getVariableSpace().generateRule(varInstances, 0.0f);
			
			for(DecisionRule ruleThis:rules) {
				if(r.matches(ruleThis)) {
					fixedRules.add(ruleThis);
				}
			}
			addNew.addRules(fixedRules);
			addNew = addNew.sumOut(varInstances.keySet(), false);
		}
		else {
			fixedRules.addAll(rules);
			addNew.rules.addAll(fixedRules);
		}

		return addNew;
	}
	
	public double getProbability(DecisionRule ruleOther) {
		double val = 0.0f;
		for(DecisionRule ruleThis:rules) {
			if(ruleThis.matches(ruleOther)) {
				 val+=ruleThis.value;
			}
		}
		
		return val;
	}

}
