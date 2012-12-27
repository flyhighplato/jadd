package masg.dd.representation;

import java.util.HashMap;
import java.util.HashSet;

import masg.dd.variables.DDVariable;

public interface DDElement  {
	public boolean isMeasure();
	public long getId();
	public Double getTotalWeight();
	public Double getValue(HashMap<DDVariable,Integer> path);
	public DDVariable getVariable();
	public HashSet<DDVariable> getVariables();
	public boolean equals(Object o);
	public String toString(String spacer);
}
