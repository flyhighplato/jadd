package masg.dd.rules.refactored;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.rules.operations.refactored.BinaryOperation;
import masg.dd.vars.DDVariable;
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
	
	public void setIsMeasure(ArrayList<DDVariable> vars, boolean isMeasure) {
		if(vars.contains(this.var)) {
			this.isMeasure = isMeasure;
		}
		
		if(subCollections!=null) {
			for(ImmutableDDElement hdrc:subCollections.values()) {
				((MutableDDElement) hdrc).setIsMeasure(vars, isMeasure);
			}
		}
		
	}
	
	public void setIsMeasure(HashMap<DDVariable,Boolean> map) {
		if(map.containsKey(this.var)) {
			this.isMeasure = map.get(this.var);
		}
		
		if(subCollections!=null) {
			for(ImmutableDDElement hdrc:subCollections.values()) {
				((MutableDDElement) hdrc).setIsMeasure(map);
			}
		}
		
	}

	/*public Double compressIntoDouble() {
		Double val = null;
		if(subCollections==null) {
			for(Entry<BitMap, ImmutableDDElement> e: subCollections.entrySet()) {
				if(e.getValue() instanceof MutableDDLeaf) {
					MutableDDLeaf leaf = (MutableDDLeaf) e.getValue();
					
					if(val==null)
						val = leaf.getValue();
					else if(!val.equals(e.getValue())) {
						return null;
					}
				}
				else {
					return null;
				}
			}
		}
		return val;
	}*/
	
	public void compress() {
		if(subCollections!=null) {
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
				System.out.println("Compressed more");
				subCollections = newSubNodes;
			}
		}
	}
}
