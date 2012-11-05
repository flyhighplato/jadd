package masg.dd.representations.dag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.operations.BinaryOperation;
import masg.dd.operations.UnaryOperation;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public class ImmutableDDNode extends BaseDDNode implements ImmutableDDElement {
	protected HashMap<BitMap,ImmutableDDElement> subCollections;
	
	protected DDVariable var;
	protected boolean isMeasure;
	
	protected ArrayList<ImmutableDDElement> uniqueSubElements = null;
	protected ArrayList<HashSet<BitMap>> uniqueSubElementKeys = null;
	
	public ImmutableDDNode(MutableDDNode mutableCollection) {
		this.isMeasure = mutableCollection.isMeasure;
		var = mutableCollection.getVariable();
		subCollections = new HashMap<BitMap,ImmutableDDElement>();
		
		ImmutableDDElement tempUniqueSubElements[] = null;
		if(mutableCollection.uniqueSubElementKeys!=null && mutableCollection.uniqueSubElementKeys!=null) {
			uniqueSubElementKeys = new ArrayList<HashSet<BitMap>>(mutableCollection.uniqueSubElementKeys);
			uniqueSubElements = new ArrayList<ImmutableDDElement>(uniqueSubElementKeys.size());
			tempUniqueSubElements = new ImmutableDDElement[uniqueSubElementKeys.size()];
			
		}
		
		for(Entry<BitMap, ImmutableDDElement> e:mutableCollection.subCollections.entrySet()) {
			ImmutableDDElement newElem = null;
			if(e.getValue() instanceof MutableDDNode) {
				newElem = new ImmutableDDNode((MutableDDNode) e.getValue());
			}
			else if(e.getValue() instanceof MutableDDLeaf) {
				newElem = new ImmutableDDLeaf((MutableDDLeaf) e.getValue());
			}
			
			subCollections.put(e.getKey(), newElem);
			
			
			if(uniqueSubElementKeys!=null && uniqueSubElements!=null) {
				for(int i=0;i<uniqueSubElementKeys.size();++i) {
					if(uniqueSubElementKeys.get(i).contains(e.getKey())) {
						if(tempUniqueSubElements[i]==null) {
							tempUniqueSubElements[i]=newElem;
							break;
						}
					}
				}
				
				
				uniqueSubElements = new ArrayList<ImmutableDDElement>(Arrays.asList(tempUniqueSubElements));
			}
		}

	}
	
	public ImmutableDDNode(ArrayList<DDVariable> vars, boolean isMeasure)  {
		this.isMeasure = isMeasure;
		subCollections = new HashMap<BitMap,ImmutableDDElement>();
		
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
		
		
		if(nextVariables.size()>0) {
			for(BitMap bm:variableValuesToBitMapValues(var)) {
				subCollections.put(bm, new MutableDDNode(nextVariables, isMeasure));
			}
		}
		else {
			for(BitMap bm:variableValuesToBitMapValues(var)) {
				subCollections.put(bm, new MutableDDLeaf(0.0f));
			}
		}
	}

	@Override
	public DDVariable getVariable() {
		return var;
	}
	
	public ArrayList<DDVariable> getVariables() {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		vars.add(var);
		if(subCollections!=null) {
			HashSet<DDVariable> uniqueVars = new HashSet<DDVariable>(vars);
			for(ImmutableDDElement v: subCollections.values()) {
				uniqueVars.addAll(v.getVariables());
			}
			vars = new ArrayList<DDVariable>(uniqueVars);
		}
		
		return vars;
	}

	public Double getValue(ArrayList<DDVariable> vars, BitMap r) {
		BitMap rVar = extractPivot(vars, r);
		
		if(rVar!=null) {	
			if(subCollections.containsKey(rVar)) {
				return subCollections.get(rVar).getValue(vars, r);
			}
		}
		else if(isMeasure){
			double val = 0.0f;
			
			if(uniqueSubElementKeys!=null && uniqueSubElements!=null) {
				for(int i=0;i<uniqueSubElements.size();++i) {
					val += (uniqueSubElements.get(i).getValue(vars, r).doubleValue() * uniqueSubElementKeys.get(i).size());
				}
			}
			else {
				for(ImmutableDDElement hdrc:subCollections.values()) {
					val += hdrc.getValue(vars, r);
				}
			}
			
			return new Double(val);
		}
		
		return null;
	}
	
	public ArrayList<Double> getValues(HashMap<DDVariable,HashSet<BitMap>> keyMap) {
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
			if(uniqueSubElementKeys!=null && uniqueSubElements!=null) {
				for(int i=0;i<uniqueSubElements.size();++i) {
					ArrayList<Double> temp = uniqueSubElements.get(i).getValues(keyMap);
					
					for(int j=0;j<temp.size();++j) {
						temp.set(j, temp.get(j) * uniqueSubElementKeys.get(i).size());
					}
					
					values.addAll(temp);
				}
			}
			else {
				for(ImmutableDDElement hdrc:subCollections.values()) {
					values.addAll(hdrc.getValues(keyMap));
				}
			}
		}
		return values;
	}
	
	public void getIsMeasure(HashMap<DDVariable,Boolean> map) {
		map.put(var, isMeasure);
		
		for(ImmutableDDElement hdrc:subCollections.values()) {
			hdrc.getIsMeasure(map);
		}
		
	}
	
	public HashMap<DDVariable,Boolean> getIsMeasure() {
		HashMap<DDVariable,Boolean> temp = new HashMap<DDVariable,Boolean>();
		temp.put(var, isMeasure);
		getIsMeasure(temp);
		return temp;
	}
	
	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, UnaryOperation oper, MutableDDElement newCollection) {
		
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			BitMap bm = joinKeys(prefix, e.getKey());
			e.getValue().apply(newPrefixVars, bm, oper, newCollection);
		}
		
		
	}
	
	@Override
	public void apply(HashMap<DDVariable,HashSet<BitMap>> prevKeys, UnaryOperation oper, MutableDDElement newCollection) {
		
		HashMap<DDVariable,HashSet<BitMap>> newPrevKeys = new HashMap<DDVariable,HashSet<BitMap>>(prevKeys);
		
		
		if(uniqueSubElementKeys!=null && uniqueSubElements!=null) {
			for(int i=0;i<uniqueSubElements.size();++i) {
				newPrevKeys.put(getVariable(), new HashSet<BitMap>());
				newPrevKeys.get(getVariable()).addAll(uniqueSubElementKeys.get(i));
				
				uniqueSubElements.get(i).apply(newPrevKeys, oper, newCollection);
				
			}
		}
		else {
			for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
				newPrevKeys.put(getVariable(), new HashSet<BitMap>());
				newPrevKeys.get(getVariable()).add(e.getKey());
				
				e.getValue().apply(newPrevKeys, oper, newCollection);
			}
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
	
	public void apply(HashMap<DDVariable,HashSet<BitMap>> prevKeys, BinaryOperation oper, ArrayList<ImmutableDDElement> otherCollections, MutableDDElement newCollection) {
		HashMap<DDVariable,HashSet<BitMap>> newPrevKeys = new HashMap<DDVariable,HashSet<BitMap>>(prevKeys);
		
		
		if(uniqueSubElementKeys!=null && uniqueSubElements!=null) {
			for(int i=0;i<uniqueSubElements.size();++i) {
				newPrevKeys.put(getVariable(), new HashSet<BitMap>());
				newPrevKeys.get(getVariable()).addAll(uniqueSubElementKeys.get(i));
				
				uniqueSubElements.get(i).apply(newPrevKeys, oper, otherCollections, newCollection);
				
			}
		}
		else {
			for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
				newPrevKeys.put(getVariable(), new HashSet<BitMap>());
				newPrevKeys.get(getVariable()).add(e.getKey());
				
				e.getValue().apply(newPrevKeys, oper, otherCollections, newCollection);
			}
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
		
	}
	
	public void primeVariables(ArrayList<DDVariable> prefixVars, BitMap prefix,MutableDDElement newColl) {
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
		
	}
	
	public MutableDDElement restrict(Map<DDVariable,Integer> elimVarValues){
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
		apply(new HashMap<DDVariable, HashSet<BitMap>>(),oper,coll);
		return coll;
	}
	
	public MutableDDNode apply(BinaryOperation oper, ImmutableDDElement otherColl) {
		MutableDDNode coll = new MutableDDNode(getVariables(), otherColl.isMeasure() && isMeasure);
		ArrayList<ImmutableDDElement> otherCollections = new ArrayList<ImmutableDDElement>();
		otherCollections.add(otherColl);
		apply(new ArrayList<DDVariable>(),null,oper,otherCollections,coll);
		
		//apply(new HashMap<DDVariable, HashSet<BitMap>>(),oper,otherCollections,coll);
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
	}
	
	@Override
	public boolean isMeasure() {
		return isMeasure;
	}

	@Override
	public Double getTotalWeight() {
		double val = 0.0f;
		for(ImmutableDDElement hdrc:subCollections.values()) {
			val += hdrc.getTotalWeight();
		}
		
		return new Double(val);
	}
	
	public String toString(String spacer) {
		String str = "";
		ArrayList<ImmutableDDElement> doneCollections = new ArrayList<ImmutableDDElement>();
		ArrayList<HashSet<BitMap>> doneKeys = new ArrayList<HashSet<BitMap>>();
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			int ix = doneCollections.indexOf(e.getValue());
			if(ix>-1) {
				doneKeys.get(ix).add(e.getKey());
			}
			else {
				doneCollections.add(e.getValue());
				HashSet<BitMap> set = new HashSet<BitMap>();
				set.add(e.getKey());
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
			
			if( (subCollections==null && otherHDRC.subCollections!=null) || (subCollections!=null && otherHDRC.subCollections==null) ) {
				return false;
			}
			
			if(subCollections==null && otherHDRC.subCollections==null) {
				return true;
			}
			else if(subCollections!=null && otherHDRC.subCollections.equals(subCollections)) {
				return true;
			}

			return false;
		}
		else {
			return false;
		}
	}
	
}
