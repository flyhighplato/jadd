package masg.dd.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import masg.dd.context.DDContext;

public class DDVariableSpace implements Iterable<HashMap<DDVariable,Integer>> {
	protected HashSet<DDVariable> variables;
	
	protected ArrayList<DDVariable> variablesInOrder = new ArrayList<DDVariable>();
	
	public DDVariableSpace() {
		init(new HashSet<DDVariable>());
	}
	
	public DDVariableSpace(Collection<DDVariable> variables) {
		init(variables);
	}
	
	public DDVariableSpace(DDVariableSpace space) {
		init(space.variables);
	}
	
	private void init(Collection<DDVariable> variables) {
		this.variables = new HashSet<DDVariable>(variables);
		
		for(DDVariable v:DDContext.canonicalVariableOrdering) {
			if(this.variables.contains(v)) {
				variablesInOrder.add(v);
			}
		}
	}
	
	@Override
	public DDVariableSpaceIterator iterator() {
		return new DDVariableSpaceIterator(this);
	}
	
	public int getVariableCount() {
		return variables.size();
	}
	
	public final DDVariable getVariable(int index) {
		return variablesInOrder.get(index);
	}
	
	public final Set<DDVariable> getVariables() {
		return Collections.unmodifiableSet(variables);
	}
	
	public boolean isEmpty() {
		return variables.size() == 0;
	}
	
	public void unprime() throws Exception {
		HashSet<DDVariable> newVars = new HashSet<DDVariable>();
		for(DDVariable var:variables) {
			DDVariable varUnprime = var.getUnprimed();
			if(newVars.contains(varUnprime)){
				throw new Exception("Unpriming would make a duplicate variable (" + varUnprime + ")");
			}
			newVars.add(varUnprime);
		}
		variables = newVars;
	}
	
	public void prime() throws Exception {
		HashSet<DDVariable> newVars = new HashSet<DDVariable>();
		for(DDVariable var:variables) {
			DDVariable varPrime = var.getPrimed();
			
			if(newVars.contains(varPrime)){
				throw new Exception("Priming would make a duplicate variable (" + varPrime + ")");
			}
			newVars.add(varPrime);
		}
		
		variables = newVars;
	}
	
	public boolean contains(DDVariable v) {
		return variables.contains(v);
	}
	
	public DDVariableSpace exclude(DDVariableSpace vars) {
		return exclude(vars.getVariables());
	}
	
	public DDVariableSpace exclude(Collection<DDVariable> excludeVars) {
		HashSet<DDVariable> temp = new HashSet<DDVariable>(variables);
		temp.removeAll(excludeVars);
		
		return new DDVariableSpace(temp);
	}
	
	public DDVariableSpace exclude(DDVariable excludeVar) {
		HashSet<DDVariable> temp = new HashSet<DDVariable>(variables);
		temp.remove(excludeVar);
		
		return new DDVariableSpace(temp);
	}
	
	public DDVariableSpace intersect(DDVariableSpace vars) {
		return intersect(vars.getVariables());
	}
	
	public DDVariableSpace intersect(Collection<DDVariable> retainVars) {
		HashSet<DDVariable> temp = new HashSet<DDVariable>(variables);
		temp.retainAll(retainVars);
		
		return new DDVariableSpace(temp);
	}
	
	public DDVariableSpace union(DDVariableSpace vars) {
		return union(vars.getVariables());
	}
	
	public DDVariableSpace union(Collection<DDVariable> unionVars) {
		HashSet<DDVariable> temp = new HashSet<DDVariable>(variables);
		temp.addAll(unionVars);
		
		return new DDVariableSpace(temp);
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
	
	public String toString() {
		return variablesInOrder.toString();
	}
}
