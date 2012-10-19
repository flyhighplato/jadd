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
			if(subCollections!=null) {
				return subCollections.get(rVar).getValue(vars, r, sumOverMissing);
			}
			else {
				return values.get(rVar);
			}
		}
		else if(sumOverMissing){
			if(subCollections!=null) {
				double val = 0.0f;
				
				for(HierarchicalDecisionRuleCollection hdrc:subCollections.values()) {
					val += hdrc.getValue(vars, r, sumOverMissing);
				}
				
				return val;
			}
			else {
				double val = 0.0f;
				for(BitMap bm:values.keySet()) {
					val+=values.get(bm);
				}
				return val;
			}
		}
		
		return null;
	}
	
	private String toString(String spacer) {
		String str = "";
		if(subCollections!=null) {
			for(Entry<BitMap,HierarchicalDecisionRuleCollection> e: subCollections.entrySet()) {
				str += e.getValue().toString(spacer + e.getKey().toString());
			}
		}
		else {
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
