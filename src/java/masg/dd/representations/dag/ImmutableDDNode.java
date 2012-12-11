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

public class ImmutableDDNode extends BaseDDNode implements ImmutableDDElement {
	//protected HashMap<BitMap,ImmutableDDElement> subCollections = new HashMap<BitMap,ImmutableDDElement>();
	protected ImmutableDDElement[] subCollections;
	
	protected DDVariable var;
	protected boolean isMeasure;
	protected long id=-1;
	
	HashSet<DDVariable> treeVars;
	
	/*public ImmutableDDNode(MutableDDNode mutableCollection) {
		this.isMeasure = mutableCollection.isMeasure;
		var = mutableCollection.getVariable();
		
		for(Entry<BitMap, ImmutableDDElement> e:mutableCollection.subCollections.entrySet()) {
			ImmutableDDElement newElem = null;
			if(e.getValue() instanceof MutableDDNode) {
				newElem = new ImmutableDDNode((MutableDDNode) e.getValue());
			}
			else if(e.getValue() instanceof MutableDDLeaf) {
				newElem = new ImmutableDDLeaf((MutableDDLeaf) e.getValue());
			}	
			subCollections.put(e.getKey(), newElem);
		}

	}*/
	
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
			
			if(!processedNodes.containsKey(mutableCollection.getId())) {
				if(mutableCollection.getChildren()[i] instanceof TableDDNode) {
					newElem = new ImmutableDDNode(allVars, (TableDDNode)mutableCollection.getChildren()[i], processedNodes, isMeasure);
				}
				else if(mutableCollection.getChildren()[i] instanceof TableDDLeaf) {
					newElem = new ImmutableDDLeaf(allVars, (TableDDLeaf)mutableCollection.getChildren()[i], processedNodes);
				}
			}
			else {
				newElem = processedNodes.get(mutableCollection.getId());
			}
			
