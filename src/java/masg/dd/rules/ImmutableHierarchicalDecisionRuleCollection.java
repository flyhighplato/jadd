package masg.dd.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public class ImmutableHierarchicalDecisionRuleCollection extends BaseHierarchicalRuleCollection {
	private final HashMap<BitMap,ImmutableHierarchicalDecisionRuleCollection> subCollections;
	private final HashMap<BitMap,Double> values;
	
	private final DDVariable var;
	
	public ImmutableHierarchicalDecisionRuleCollection(HierarchicalDecisionRuleCollection mutableCollection) {
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
				for(ImmutableHierarchicalDecisionRuleCollection hdrc:subCollections.values()) {
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
	
}
