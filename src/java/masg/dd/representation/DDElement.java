package masg.dd.representation;

import java.util.HashMap;

import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public interface DDElement  {
	public boolean isMeasure();
	public long getId();
	public Double getTotalWeight();
	public Double getValue(HashMap<DDVariable,Integer> path);
	public DDVariable getVariable();
	public DDVariableSpace getVariables();
	public boolean equals(Object o);
	public String toString(String spacer);
}
