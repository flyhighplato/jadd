package masg.dd.vars;

import java.util.HashMap;
import java.util.Iterator;

public class DDVariableSpaceIterator implements Iterator<HashMap<DDVariable,Integer>> {
	protected DDVariableSpace varSpace;
	protected int[] varIndices;
	boolean overflow = false;
	
	HashMap<DDVariable, Integer> nextVarSpacePoint = new HashMap<DDVariable, Integer>();
	
	public DDVariableSpaceIterator(DDVariableSpace varSpace) {
		this.varSpace = varSpace;
		this.varIndices = new int[varSpace.getVariableCount()];
		
		
		for(int i = varIndices.length-1;i>=0;i--) {
			nextVarSpacePoint.put(varSpace.getVariable(i), varIndices[i]);
		}
	}

	@Override
	public boolean hasNext() {
		return !overflow;
	}

	@Override
	public final HashMap<DDVariable, Integer> next() {
		if(overflow)
			return null;
		
		@SuppressWarnings("unchecked")
		HashMap<DDVariable, Integer> currVarSpacePoint = (HashMap<DDVariable, Integer>) nextVarSpacePoint.clone();
		
		for(int i = varIndices.length-1;i>=0;i--) {
			if(varIndices[i] < (varSpace.getVariable(i).getValueCount()-1)) {
				varIndices[i]++;
				nextVarSpacePoint.put(varSpace.getVariable(i), varIndices[i]);
				break;
			}
			else {
				varIndices[i]=0;
				nextVarSpacePoint.put(varSpace.getVariable(i), varIndices[i]);
				
				if(i==0) {
					overflow = true;
				}
			}
		}
		
		return currVarSpacePoint;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Removing a point in the variable space is unsupported.");
	}
}
