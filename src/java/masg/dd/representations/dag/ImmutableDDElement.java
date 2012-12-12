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
	public boolean isMeasure();
	public Double getTotalWeight();
	public Double getValue(HashMap<DDVariable,Integer> path);
	public DDVariable getVariable();
	public HashSet<DDVariable> getVariables();
	public boolean equals(Object o);
	public String toString(String spacer);
}
