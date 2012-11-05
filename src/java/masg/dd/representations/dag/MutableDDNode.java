package masg.dd.representations.dag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import masg.dd.operations.BinaryOperation;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public class MutableDDNode extends ImmutableDDNode implements MutableDDElement {
	
	public MutableDDNode(ArrayList<DDVariable> vars, boolean isMeasure) {
		super(vars,isMeasure);
	}
	
	public void applySetValue(ArrayList<DDVariable> vars, BitMap r, double value, BinaryOperation oper) {
		BitMap rVar = extractPivot(vars, r);
		
		if(rVar!=null) {	
			((MutableDDElement) subCollections.get(rVar)).applySetValue(vars, r, value, oper);
		}
		else {
			for(ImmutableDDElement node:subCollections.values()) {
				((MutableDDElement) node).applySetValue(vars, r, value, oper);
			}
		}
		
	}
	
	public void setValue(ArrayList<DDVariable> vars, BitMap r, double value) {
		BitMap rVar = extractPivot(vars, r);
		
		if(rVar!=null) {	
			((MutableDDElement) subCollections.get(rVar)).setValue(vars, r, value);
		}
		else {
			for(ImmutableDDElement hdrc:subCollections.values()) {
				((MutableDDElement) hdrc).setValue(vars, r, value);
			}
		}
		
		
	}
	
	public void setValue(HashMap<DDVariable,HashSet<BitMap>> keyMap, double value) {
		if(keyMap.containsKey(getVariable())) {
			if(uniqueSubElementKeys!=null && uniqueSubElements!=null) {
				for(int i=0;i<uniqueSubElements.size();++i) {
					HashSet<BitMap> elKeys = new HashSet<BitMap>(uniqueSubElementKeys.get(i));
					elKeys.retainAll(keyMap.get(getVariable()));
					
					if(!elKeys.isEmpty()) {
						((MutableDDNode) uniqueSubElements.get(i)).setValue(keyMap, value);
					}
				}
			}
			else {
				for(BitMap key:keyMap.get(getVariable())) {
					((MutableDDElement) subCollections.get(key)).setValue(keyMap, value);
				}
			}
		}
		else {
			if(uniqueSubElementKeys!=null && uniqueSubElements!=null) {
				for(int i=0;i<uniqueSubElements.size();++i) {
					((MutableDDNode) uniqueSubElements.get(i)).setValue(keyMap, value);
				}
			}
			else {
				for(ImmutableDDElement hdrc:subCollections.values()) {
					((MutableDDElement) hdrc).setValue(keyMap, value);
				}
			}
			
		}
	}
	
	public void setIsMeasure(ArrayList<DDVariable> vars, boolean isMeasure) {
		if(vars.contains(this.var)) {
			this.isMeasure = isMeasure;
		}
		
		for(ImmutableDDElement hdrc:subCollections.values()) {
			((MutableDDElement) hdrc).setIsMeasure(vars, isMeasure);
		}
		
		
	}
	
	public void setIsMeasure(HashMap<DDVariable,Boolean> map) {
		if(map.containsKey(this.var)) {
			this.isMeasure = map.get(this.var);
		}
		
		for(ImmutableDDElement hdrc:subCollections.values()) {
			((MutableDDElement) hdrc).setIsMeasure(map);
		}

	}
	
	public void compress() {
		HashMap<BitMap,ImmutableDDElement> newSubNodes = new HashMap<BitMap,ImmutableDDElement>();
		
		ImmutableDDElement sameHDRC = null;
		
		newSubNodes = new HashMap<BitMap,ImmutableDDElement>();
		for(Entry<BitMap, ImmutableDDElement> e: subCollections.entrySet()) {
			if(sameHDRC==null) {
				sameHDRC = e.getValue();
			}
			else if(!sameHDRC.equals(e.getValue())) {
				sameHDRC = null;
				break;
			}
			
			newSubNodes.put(e.getKey(), e.getValue());
		}

		if(sameHDRC!=null) {
			subCollections = newSubNodes;
		}
		
		
		uniqueSubElements = new ArrayList<ImmutableDDElement>();
		uniqueSubElementKeys = new ArrayList<HashSet<BitMap>>();
		
		for(Entry<BitMap,ImmutableDDElement> e: subCollections.entrySet()) {
			int ix = uniqueSubElements.indexOf(e.getValue());
			if(ix>-1) {
				uniqueSubElementKeys.get(ix).add(e.getKey());
			}
			else {
				uniqueSubElements.add(e.getValue());
				HashSet<BitMap> set = new HashSet<BitMap>();
				set.add(e.getKey());
				uniqueSubElementKeys.add(set);
			}
		}

	}
}