			subCollections[i]=newElem;
		}
		
		processedNodes.put(mutableCollection.getId(), this);
	}
	
	public ImmutableDDNode(ArrayList<DDVariable> vars, boolean isMeasure)  {
		this.isMeasure = isMeasure;
		subCollections = new ImmutableDDElement[var.getValueCount()];
		
		ArrayList<DDVariable> nextVariables = new ArrayList<DDVariable>(vars);
		
		if(DDContext.canonicalVariableOrdering!=null && DDContext.canonicalVariableOrdering.size()>0) {
			for(DDVariable nextVar:DDContext.canonicalVariableOrdering) {
				if(nextVariables.contains(nextVar)) {
					var = nextVar;
					nextVariables.remove(nextVar);
					break;
				}
			}
			
		}
	}

	@Override
	public DDVariable getVariable() {
		return var;
	}
	
	public HashSet<DDVariable> getVariables() {
		/*ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		vars.add(var);
		if(subCollections!=null) {
			HashSet<DDVariable> uniqueVars = new HashSet<DDVariable>(vars);
			for(ImmutableDDElement v: subCollections.values()) {
				uniqueVars.addAll(v.getVariables());
			}
			vars = new ArrayList<DDVariable>(uniqueVars);
		}*/
		
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
	
	
	/*public ArrayList<Double> getValues(HashMap<DDVariable,HashSet<BitMap>> keyMap) {
		HashSet<BitMap> keys = keyMap.get(getVariable());
		
		ArrayList<Double> values = new ArrayList<Double>();
		if(keys!=null) {
			for(Entry<BitMap, ImmutableDDElement> e:subCollections.entrySet()) {
				if(keys.contains(e.getKey())) {
					values.addAll(e.getValue().getValues(keyMap));
				}
			}
		}
		else if(isMeasure){
			for(ImmutableDDElement hdrc:subCollections.values()) {
				values.addAll(hdrc.getValues(keyMap));
			}
		}
		return values;
	}*/
	
	/*public void getIsMeasure(HashMap<DDVariable,Boolean> map) {
		map.put(var, isMeasure);
		
		for(ImmutableDDElement hdrc:subCollections.values()) {
			hdrc.getIsMeasure(map);
		}
		
	}*/
	
	/*public HashMap<DDVariable,Boolean> getIsMeasure() {
		HashMap<DDVariable,Boolean> temp = new HashMap<DDVariable,Boolean>();
		temp.put(var, isMeasure);
		getIsMeasure(temp);
		return temp;
	}*/
	
	/*public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, UnaryOperation oper, MutableDDElement newCollection) {
		
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			BitMap bm = joinKeys(prefix, e.getKey());
			e.getValue().apply(newPrefixVars, bm, oper, newCollection);
		}
		
		
	}
	
	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, BinaryOperation oper, ArrayList<ImmutableDDElement> otherCollections, MutableDDElement newCollection) {
		
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			BitMap bm = joinKeys(prefix, e.getKey());
			e.getValue().apply(newPrefixVars, bm, oper, otherCollections, newCollection);
		}
		
		
		
		
	}
	
	public void copy(ArrayList<DDVariable> prefixVars, BitMap prefix, BinaryOperation oper, MutableDDElement newCollection) {
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			BitMap bm = joinKeys(prefix, e.getKey());
			e.getValue().copy(newPrefixVars, bm, oper, newCollection);
		}
		
	}
	
	public void restrict(ArrayList<DDVariable> prefixVars, BitMap prefix, ArrayList<DDVariable> restrictVars, BitMap restrictKey, MutableDDElement newCollection) {
		
		BitMap rVar = extractPivot(restrictVars, restrictKey);
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		if(rVar==null) {
			newPrefixVars.add(getVariable());
			
			for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
				BitMap bm = joinKeys(prefix, e.getKey());
				e.getValue().restrict(newPrefixVars, bm, restrictVars, restrictKey, newCollection);
			}
			
		}
		else {
			subCollections.get(rVar).restrict(newPrefixVars, prefix, restrictVars, restrictKey, newCollection);
			
		}
	}
	
	public MutableDDElement eliminateVariables(ArrayList<DDVariable> elimVars, BinaryOperation oper){
		HashSet<DDVariable> haveVars = new HashSet<DDVariable>(getVariables());
		haveVars.removeAll(elimVars);
		MutableDDElement coll;
		if(haveVars.size()>0) {
			coll = new MutableDDNode(new ArrayList<DDVariable>(haveVars), isMeasure);
			
		}
		else {
			coll = new MutableDDLeaf(0.0f);
		}
		
		copy(new ArrayList<DDVariable>(),null,oper,coll);
		return coll;
		
	}*/
	
	/*public void primeVariables(ArrayList<DDVariable> prefixVars, BitMap prefix,MutableDDElement newColl) {
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			BitMap bm = joinKeys(prefix, e.getKey());
			e.getValue().primeVariables(newPrefixVars, bm, newColl);
		}
	}
	
	public MutableDDElement primeVariables(){
		HashSet<DDVariable> haveVars = new HashSet<DDVariable>();
		for(DDVariable v:getVariables()) {
			haveVars.add(v.getPrimed());
		}
		
		MutableDDElement coll;
		if(haveVars.size()>0) {
			coll = new MutableDDNode(new ArrayList<DDVariable>(haveVars), isMeasure);
			
		}
		else {
			coll = new MutableDDLeaf(0.0f);
		}
		
		primeVariables(new ArrayList<DDVariable>(),null,coll);
		return coll;
		
	}
	
	public void unprimeVariables(ArrayList<DDVariable> prefixVars, BitMap prefix,MutableDDElement newColl) {
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			BitMap bm = joinKeys(prefix, e.getKey());
			e.getValue().unprimeVariables(newPrefixVars, bm, newColl);
		}
	}
	
	public MutableDDElement unprimeVariables(){
		HashSet<DDVariable> haveVars = new HashSet<DDVariable>();
		for(DDVariable v:getVariables()) {
			haveVars.add(v.getUnprimed());
		}
		
		MutableDDElement coll;
		if(haveVars.size()>0) {
			coll = new MutableDDNode(new ArrayList<DDVariable>(haveVars), isMeasure);
			
		}
		else {
			coll = new MutableDDLeaf(0.0f);
		}
		
		unprimeVariables(new ArrayList<DDVariable>(),null,coll);
		return coll;
		
	}*/
	
	/*public MutableDDElement restrict(Map<DDVariable,Integer> elimVarValues){
		HashSet<DDVariable> haveVars = new HashSet<DDVariable>(getVariables());
		haveVars.removeAll(elimVarValues.keySet());
		
		MutableDDElement coll;
		if(haveVars.size()>0) {
			coll = new MutableDDNode(new ArrayList<DDVariable>(haveVars), isMeasure);
		}
		else {
			coll = new MutableDDLeaf(0.0f);
		}
		
		BitMap restrictKey = null;
		ArrayList<DDVariable> restrictVars = new ArrayList<DDVariable>(elimVarValues.keySet());
		for(DDVariable elimVar:restrictVars) {
			BitMap suffix = variableValuetoBitMap(elimVar,elimVarValues.get(elimVar));
			restrictKey = joinKeys(restrictKey,suffix);
		}
		
		restrict(new ArrayList<DDVariable>(),null,restrictVars,restrictKey,coll);
		
		coll.setIsMeasure(getIsMeasure());
		return coll;
	}
	
	public MutableDDElement apply(UnaryOperation oper) {
		MutableDDNode coll = new MutableDDNode(getVariables(), isMeasure);
		apply(new ArrayList<DDVariable>(),null, oper, coll);
		return coll;
	}
	
	public MutableDDNode apply(BinaryOperation oper, ImmutableDDElement otherColl) {
		MutableDDNode coll = new MutableDDNode(getVariables(), otherColl.isMeasure() && isMeasure);
		ArrayList<ImmutableDDElement> otherCollections = new ArrayList<ImmutableDDElement>();
		otherCollections.add(otherColl);
		apply(new ArrayList<DDVariable>(),null,oper,otherCollections,coll);
		return coll;
	}
	
	public MutableDDNode apply(BinaryOperation oper, ArrayList<ImmutableDDElement> otherColl) {
		boolean isAllMeasure = isMeasure;
		
		if(isMeasure) {
			for(ImmutableDDElement coll:otherColl) {
				if(!coll.isMeasure()) {
					isAllMeasure = false;
					break;
				}
			}
		}
		
		MutableDDNode coll = new MutableDDNode(getVariables(), isAllMeasure);
		apply(new ArrayList<DDVariable>(),null,oper,otherColl,coll);
		return coll;
	}*/
	
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
		
		//for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
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
