package masg.dd.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.context.DDContext;
import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public class HierarchicalDecisionRuleCollection extends BaseHierarchicalRuleCollection {
	public HashMap<BitMap,HierarchicalDecisionRuleCollection> subCollections = null;
	public HashMap<BitMap,Double> values = null;
	
	private DDVariable var;
	
	public HierarchicalDecisionRuleCollection(ArrayList<DDVariable> vars) {
		
		ArrayList<DDVariable> nextVariables = new ArrayList<DDVariable>(vars);
		
		
		for(DDVariable nextVar:DDContext.canonicalVariableOrdering) {
			if(nextVariables.contains(nextVar)) {
				var = nextVar;
				nextVariables.remove(nextVar);
				break;
			}
		}
		
		
		if(nextVariables.size()>0) {
			subCollections = new HashMap<BitMap,HierarchicalDecisionRuleCollection>();
			for(BitMap bm:variableValuesToBitMapValues(var)) {
				subCollections.put(bm, new HierarchicalDecisionRuleCollection(nextVariables));
			}
		}
		else {
			values = new HashMap<BitMap,Double>();
			for(BitMap bm:variableValuesToBitMapValues(var)) {
				values.put(bm, new Double(0.0f));
			}
		}
	}
	
	
	public DDVariable getVariable() {
		return var;
	}
	
	
	public void setValue(ArrayList<DDVariable> vars, BitMap r, double value) {
		BitMap rVar = extractPivot(vars, r);
		
		if(rVar!=null) {	
			if(subCollections!=null) {
				subCollections.get(rVar).setValue(vars, r, value);
			}
			else {
				values.put(rVar, value);
			}
		}
		else if(subCollections!=null) {
			for(HierarchicalDecisionRuleCollection hdrc:subCollections.values()) {
				hdrc.setValue(vars, r, value);
			}
		}
		else {
			for(BitMap bm:values.keySet()) {
				values.put(bm, value);
			}
		}
	}
	
	public Double getValue(ArrayList<DDVariable> vars, BitMap r, boolean sumOverMissing) {
		BitMap rVar = extractPivot(vars, r);
		
		if(rVar!=null) {	
			if(subCollections!=null && subCollections.containsKey(rVar)) {
				return subCollections.get(rVar).getValue(vars, r, sumOverMissing);
			}
			if(values!=null && values.containsKey(rVar)) {
				return new Double(values.get(rVar));
			}
		}
		else if(sumOverMissing){
			double val = 0.0f;
			if(subCollections!=null) {
				for(HierarchicalDecisionRuleCollection hdrc:subCollections.values()) {
					val += hdrc.getValue(vars, r, sumOverMissing);
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
	
	protected Double compressIntoDouble() {
		Double val = null;
		if(values!=null && subCollections==null) {
			for(Entry<BitMap,Double> e: values.entrySet()) {
				if(val==null)
					val = e.getValue();
				else if(!val.equals(e.getValue())) {
					return null;
				}
			}
		}
		return val;
	}
	
	public void compress() {
		if(subCollections!=null) {
			for(Entry<BitMap,HierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
				e.getValue().compress();
				Double val = e.getValue().compressIntoDouble();
				if(val!=null) {
					if(values==null) {
						values = new HashMap<BitMap,Double>();
					}
					values.put(e.getKey(), val);
				}
			}
			
			if(values!=null) {
				for(BitMap bm: values.keySet()) {
					subCollections.remove(bm);
				}
			}
			
			if(subCollections.size()==0) {
				subCollections = null;
			}
			
			if(values == null && subCollections!=null) {
				HierarchicalDecisionRuleCollection sameHDRC = null;
				for(Entry<BitMap,HierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
					if(sameHDRC==null) {
						sameHDRC = e.getValue();
					}
					else if(!sameHDRC.equals(e.getValue())) {
						sameHDRC = null;
						break;
					}
				}
				
				if(sameHDRC!=null) {
					this.var = sameHDRC.var;
					this.subCollections = sameHDRC.subCollections;
					this.values = sameHDRC.values;
				}
			}
		}
	}
	
	private String toString(String spacer) {
		String str = "";
		if(subCollections!=null) {
			for(Entry<BitMap,HierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
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
	
	private ArrayList<DDVariable> getVariables() {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
		if(subCollections!=null) {
			for(HierarchicalDecisionRuleCollection v: subCollections.values()) {
				vars.addAll(v.getVariables());
				break;
			}
		}
		vars.add(var);
		return vars;
	}
	
	public String toString() {
		return getVariables() + "\n" + toString("");
	}
	
	public boolean equals(Object o) {
		if(o instanceof HierarchicalDecisionRuleCollection) {
			HierarchicalDecisionRuleCollection otherHDRC = (HierarchicalDecisionRuleCollection) o;
			
			if( (values==null && otherHDRC.values!=null) || (values!=null && otherHDRC.values==null) )
				return false;
			
			if( (subCollections==null && otherHDRC.subCollections!=null) || (subCollections!=null && otherHDRC.subCollections==null) )
				return false;
			
			boolean bValuesEqual = false;
			if(values==null && otherHDRC.values==null) {
				bValuesEqual = true;
			}
			else if(values!=null && otherHDRC.values.equals(values)) {
				bValuesEqual = true;
			}
			
			if(bValuesEqual) {
				if(subCollections==null && otherHDRC.subCollections==null) {
					return true;
				}
				else if(subCollections!=null && otherHDRC.subCollections.equals(subCollections)) {
					return true;
				}
			}
			
			return false;
		}
		else {
			return o.equals(this);
		}
	}
}