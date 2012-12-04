package masg.dd.representations.dag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.operations.BinaryOperation;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public class MutableDDLeaf extends ImmutableDDLeaf implements MutableDDElement{
	public MutableDDLeaf(double value) {
		super(value);
	}
	
	@Override
	public void applySetValue(ArrayList<DDVariable> vars, BitMap r,
			double value, BinaryOperation oper) {
		this.value = oper.invoke(this.value, value);
	}

	@Override
	public void compress() {
		
	}

	@Override
	public void setValue(ArrayList<DDVariable> vars, BitMap r, double value) {
		this.value = new Double(value);
	}

	/*@Override
	public void setValue(HashMap<DDVariable, HashSet<BitMap>> keyMap,
			double value) {
		this.value = new Double(value);
		
	}*/
	
	@Override
	public void setIsMeasure(ArrayList<DDVariable> vars, boolean isMeasure) {
	}

	@Override
	public void setIsMeasure(HashMap<DDVariable, Boolean> map) {
	}

	
}
