package masg.dd

import groovy.lang.Closure;
import masg.dd.rules.HierarchicalDecisionRuleCollection
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace
import masg.util.BitMap

class HierarchicalDecisionRuleCollectionBuilder {
	public static HierarchicalDecisionRuleCollection build(ArrayList<DDVariable> vars, Closure c) {
		HierarchicalDecisionRuleCollection hdrc = new HierarchicalDecisionRuleCollection(vars);
		
		int totalBitCount = 0;
		vars.each{totalBitCount+=it.getBitCount()}
		
		DDVariableSpace varSpace = new DDVariableSpace(vars);
		varSpace.each{ HashMap<DDVariable,Integer> varSpacePoint ->
			double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
			
			BitMap bm = varSpacePointToBitMap(vars,varSpacePoint);

			hdrc.setValue(vars, bm, val)
		}
		
		return hdrc;
	}
	
	public static BitMap varSpacePointToBitMap(ArrayList<DDVariable> vars, HashMap<DDVariable,Integer> varSpacePoint) {
		int totalBitCount = 0;
		vars.each{totalBitCount+=it.getBitCount()}
		
		BitMap bm = new BitMap(totalBitCount);
		
		int varBitIndexOffset=0;
		for(DDVariable var:vars) {
			int varValue = varSpacePoint.get(var);
			
			for(int currBitIndex = varBitIndexOffset; (currBitIndex - varBitIndexOffset) <var.getBitCount();++currBitIndex) {
				int valueBitIndex = currBitIndex - varBitIndexOffset;
				boolean setInVarValue = ((varValue & (1 << valueBitIndex)) > 0);
				if(setInVarValue) {
					bm.set(currBitIndex);
				}
			}
			
			varBitIndexOffset+=var.getBitCount();
		}
		
		return bm;
	}
}
