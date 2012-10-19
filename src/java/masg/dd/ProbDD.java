package masg.dd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import masg.dd.context.DDContext;
import masg.dd.rules.DecisionRule;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class ProbDD extends AlgebraicDD {
	double tolerance = 0.0001f;
	
	public ProbDD(DDContext ctx) {
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
		
		DDContext newCtx = new DDContext(newVarSpace);
		ProbDD resultDD = new ProbDD(newCtx);
		
		ArrayList<DecisionRule> newRules = new ArrayList<DecisionRule>(rules.size());
		for(DecisionRule ruleThis:rules) {
			DecisionRule ruleNewInNewContext;
			if(newVarSpace.getVariableCount()>0) {
				ruleNewInNewContext = newCtx.getVariableSpace().translateRule(ruleThis, context.getVariableSpace());
			}
			else {
				ruleNewInNewContext = new DecisionRule(0, ruleThis.value);
			}
			
			newRules.add(ruleNewInNewContext);
		}
		
		boolean doAgain = true;
		
		while(doAgain) {
			doAgain = false;
			Collections.sort(newRules);
			
			//Remove duplicate matches
			for(int i=0;i<newRules.size();++i) {
				DecisionRule r1 = newRules.get(i);
				
				for(int j=i+1;j<newRules.size();++j) {
					DecisionRule r2 = newRules.get(j);
					
					boolean noMoreMatches = false;
					for(int currBitIx=0;currBitIx<r1.getNumBits();++currBitIx) {
						if(r1.getBit(currBitIx)=='*' || r2.getBit(currBitIx)=='*')
							break; //We can't tell if this might be a duplicate rule
						if(r1.getBit(currBitIx)!=r2.getBit(currBitIx)) {
							noMoreMatches = true;
						}
					}
					
					if(noMoreMatches)
						break;
					
					if(r1.bitStringEquals(r2)) {
						r1.value += r2.value;
						newRules.remove(j);
						j--;
					}
					else {
						DecisionRule rSup = DecisionRule.getSupersetBitStringRule(r1, r2);
						
						if(rSup!=null) {
							//This screws up the ordering
							doAgain = true;
							
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
