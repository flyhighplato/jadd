package masg.dd.representations.dag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import masg.dd.operations.BinaryOperation;
import masg.dd.operations.UnaryOperation;
import masg.dd.variables.DDVariable;
import masg.util.BitMap;

public interface ImmutableDDElement  {
	public void getIsMeasure(HashMap<DDVariable,Boolean> map);
	public HashMap<DDVariable,Boolean> getIsMeasure();
	public boolean isMeasure();
	public Double getTotalWeight();
	public Double getValue(ArrayList<DDVariable> vars, BitMap r);
	public ArrayList<Double> getValues(HashMap<DDVariable,HashSet<BitMap>> keyMap);
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
	public MutableDDElement unprimeVariables();
	public void unprimeVariables(ArrayList<DDVariable> prefixVars, BitMap prefix,MutableDDElement newColl);
	public MutableDDElement primeVariables();
	public void primeVariables(ArrayList<DDVariable> prefixVars, BitMap prefix,MutableDDElement newColl);
	public void restrict(ArrayList<DDVariable> prefixVars, BitMap prefix, ArrayList<DDVariable> restrictVars, BitMap restrictKey, MutableDDElement newCollection);
	public MutableDDElement restrict(Map<DDVariable,Integer> elimVarValues);
	public MutableDDElement eliminateVariables(ArrayList<DDVariable> elimVars, BinaryOperation oper);
}
