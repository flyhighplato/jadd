package masg.dd.vars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.DecisionRule;

public class DDVariableSpace implements Iterable<HashMap<DDVariable,Integer>> {
	protected ArrayList<DDVariable> variables = new ArrayList<DDVariable>();

	@Override
	public DDVariableSpaceIterator iterator() {
		return new DDVariableSpaceIterator(this);
	}
	
	public DecisionRule generateRule(HashMap<DDVariable,Integer> varValues, double val) throws Exception {
		DecisionRule r = new DecisionRule(getBitCount(),val);
		
		int ruleOffset = 0;
		for(DDVariable currVariable:variables) {
			if(varValues.containsKey(currVariable)) {
				Integer varInValue = varValues.get(currVariable);
				String binStr = String.format("%0" + currVariable.getBitCount() + "d", Integer.parseInt(Integer.toBinaryString(varInValue)));
				binStr = new StringBuffer(binStr).reverse().toString();
				
				char[] bitChars = binStr.toCharArray() ;
				
				int i;
				for(i=0;i<bitChars.length;i++) {
					r.setBit(ruleOffset+i, bitChars[i]);
				}
				
				
			}
			
			ruleOffset += currVariable.getBitCount();
		}
		
		return r;
	}
	
	public DecisionRule translateRule(DecisionRule fromRule, DDVariableSpace fromVarSpace) throws Exception {
		DecisionRule toRule = new DecisionRule(getBitCount(),fromRule.value);
		
		int toStartBitIx=0;
		for(int i = 0;i<variables.size();i++) {
			DDVariable currVar = variables.get(i);
			int fromVarIx = fromVarSpace.variables.indexOf(currVar);
			
			if(fromVarIx>-1) {
				
				int fromStartBitIx = 0;
				for(int j = 0;j<fromVarIx;j++) {
					fromStartBitIx+=fromVarSpace.variables.get(j).getBitCount();
				}
				
				for(int j = 0; j<currVar.getBitCount();j++) {
					toRule.setBit(toStartBitIx + j, fromRule.getBit(fromStartBitIx + j));
				}
				
			}
			
			toStartBitIx += variables.get(i).getBitCount();
		}
		
		return toRule;
	}
	
	public int getVariableCount() {
		return variables.size();
	}
	
	public DDVariable getVariable(int index) {
		return variables.get(index);
	}
	
	public final ArrayList<DDVariable> getVariables() {
		return variables;
	}
	
	public void addVariable(DDVariable var) {
		variables.add(var);
	}
	
	public void addVariables(Collection<DDVariable> var) {
		variables.addAll(var);
	}
	
	public int getBitCount() {
		int sumNumBits = 0;
		for(DDVariable var:variables) {
			sumNumBits += var.numBits;
		}
		return sumNumBits;
	}
	
	@SuppressWarnings("unchecked")
	public DDVariableSpace plus(DDVariableSpace otherVarSpace) {
		DDVariableSpace newVarSpace = new DDVariableSpace();
		newVarSpace.variables = (ArrayList<DDVariable>) variables.clone();
		newVarSpace.variables.addAll((Collection<? extends DDVariable>) otherVarSpace.variables.clone());
		return newVarSpace;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if(o instanceof Collection) {
			return (new HashSet<DDVariable>(variables)).equals(new HashSet<DDVariable>((Collection<DDVariable>)o));
		}
		else{
			return o == this;
		}
	}
}
