package masg.dd.representations.dag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.context.DDContext;
import masg.dd.representations.tables.TableDDLeaf;
import masg.dd.variables.DDVariable;

public class ImmutableDDLeaf implements ImmutableDDElement{
	protected Double value;

	HashSet<DDVariable> treeVars;
	
	public ImmutableDDLeaf(double value) {
		this.value = value;
	}
	
	long id=-1;
	
	public ImmutableDDLeaf(HashSet<DDVariable> allVars, TableDDLeaf leaf, HashMap<Long,ImmutableDDElement> processedNodes) {
		treeVars = allVars;
		id = leaf.getId();
		if(processedNodes==null) {
			processedNodes = new HashMap<Long,ImmutableDDElement>();
		}
		this.value = leaf.getValue();
		processedNodes.put(leaf.getId(), this);
	}

	@Override
	public Double getTotalWeight() {
		return value;
	}

	@Override
	public Double getValue(HashMap<DDVariable,Integer> path) {
		return value;
	}
	
	public Double getValue() {
		return value;
	}

	@Override
	public HashSet<DDVariable> getVariables() {
		return treeVars;
	}

	@Override
	public String toString(String spacer) {
		return spacer + ":" + value + "\n";
	}

	@Override
	public boolean isMeasure() {
		return true;
	}
	
	@Override
	public DDVariable getVariable() {
		return null;
	}

	public boolean equals(Object o) {
		if(o==this)
			return true;
		
		if(o instanceof ImmutableDDLeaf) {
			ImmutableDDLeaf leaf = (ImmutableDDLeaf) o;
			return Math.abs(leaf.getValue().doubleValue()-getValue().doubleValue())<0.00001f;
		}
		else {
			return o.equals(this);
		}
	}

	public String toString() {
		HashSet<DDVariable> uniqVars = new HashSet<DDVariable>(getVariables());
		ArrayList<DDVariable> varsInOrder = new ArrayList<DDVariable>();
		for(DDVariable v:DDContext.canonicalVariableOrdering) {
			if(uniqVars.contains(v)) {
				varsInOrder.add(v);
			}
		}
		return  varsInOrder + "\n" + value.toString();
	}
	
	public int hashCode() {
		return (int)id;
	}
	
}
