package masg.dd.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.rules.operations.BinaryOperation;
import masg.dd.rules.operations.UnaryOperation;
import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public class ImmutableHierarchicalDecisionRuleCollection extends BaseHierarchicalRuleCollection {
	private final HashMap<BitMap,ImmutableHierarchicalDecisionRuleCollection> subCollections;
	private final HashMap<BitMap,Double> values;
	
	private final DDVariable var;
	private final boolean isMeasure;
	
	public ImmutableHierarchicalDecisionRuleCollection(HierarchicalDecisionRuleCollection mutableCollection, boolean isMeasure) {
		this.isMeasure = isMeasure;
		var = mutableCollection.getVariable();
		
		if(mutableCollection.subCollections!=null) {
			subCollections = new HashMap<BitMap,ImmutableHierarchicalDecisionRuleCollection>();
			for(Entry<BitMap,HierarchicalDecisionRuleCollection> e:mutableCollection.subCollections.entrySet()) {
				subCollections.put(e.getKey(), new ImmutableHierarchicalDecisionRuleCollection(e.getValue(), isMeasure));
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
				
				BitMap bm;
				if(prefix!=null) {
					bm = new BitMap(prefix.size() + getVariable().getBitCount());
					bm.or(prefix);
					bm.or(prefix.size(),e.getKey());
				}
				else {
					bm = new BitMap(getVariable().getBitCount());
					bm.or(e.getKey());
				}
				
				e.getValue().apply(newPrefixVars, bm, oper, newCollection);
			}
		}
		if(values!=null) {
			for(Entry<BitMap,Double> e: values.entrySet()) {
				
				BitMap bm;
				if(prefix!=null) {
					bm = new BitMap(prefix.size() + getVariable().getBitCount());
					bm.or(prefix);
					bm.or(prefix.size(),e.getKey());
				}
				else {
					bm = new BitMap(getVariable().getBitCount());
					bm.or(e.getKey());
				}
				
				newCollection.setValue(newPrefixVars, bm, oper.invoke(e.getValue()));
			}
		}
	}
	
	private void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, BinaryOperation oper, ImmutableHierarchicalDecisionRuleCollection otherCollection, HierarchicalDecisionRuleCollection newCollection) {
		
		ArrayList<DDVariable> newPrefixVars = new ArrayList<DDVariable>(prefixVars);
		newPrefixVars.add(getVariable());
		
		if(subCollections!=null) {
			for(Entry<BitMap,ImmutableHierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
				
				BitMap bm;
				if(prefix!=null) {
					bm = new BitMap(prefix.size() + getVariable().getBitCount());
					bm.or(prefix);
					bm.or(prefix.size(),e.getKey());
				}
				else {
					bm = new BitMap(getVariable().getBitCount());
					bm.or(e.getKey());
				}
				
				e.getValue().apply(newPrefixVars, bm, oper, otherCollection, newCollection);
			}
		}
		if(values!=null) {
			for(Entry<BitMap,Double> e: values.entrySet()) {
				
				BitMap bm;
				if(prefix!=null) {
					bm = new BitMap(prefix.size() + getVariable().getBitCount());
					bm.or(prefix);
					bm.or(prefix.size(),e.getKey());
				}
				else {
					bm = new BitMap(getVariable().getBitCount());
					bm.or(e.getKey());
				}
				Double valOther = otherCollection.getValue(newPrefixVars, bm);
				
				newCollection.setValue(newPrefixVars, bm, oper.invoke(e.getValue(), valOther));
			}
		}
	}
	
	public HierarchicalDecisionRuleCollection apply(UnaryOperation oper) {
		HierarchicalDecisionRuleCollection coll = new HierarchicalDecisionRuleCollection(getVariables());
		apply(new ArrayList<DDVariable>(),null,oper,coll);
		return coll;
	}
	
	public HierarchicalDecisionRuleCollection apply(BinaryOperation oper, ImmutableHierarchicalDecisionRuleCollection otherColl) {
		HierarchicalDecisionRuleCollection coll = new HierarchicalDecisionRuleCollection(getVariables());
		apply(new ArrayList<DDVariable>(),null,oper,otherColl,coll);
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
		return toString("");
	}
}
