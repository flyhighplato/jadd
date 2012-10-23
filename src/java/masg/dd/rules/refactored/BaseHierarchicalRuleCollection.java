package masg.dd.rules.refactored;

import java.util.ArrayList;

import masg.dd.vars.DDVariable;
import masg.util.BitMap;

abstract public class BaseHierarchicalRuleCollection {
	abstract public DDVariable getVariable();
	
	protected ArrayList<BitMap> variableValuesToBitMapValues(DDVariable var) {
		ArrayList<BitMap> bitMaps = new ArrayList<BitMap>();
		for(int varValue=0;varValue<var.getValueCount();++varValue) {
			bitMaps.add(variableValuetoBitMap(var,varValue));
		}
		return bitMaps;
	}
	
	protected BitMap variableValuetoBitMap(DDVariable var, int varValue) {
		BitMap bm = new BitMap(var.getBitCount());
		for(int currBitIndex = 0; currBitIndex<var.getBitCount();++currBitIndex) {
			boolean setInVarValue = ((varValue & (1 << currBitIndex)) > 0);
			if(setInVarValue) {
				bm.set(currBitIndex);
			}
		}
		return bm;
	}
	
	protected BitMap extractPivot(ArrayList<DDVariable> vars, BitMap r) {
		int startBitIx = 0;
		
		int ixOfVar = vars.indexOf(getVariable());
		for(int ix=0;ix<ixOfVar;++ix) {
			startBitIx+=vars.get(ix).getBitCount();
		}
		
		if(ixOfVar!=-1) {
			return r.get(startBitIx, getVariable().getBitCount());
		}
		
		return null;
	}
	
	protected BitMap joinKeys(BitMap prefix, BitMap suffix) {
		BitMap bm;
		if(prefix!=null) {
			bm = new BitMap(prefix.size() + suffix.size());
			bm.or(prefix);
			bm.or(prefix.size(),suffix);
		}
		else {
			bm = new BitMap(suffix.size());
			bm.or(suffix);
		}
		
		return bm;
	}
	
}
