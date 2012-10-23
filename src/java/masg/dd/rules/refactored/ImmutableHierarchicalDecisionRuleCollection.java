package masg.dd.rules.refactored;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import masg.dd.rules.operations.refactored.BinaryOperation;
import masg.dd.rules.operations.refactored.UnaryOperation;
import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public class ImmutableHierarchicalDecisionRuleCollection extends BaseHierarchicalRuleCollection {
	private final HashMap<BitMap,ImmutableHierarchicalDecisionRuleCollection> subCollections;
	private final HashMap<BitMap,Double> values;
	
	private final DDVariable var;
	private final boolean isMeasure;
	
	public ImmutableHierarchicalDecisionRuleCollection(HierarchicalDecisionRuleCollection mutableCollection) {
		this.isMeasure = mutableCollection.isMeasure;
		var = mutableCollection.getVariable();
		
		if(mutableCollection.subCollections!=null) {
			subCollections = new HashMap<BitMap,ImmutableHierarchicalDecisionRuleCollection>();
			for(Entry<BitMap,HierarchicalDecisionRuleCollection> e:mutableCollection.subCollections.entrySet()) {
				subCollections.put(e.getKey(), new ImmutableHierarchicalDecisionRuleCollection(e.getValue()));
			}
		}
		else {
			subCollections = null;
		}
		
		if(mutableCollection.values!=null) {
			values = new HashMap<BitMap,Double>();
			for(Entry<BitMap,Double> e:mutableCollection.values.entrySet()) {
				values.put(e.getKey(), e.getValue());
			}
		}
		else {
			values = null;
		}
	}

	@Override
	public DDVariable getVariable() {
		return var;
	}
	
	private ArrayList<DDVariable> getVariables() {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		if(subCollections!=null) {
			for(ImmutableHierarchicalDecisionRuleCollection v: subCollections.values()) {
				vars.addAll(v.getVariables());
				break;
			}
		}
		vars.add(var);
		return vars;
	}

	public Double getValue(ArrayList<DDVariable> vars, BitMap r) {
		BitMap rVar = extractPivot(vars, r);
		
		if(rVar!=null) {	
			if(subCollections!=null && subCollections.containsKey(rVar)) {
				return subCollections.get(rVar).getValue(vars, r);
			}
			if(values!=null && values.containsKey(rVar)) {
				return new Double(values.get(rVar));
			}
		}
		else if(isMeasure){
			double val = 0.0f;
			if(subCollections!=null) {
				for(ImmutableHierarchicalDecisionRuleCollection hdrc:subCollections.values()) {
					val += hdrc.getValue(vars, r);
				}
			}
			if(values!=null) {
				
				for(BitMap bm:values.keySet()) {
					val+=values.get(bm);
				}
				
			}
			return new Double(val);
		}
		
		return null;
	}
	
	
	private void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, UnaryOperation oper, HierarchicalDecisionRuleCollection newCollection) {
		
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		if(subCollections!=null) {
			for(Entry<BitMap,ImmutableHierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
				BitMap bm = joinKeys(prefix, e.getKey());
				e.getValue().apply(newPrefixVars, bm, oper, newCollection);
			}
		}
		if(values!=null) {
			for(Entry<BitMap,Double> e: values.entrySet()) {
				BitMap bm = joinKeys(prefix, e.getKey());
				newCollection.setValue(newPrefixVars, bm, oper.invoke(e.getValue()));
			}
		}
	}
	
	private void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, BinaryOperation oper, ArrayList<ImmutableHierarchicalDecisionRuleCollection> otherCollections, HierarchicalDecisionRuleCollection newCollection) {
		
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		if(subCollections!=null) {
			for(Entry<BitMap,ImmutableHierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
				BitMap bm = joinKeys(prefix, e.getKey());
				e.getValue().apply(newPrefixVars, bm, oper, otherCollections, newCollection);
			}
		}
		if(values!=null) {
			for(Entry<BitMap,Double> e: values.entrySet()) {
				BitMap bm = joinKeys(prefix, e.getKey());
				
				Double val = e.getValue();
				for(ImmutableHierarchicalDecisionRuleCollection otherIHDRC:otherCollections) {
					val = oper.invoke(val, otherIHDRC.getValue(newPrefixVars,bm));
				}
				newCollection.setValue(newPrefixVars, bm, val);
			}
		}
	}
	
	private void copy(ArrayList<DDVariable> prefixVars, BitMap prefix, BinaryOperation oper, HierarchicalDecisionRuleCollection newCollection) {
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		if(subCollections!=null) {
			for(Entry<BitMap,ImmutableHierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
				BitMap bm = joinKeys(prefix, e.getKey());
				e.getValue().copy(newPrefixVars, bm, oper, newCollection);
			}
		}
		if(values!=null) {
			for(Entry<BitMap,Double> e: values.entrySet()) {
				BitMap bm = joinKeys(prefix, e.getKey());
				newCollection.applySetValue(newPrefixVars, bm, e.getValue(), oper);
			}
		}
	}
	
	private void restrict(ArrayList<DDVariable> prefixVars, BitMap prefix, ArrayList<DDVariable> restrictVars, BitMap restrictKey, HierarchicalDecisionRuleCollection newCollection) {
		
		BitMap rVar = extractPivot(restrictVars, restrictKey);
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		if(rVar==null) {
			newPrefixVars.add(getVariable());
			
			if(subCollections!=null) {
				for(Entry<BitMap,ImmutableHierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
					BitMap bm = joinKeys(prefix, e.getKey());
					e.getValue().restrict(newPrefixVars, bm, restrictVars, restrictKey, newCollection);
				}
			}
			if(values!=null) {
				for(Entry<BitMap,Double> e: values.entrySet()) {
					BitMap bm = joinKeys(prefix, e.getKey());
					newCollection.setValue(newPrefixVars, bm, e.getValue());
				}
			}
		}
		else {
			if(subCollections!=null) {
				subCollections.get(rVar).restrict(newPrefixVars, prefix, restrictVars, restrictKey, newCollection);
			}
			if(values!=null) {
				newCollection.setValue(newPrefixVars, prefix,values.get(rVar));
			}
		}
	}
	
	
	
	public HierarchicalDecisionRuleCollection eliminateVariables(ArrayList<DDVariable> elimVars, BinaryOperation oper){
		HashSet<DDVariable> haveVars = new HashSet<DDVariable>(getVariables());
		haveVars.removeAll(elimVars);
		HierarchicalDecisionRuleCollection coll = new HierarchicalDecisionRuleCollection(new ArrayList<DDVariable>(haveVars), isMeasure);
		copy(new ArrayList<DDVariable>(),null,oper,coll);
		return coll;
	}
	
	public HierarchicalDecisionRuleCollection restrict(Map<DDVariable,Integer> elimVarValues){
		HashSet<DDVariable> haveVars = new HashSet<DDVariable>(getVariables());
		haveVars.removeAll(elimVarValues.keySet());
		HierarchicalDecisionRuleCollection coll = new HierarchicalDecisionRuleCollection(new ArrayList<DDVariable>(haveVars), isMeasure);
		
		BitMap restrictKey = null;
		ArrayList<DDVariable> restrictVars = new ArrayList<DDVariable>(elimVarValues.keySet());
		for(DDVariable elimVar:restrictVars) {
			BitMap suffix = variableValuetoBitMap(elimVar,elimVarValues.get(elimVar));
			restrictKey = joinKeys(restrictKey,suffix);
		}
		
		restrict(new ArrayList<DDVariable>(),null,restrictVars,restrictKey,coll);
		return coll;
	}
	
	public HierarchicalDecisionRuleCollection apply(UnaryOperation oper) {
		HierarchicalDecisionRuleCollection coll = new HierarchicalDecisionRuleCollection(getVariables(), isMeasure);
		apply(new ArrayList<DDVariable>(),null,oper,coll);
		return coll;
	}
	
	public HierarchicalDecisionRuleCollection apply(BinaryOperation oper, ImmutableHierarchicalDecisionRuleCollection otherColl) {
		HierarchicalDecisionRuleCollection coll = new HierarchicalDecisionRuleCollection(getVariables(), otherColl.isMeasure && isMeasure);
		ArrayList<ImmutableHierarchicalDecisionRuleCollection> otherCollections = new ArrayList<ImmutableHierarchicalDecisionRuleCollection>();
		otherCollections.add(otherColl);
		apply(new ArrayList<DDVariable>(),null,oper,otherCollections,coll);
		return coll;
	}
	
	private String toString(String spacer) {
		String str = "";
		if(subCollections!=null) {
			for(Entry<BitMap,ImmutableHierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
				str += e.getValue().toString(spacer + e.getKey().toString());
			}
		}
		if(values!=null) {
			for(Entry<BitMap,Double> e: values.entrySet()) {
				str += spacer + e.getKey() + ":" + e.getValue() + "\n";
			}
		}
		return str;
	}
	
	public String toString() {
		return getVariables() + "\n" + toString("");
	}
}
