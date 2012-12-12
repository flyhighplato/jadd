package masg.dd.representations.dag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.representations.tables.TableDDLeaf;
import masg.dd.representations.tables.TableDDNode;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public class ImmutableDDNode implements ImmutableDDElement {
	protected ImmutableDDElement[] subCollections;
	
	protected DDVariable var;
	protected boolean isMeasure;
	protected long id=-1;
	
	HashSet<DDVariable> treeVars;

	public ImmutableDDNode(HashSet<DDVariable> allVars, TableDDNode mutableCollection, HashMap<Long,ImmutableDDElement> processedNodes, boolean isMeasure) {
		if(processedNodes==null) {
			processedNodes = new HashMap<Long,ImmutableDDElement>();
		}
		
		var = mutableCollection.getVariable();
		id = mutableCollection.getId();
		treeVars = allVars;
		this.isMeasure = isMeasure;
		
		subCollections = new ImmutableDDElement[var.getValueCount()];
		
		for(int i = 0; i<var.getValueCount(); i++) {
			ImmutableDDElement newElem = null;
			
			if(!processedNodes.containsKey(mutableCollection.getChildren()[i].getId())) {
				if(mutableCollection.getChildren()[i] instanceof TableDDNode) {
					newElem = new ImmutableDDNode(allVars, (TableDDNode)mutableCollection.getChildren()[i], processedNodes, isMeasure);
				}
				else if(mutableCollection.getChildren()[i] instanceof TableDDLeaf) {
					newElem = new ImmutableDDLeaf(allVars, (TableDDLeaf)mutableCollection.getChildren()[i], processedNodes);
				}
			}
			else {
				newElem = processedNodes.get(mutableCollection.getChildren()[i].getId());
			}
			
			subCollections[i]=newElem;
		}
		
		processedNodes.put(mutableCollection.getId(), this);
	}

	@Override
	public DDVariable getVariable() {
		return var;
	}
	
	public HashSet<DDVariable> getVariables() {
		return treeVars;
	}
	
	public ImmutableDDElement getChild(int i) {
		return subCollections[i];
	}
	
	public final ImmutableDDElement[] getChildren() {
		return subCollections;
	}

	public Double getValue(HashMap<DDVariable,Integer> path) {
		if(path.containsKey(var)) {	
			return subCollections[path.get(var)].getValue(path);
			
		}
		else if(isMeasure){
			Double val = null;
			
			HashMap<ImmutableDDElement,Double> cachedValues = new HashMap<ImmutableDDElement,Double>();
			for(int key=0;key<var.getValueCount();key++) {
				
				ImmutableDDElement hdrc = subCollections[key];
				
				Double subValue;
				if(cachedValues.containsKey(hdrc)) {
					subValue = cachedValues.get(hdrc);
				}
				else {
					subValue = hdrc.getValue(path);
					cachedValues.put(hdrc, subValue);
				}
				
				if(subValue!=null) {
					if(val==null) {
						val = subValue;
					}
					else {
						val += subValue;
					}
				}
				else {
					return null;
				}
			}
			
			return val;
		}
		
		return null;
	}
	
	@Override
	public boolean isMeasure() {
		return isMeasure;
	}

	@Override
	public Double getTotalWeight() {
		double val = 0.0f;
		for(ImmutableDDElement hdrc:subCollections) {
			val += hdrc.getTotalWeight();
		}
		
		return new Double(val);
	}
	
	public String toString(String spacer) {
		String str = "";
		ArrayList<ImmutableDDElement> doneCollections = new ArrayList<ImmutableDDElement>();
		ArrayList<HashSet<Integer>> doneKeys = new ArrayList<HashSet<Integer>>();
		
		for(int key=0;key<var.getValueCount();key++) {
			ImmutableDDElement elem = subCollections[key];
			
			int ix = doneCollections.indexOf(elem);
			if(ix>-1) {
				doneKeys.get(ix).add(key);
			}
			else {
				doneCollections.add(elem);
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(key);
				doneKeys.add(set);
			}
		}
		
		for(int i=0;i<doneCollections.size();++i) {
			str += doneCollections.get(i).toString(spacer + doneKeys.get(i).toString());
		}
		
		return str;
	}
	
	public String toString() {
		HashSet<DDVariable> uniqVars = new HashSet<DDVariable>(getVariables());
		ArrayList<DDVariable> varsInOrder = new ArrayList<DDVariable>();
		for(DDVariable v:DDContext.canonicalVariableOrdering) {
			if(uniqVars.contains(v)) {
				varsInOrder.add(v);
			}
		}
		return  varsInOrder + "\n" + toString("");
	}

	public boolean equals(Object o) {
		if(o==this)
			return true;
		
		if(o instanceof ImmutableDDNode) {
			ImmutableDDNode otherHDRC = (ImmutableDDNode) o;
			return otherHDRC.id == id;
		}
		else {
			return false;
		}
	}
	
	public int hashCode() {
		return (int)id;
	}
	
}
