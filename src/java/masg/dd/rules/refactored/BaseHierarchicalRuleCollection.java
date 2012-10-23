package masg.dd.rules.refactored;

import java.util.ArrayList;

import masg.dd.vars.DDVariable;
import masg.util.BitMap;

abstract public class BaseHierarchicalRuleCollection {
	abstract public DDVariable getVariable();
	
	protected ArrayList<BitMap> variableValuesToBitMapValues(DDVariable var) {
		ArrayList<BitMap> bitMaps = new ArrayList<BitMap>();
		for(int varValue=0;varValue<var.getValueCount();++varValue) {
			BitMap bm = new BitMap(var.getBitCount());
			for(int currBitIndex = 0; currBitIndex<var.getBitCount();++currBitIndex) {
				boolean setInVarValue = ((varValue & (1 << currBitIndex)) > 0);
				if(setInVarValue) {
					bm.set(currBitIndex);
				}
			}
			bitMaps.add(bm);
		}
		return bitMaps;
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
}
