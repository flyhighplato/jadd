package masg.dd.rules.refactored;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import masg.dd.rules.operations.refactored.BinaryOperation;
import masg.dd.rules.operations.refactored.UnaryOperation;
import masg.dd.vars.DDVariable;
import masg.util.BitMap;

public interface ImmutableDDElement {
	public void getIsMeasure(HashMap<DDVariable,Boolean> map);
	public HashMap<DDVariable,Boolean> getIsMeasure();
	public boolean isMeasure();
	public Double getTotalWeight();
	public Double getValue(ArrayList<DDVariable> vars, BitMap r);
	public DDVariable getVariable();
	public ArrayList<DDVariable> getVariables();
	public boolean equals(Object o);
	public String toString(String spacer);
	public MutableDDElement apply(UnaryOperation oper);
	public MutableDDElement apply(BinaryOperation oper, ImmutableDDElement otherColl);
	public MutableDDElement apply(BinaryOperation oper, ArrayList<ImmutableDDElement> otherColl);
	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, UnaryOperation oper, MutableDDElement newCollection);
	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, BinaryOperation oper, ArrayList<ImmutableDDElement> otherCollections, MutableDDElement newCollection);
	public void copy(ArrayList<DDVariable> prefixVars, BitMap prefix, BinaryOperation oper, MutableDDElement newCollection);
	public void restrict(ArrayList<DDVariable> prefixVars, BitMap prefix, ArrayList<DDVariable> restrictVars, BitMap restrictKey, MutableDDElement newCollection);
	public MutableDDElement restrict(Map<DDVariable,Integer> elimVarValues);
	public MutableDDElement eliminateVariables(ArrayList<DDVariable> elimVars, BinaryOperation oper);
}
