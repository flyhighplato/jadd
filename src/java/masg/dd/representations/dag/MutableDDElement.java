package masg.dd.representations.dag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.operations.BinaryOperation;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public interface MutableDDElement extends ImmutableDDElement {
	public void applySetValue(ArrayList<DDVariable> vars, BitMap r, double value, BinaryOperation oper);
	public void compress();
	//public void setValue(HashMap<DDVariable,HashSet<BitMap>> keyMap, double value);
	public void setValue(ArrayList<DDVariable> vars, BitMap r, double value);
	public void setIsMeasure(ArrayList<DDVariable> vars, boolean isMeasure);
	public void setIsMeasure(HashMap<DDVariable,Boolean> map);
}
