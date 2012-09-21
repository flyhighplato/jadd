package masg.dd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.context.DecisionDiagramContext;
import masg.dd.context.ProbDD;
import masg.dd.rules.DecisionRule;
import masg.dd.rules.DecisionRuleCollection;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class AlgebraicDD extends AbstractDecisionDiagram {
	protected DecisionRuleCollection rules;

	public AlgebraicDD(DecisionDiagramContext ctx) {
		super(ctx);
		rules = new DecisionRuleCollection(ctx.getVariableSpace().getBitCount());
	}

	public DecisionRuleCollection getRules() {
		return rules;
	}
	
	public synchronized void addRule(DecisionRule rule) throws Exception {
		rules.add(rule);

	}
	
	public synchronized void addRules(ArrayList<DecisionRule> rules) throws Exception {
		this.rules.addAll(rules);
	}

	public void compress() throws Exception {
		rules.compress();
	}

	public AlgebraicDD expandRules(Collection<DDVariable> newVars) throws Exception {
		ArrayList<DDVariable> oldVars = new ArrayList<DDVariable>(context.getVariableSpace().getVariables());
		newVars.removeAll(oldVars);
		oldVars.addAll(newVars);
		
		DecisionDiagramContext newContext = new DecisionDiagramContext(new DDVariableSpace(oldVars));
		AlgebraicDD newDD = new AlgebraicDD(newContext);
		
		ArrayList<DecisionRule> newRules = new ArrayList<DecisionRule>();
		for(DecisionRule oldRule:rules) {
			DecisionRule newRule = newContext.getVariableSpace().translateRule(oldRule, context.getVariableSpace());
			newRules.add(newRule);
		}
		
		newDD.addRules(newRules);
		return newDD;
	}
	
	public double getValue(HashMap<DDVariable,Integer> varValues) throws Exception {
		return getValue(context.getVariableSpace().generateRule(varValues, 0));
	}
	
	public double getValue(DecisionRule ruleOther) {
		for(DecisionRule ruleThis:rules) {
			if(ruleThis.matches(ruleOther)) {
				return ruleThis.value;
			}
		}
		
		return Double.NaN;
	}
	
	public double maxDiff(AlgebraicDD addOther) throws Exception {
		
		double maxDiff = 0.0f;
		
		for(DecisionRule ruleThis:rules) {
			for(DecisionRule ruleOther:addOther.rules) {
				DecisionRule ruleTrans = context.getVariableSpace().translateRule(ruleOther, addOther.context.getVariableSpace());
				if(ruleThis.matches(ruleTrans)) {
					double diff = Math.abs(ruleThis.value - ruleOther.value);
					
					if(diff>maxDiff) {
						maxDiff = diff;
						System.out.println(ruleThis + " " + ruleOther + " " + ruleTrans);
					}
				}
			}
		}
		
		return maxDiff;
	}
	
	public AlgebraicDD plus(AlgebraicDD addOther) throws Exception {
		AlgebraicDD addNew = new AlgebraicDD(context);
		
		ArrayList<DecisionRule> rRules = new ArrayList<DecisionRule>();
		for(DecisionRule ruleThis:rules) {
			for(DecisionRule ruleOther:addOther.rules) {
				ruleOther = context.getVariableSpace().translateRule(ruleOther, addOther.context.getVariableSpace());
				if(ruleThis.matches(ruleOther)) {
					DecisionRule resRule = DecisionRule.getIntersectionBitStringRule(ruleThis, ruleOther);
					resRule.value = ruleThis.value + ruleOther.value;
					rRules.add(resRule);
				}
			}
		}
		
		addNew.addRules(rRules);
		
		return addNew;
	}
	
	public AlgebraicDD restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		
		boolean willChange = false;
		
		for(DDVariable var:context.getVariableSpace().getVariables()) {
			if(varInstances.containsKey(var)) {
				willChange = true;
				break;
			}
		}
		
		AlgebraicDD addNew = new AlgebraicDD(context);
		ArrayList<DecisionRule> fixedRules = new ArrayList<DecisionRule>();
		
		if(willChange) {
			DecisionRule r = context.getVariableSpace().generateRule(varInstances, 0.0f);
			
			for(DecisionRule ruleThis:rules) {
				if(r.matches(ruleThis)) {
					fixedRules.add(ruleThis);
				}
			}
			addNew.addRules(fixedRules);
		}
		else {
			fixedRules.addAll(rules);
			addNew.rules.addAll(fixedRules);
		}

		return addNew;
	}
	
	public AlgebraicDD sumOutAllExcept(Collection<DDVariable> values) throws Exception {
		ArrayList<DDVariable> sumOutValues = new ArrayList<DDVariable>();
		
		ArrayList<DDVariable> currVariables = context.getVariableSpace().getVariables();
		for(DDVariable val:currVariables) {
			if(!values.contains(val)) {
				sumOutValues.add(val);
			}
		}
		
		return sumOut(sumOutValues);
	}
	
	public AlgebraicDD sumOut(Collection<DDVariable> sumOutVars) throws Exception {
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
		
		if(newVarSpace.getVariableCount()>0) {
			for(DecisionRule ruleThis:rules) {
				DecisionRule rNew = newCtx.getVariableSpace().translateRule(ruleThis, context.getVariableSpace());
				
				String newRuleStr = rNew.toBitString();
				if(newRuleNonnormValues.containsKey(newRuleStr)) {
					newRuleNonnormValues.put(newRuleStr, newRuleNonnormValues.get(newRuleStr) + ruleThis.value);
				}
				else {
					newRuleNonnormValues.put(newRuleStr, ruleThis.value);
				}
			}
			
		}
		else {
			for(DecisionRule ruleThis:rules) {

				DecisionRule rNew = new DecisionRule(0, ruleThis.value);
				
				String newRuleStr = rNew.toBitString();
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
			DecisionRule r = new DecisionRule(newRuleValueEntry.getKey(),newRuleValueEntry.getValue());
			newRules.add(r);
		}

		resultDD.addRules(newRules);

		return resultDD;
	}
	
	public AlgebraicDD times(double value) throws Exception {
		AlgebraicDD addNew = new AlgebraicDD(context);
		
		for(DecisionRule ruleThis:rules) {
			DecisionRule resRule = new DecisionRule(ruleThis);
			resRule.value = ruleThis.value * value;
			addNew.addRule(resRule);
		}
		
		return addNew;
	}
	
	public AlgebraicDD times(AlgebraicDD addOther) throws Exception {
		AlgebraicDD addNew = new AlgebraicDD(context);
		
		ArrayList<DecisionRule> rRules = new ArrayList<DecisionRule>();
		for(DecisionRule ruleThis:rules) {
			for(DecisionRule ruleOther:addOther.rules) {
				ruleOther = context.getVariableSpace().translateRule(ruleOther, addOther.context.getVariableSpace());
				if(ruleThis.matches(ruleOther)) {
					DecisionRule resRule = DecisionRule.getIntersectionBitStringRule(ruleThis, ruleOther);
					resRule.value = ruleThis.value * ruleOther.value;
					rRules.add(resRule);
				}
			}
		}
		
		addNew.addRules(rRules);
		return addNew;
	}
	
	public void normalize() {
		double totalWeight = 0.0f;
		
		for(DecisionRule ruleThis:rules) {
			totalWeight  += ruleThis.value;
		}
		
		for(DecisionRule ruleThis:rules) {
			ruleThis.value = ruleThis.value/totalWeight;
		}
	}
	
	public String toString() {
		String str = "";
		str += context.getVariableSpace().getVariables() + "\n";
		
		int i = 0;
		for(DecisionRule rule:rules) {
			i++;
			str += rule + "\n";
			if(i>1000)
				break;
		}
		return str;
	}
}
