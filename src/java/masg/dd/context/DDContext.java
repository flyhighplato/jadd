package masg.dd.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public class DDContext {
	protected DDVariableSpace varSpace = new DDVariableSpace();
	double tolerance = 0.0001f;
	
	private static ArrayList<DDVariable> canonicalVariableOrdering = new ArrayList<DDVariable>();
	private static HashMap<DDVariable,Integer> varIndices = null;
	
	
	public static ArrayList<DDVariable> getCanonicalVariableOrdering() {
		return canonicalVariableOrdering;
	}
	
	public static void setCanonicalVariableOrdering(ArrayList<DDVariable> newVariableOrdering) {

		newVariableOrdering.removeAll(canonicalVariableOrdering);
		
		canonicalVariableOrdering.addAll(newVariableOrdering);
		
		varIndices = new HashMap<DDVariable,Integer>();
		
		for(int i=0;i<canonicalVariableOrdering.size();++i) {
			varIndices.put(canonicalVariableOrdering.get(i), i);
		}
	}
	
	protected DDContext() {
		
	}
	
	public DDContext(DDVariableSpace varSpace) {
		this.varSpace = new DDVariableSpace(varSpace.getVariables());
	}
	
	public DDContext(ArrayList<DDVariable> variables) {
		this.varSpace = new DDVariableSpace(variables);
	}
	
	public final DDVariableSpace getVariableSpace() {
		return varSpace;
	}
	
	public ArrayList<DDVariable> getVariables() {
		return varSpace.getVariables();
	}
	
	public static int getVariableIndex(DDVariable v) {
		return varIndices.get(v);
	}
	

}
