package masg.dd.refactored

import masg.dd.rules.refactored.MutableDDElement
import masg.dd.rules.refactored.MutableDDLeaf
import masg.dd.rules.refactored.MutableDDNode
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace
import masg.util.BitMap

class MutableDDElementBuilder {
	public static MutableDDElement build(ArrayList<DDVariable> vars, Closure<Double> c, boolean isMeasure) {
		MutableDDNode hdrc = new MutableDDNode(vars, isMeasure);
		
		int totalBitCount = 0;
		vars.each{totalBitCount+=it.getBitCount()}
		
		DDVariableSpace varSpace = new DDVariableSpace(vars);
		varSpace.each{ HashMap<DDVariable,Integer> varSpacePoint ->
			double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
			
			BitMap bm = varSpacePointToBitMap(vars,varSpacePoint);

			hdrc.setValue(vars, bm, val)
		}
		
		if(hdrc.compressIntoDouble() == null) {
			return hdrc;
		}
		else {
			return new MutableDDLeaf(hdrc.compressIntoDouble());
		}
	}
	
	
	public static MutableDDElement buildProbability(ArrayList<DDVariable> vars, Closure<Double>... closures) {
		Closure<Double> c = { Map variables ->
			double val = 1.0f;
			closures.each{ Closure<Double> c ->
				val*=c(variables)
			}
			return val;
		}
		
		return build(vars,c,true);
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
