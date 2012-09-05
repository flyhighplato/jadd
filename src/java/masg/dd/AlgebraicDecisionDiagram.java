package masg.dd;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class AlgebraicDecisionDiagram extends AbstractDecisionDiagram {
	protected DecisionRuleCollection rules;

	public AlgebraicDecisionDiagram(DecisionDiagramContext ctx) {
		super(ctx);
		rules = new DecisionRuleCollection(ctx.getVariableSpace().getBitCount());
	}

	public DecisionRuleCollection getRules() {
		return rules;
	}
	
	public synchronized void addRule(DecisionRule rule) throws Exception {
		rules.add(rule);
		compress();

	}
	
	public synchronized void addRules(ArrayList<DecisionRule> rules) throws Exception {
		this.rules.addAll(rules);
		compress();
	}

	public void compress() throws Exception {
		rules.compress();
	}
	
	public AlgebraicDecisionDiagram sumOutAllExcept(Collection<DDVariable> values) throws Exception {
		ArrayList<DDVariable> sumOutValues = new ArrayList<DDVariable>();
		
		ArrayList<DDVariable> currVariables = context.varSpace.getVariables();
		for(DDVariable val:currVariables) {
			if(!values.contains(val)) {
				sumOutValues.add(val);
			}
		}
		
		return sumOut(sumOutValues);
	}
	
	public AlgebraicDecisionDiagram restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		
		boolean willChange = false;
		
		for(DDVariable var:context.getVariableSpace().getVariables()) {
			if(varInstances.containsKey(var)) {
				willChange = true;
				break;
			}
		}
		
		
		
		AlgebraicDecisionDiagram addNew = new AlgebraicDecisionDiagram(context);
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
	
	public AlgebraicDecisionDiagram sumOut(Collection<DDVariable> sumOutVars) throws Exception {
		return sumOut(sumOutVars,true);
	}
	
	public AlgebraicDecisionDiagram sumOut(Collection<DDVariable> sumOutVars, boolean normalize) throws Exception {
		DDVariableSpace newVarSpace = new DDVariableSpace();
		
		DDVariableSpace ignoreVarSpace = new DDVariableSpace();
		
		ArrayList<DDVariable> currVariables = context.varSpace.getVariables();
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
		AlgebraicDecisionDiagram resultDD = new AlgebraicDecisionDiagram(newCtx);
		
		HashMap<String,Double> newRuleNonnormValues = new HashMap<String,Double>();
		double totalSum = 0.0f;
		
		if(newVarSpace.getVariableCount()>0) {
			for(HashMap<DDVariable,Integer> newVarSpacePoint: newVarSpace){
				DecisionRule ruleNewInOldContext = context.varSpace.generateRule(newVarSpacePoint, 0);
				
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
			if(normalize) {
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
	
	
	public double getValue(DecisionRule ruleOther) {
		for(DecisionRule ruleThis:rules) {
			if(ruleThis.matches(ruleOther)) {
				return ruleThis.value;
			}
		}
		
		return Double.NaN;
	}
	
	public AlgebraicDecisionDiagram multiply(AlgebraicDecisionDiagram addOther) throws Exception {
		AlgebraicDecisionDiagram addNew = new AlgebraicDecisionDiagram(context);
		
		for(DecisionRule ruleThis:rules) {
			for(DecisionRule ruleOther:addOther.rules) {
				
				if(ruleThis.matches(ruleOther)) {
					DecisionRule resRule = DecisionRule.getSubsetBitStringRule(ruleThis, ruleOther);
					resRule.value = ruleThis.value * ruleOther.value;
					addNew.addRule(resRule);
				}
			}
		}
		
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
		for(DecisionRule rule:rules) {
			str += rule + "\n";
		}
		return str;
	}
}
