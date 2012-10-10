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
	double tolerance = 0.0001f;
	
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
		
		
		//Remove duplicate matches
		for(int i=0;i<newRules.size();++i) {
			DecisionRule r1 = newRules.get(i);
			for(int j=i+1;j<newRules.size();++j) {
				DecisionRule r2 = newRules.get(j);
				
				if(r1.bitStringEquals(r2)) {
					r1.value += r2.value;
					newRules.remove(j);
					j--;
				}
				else {
					DecisionRule rSup = DecisionRule.getSupersetBitStringRule(r1, r2);
					
					if(rSup!=null) {
						DecisionRule rOther;
						if(rSup.equals(r1)) {
							rSup = r1;
							rOther = r2;
						}
						else {
							rSup = r2;
							rOther = r1;
						}
						
						if(Math.abs(r1.value-r2.value)>tolerance) {
							for(int currBitIx=rSup.getNumBits()-1;currBitIx>=0;currBitIx--) {
								if(rSup.getBit(currBitIx)=='*') {
									if(rOther.getBit(currBitIx)=='0') {
										rSup.setBit(currBitIx, '1');
										break;
									}
									else if(rOther.getBit(currBitIx)=='1') {
										rSup.setBit(currBitIx, '0');
										break;
									}
								}
							}
						}
						else {
							if(rSup==r1) {
								newRules.remove(j);
								j--;
							}
							else {
								newRules.remove(i);
								i--;
								break;
							}
						}
					}
				}
				
			}
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
	
	public ProbDD times(double value) throws Exception {
		ProbDD addNew = new ProbDD(context);
		
		for(DecisionRule ruleThis:rules) {
			DecisionRule resRule = new DecisionRule(ruleThis);
			resRule.value = ruleThis.value * value;
			addNew.addRule(resRule);
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
