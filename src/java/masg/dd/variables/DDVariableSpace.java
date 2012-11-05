package masg.dd.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class DDVariableSpace implements Iterable<HashMap<DDVariable,Integer>> {
	protected DDVariable[] variables;
	
	public DDVariableSpace() {
		
	}
	
	public DDVariableSpace(ArrayList<DDVariable> variables) {
		this.variables = variables.toArray(new DDVariable[variables.size()]);
	}
	
	@Override
	public DDVariableSpaceIterator iterator() {
		return new DDVariableSpaceIterator(this);
	}
	
	public int getVariableCount() {
		return variables==null?0:variables.length;
	}
	
	public DDVariable getVariable(int index) {
		return variables[index];
	}
	
	public final ArrayList<DDVariable> getVariables() {
		if(variables != null)
			return new ArrayList<DDVariable>(Arrays.asList(variables));
		else
			return new ArrayList<DDVariable>();
	}
	
	public void addVariable(DDVariable var) {
		ArrayList<DDVariable> listVars;
		
		if(variables!=null) {
			listVars = new ArrayList<DDVariable>(Arrays.asList(variables));
		}
		else {
			listVars = new ArrayList<DDVariable>();
		}
		listVars.add(var);
		variables = listVars.toArray(new DDVariable[listVars.size()]);
	}
	
	public void addVariables(Collection<DDVariable> var) {
		ArrayList<DDVariable> listVars;
		
		if(variables!=null) {
			listVars = new ArrayList<DDVariable>(Arrays.asList(variables));
		}
		else {
			listVars = new ArrayList<DDVariable>();
		}
		listVars.addAll(var);
		variables = listVars.toArray(new DDVariable[listVars.size()]);
	}
	
	public int getBitCount() {
		int sumNumBits = 0;
		
		if(variables!=null) {
			for(DDVariable var:variables) {
				sumNumBits += var.numBits;
			}
		}
		return sumNumBits;
	}
	
	public void unprime() throws Exception {
		ArrayList<DDVariable> newVars = new ArrayList<DDVariable>();
		for(DDVariable var:variables) {
			DDVariable varUnprime = var.getUnprimed();
			if(newVars.contains(varUnprime)){
				throw new Exception("Unpriming would make a duplicate variable (" + varUnprime + ")");
			}
			newVars.add(varUnprime);
		}
		variables = newVars.toArray(new DDVariable[newVars.size()]);
	}
	
	public void prime() throws Exception {
		ArrayList<DDVariable> newVars = new ArrayList<DDVariable>();
		for(DDVariable var:variables) {
			DDVariable varPrime = var.getPrimed();
			
			if(newVars.contains(varPrime)){
				throw new Exception("Priming would make a duplicate variable (" + varPrime + ")");
			}
			newVars.add(varPrime);
		}
		
		variables = newVars.toArray(new DDVariable[newVars.size()]);
	}
	
	public DDVariableSpace plus(DDVariableSpace otherVarSpace) {
		DDVariableSpace newVarSpace = new DDVariableSpace();
		if(variables != null) {
			newVarSpace.variables = new DDVariable[variables.length + otherVarSpace.variables.length];
			System.arraycopy(variables, 0, newVarSpace.variables, 0, variables.length);
			System.arraycopy(otherVarSpace.variables, 0, newVarSpace.variables, variables.length, otherVarSpace.variables.length);
		}
		else {
			newVarSpace.variables = new DDVariable[otherVarSpace.variables.length];
			System.arraycopy(otherVarSpace.variables, 0, newVarSpace.variables, 0, otherVarSpace.variables.length);
		}
		return newVarSpace;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if(o instanceof Collection) {
			return (new HashSet<DDVariable>(Arrays.asList(variables))).equals(new HashSet<DDVariable>((Collection<DDVariable>)o));
		}
		else{
			return o == this;
		}
	}
}
