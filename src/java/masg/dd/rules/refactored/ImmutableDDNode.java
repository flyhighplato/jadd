package masg.dd.rules.refactored;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.rules.operations.refactored.BinaryOperation;
import masg.dd.rules.operations.refactored.UnaryOperation;
import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public class ImmutableDDNode extends BaseDDNode implements ImmutableDDElement {
	protected HashMap<BitMap,ImmutableDDElement> subCollections;
	
	protected DDVariable var;
	protected boolean isMeasure;
	
	public ImmutableDDNode(MutableDDNode mutableCollection) {
		this.isMeasure = mutableCollection.isMeasure;
		var = mutableCollection.getVariable();
		subCollections = new HashMap<BitMap,ImmutableDDElement>();
		
		for(Entry<BitMap, ImmutableDDElement> e:mutableCollection.subCollections.entrySet()) {
			if(e.getValue() instanceof MutableDDNode)
				subCollections.put(e.getKey(), new ImmutableDDNode((MutableDDNode) e.getValue()));
			else if(e.getValue() instanceof MutableDDLeaf)
				subCollections.put(e.getKey(), new ImmutableDDLeaf((MutableDDLeaf) e.getValue()));
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
			if(subCollections!=null && subCollections.containsKey(rVar)) {
				return subCollections.get(rVar).getValue(vars, r);
			}
		}
		else if(isMeasure){
			double val = 0.0f;
			for(ImmutableDDElement hdrc:subCollections.values()) {
				val += hdrc.getValue(vars, r);
			}
			
			return new Double(val);
		}
		
		return null;
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
		apply(new ArrayList<DDVariable>(),null,oper,coll);
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
		return getVariables() + "\n" + toString("");
	}

	public boolean equals(Object o) {
		if(o==this)
			return true;
		
		if(o instanceof MutableDDNode) {
			MutableDDNode otherHDRC = (MutableDDNode) o;
			
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
			return o.equals(this);
		}
	}
	
}
