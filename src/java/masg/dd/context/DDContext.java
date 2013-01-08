package masg.dd.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public class DDContext {
	protected DDVariableSpace varSpace = null;
	double tolerance = 0.0001f;
	
	public static ArrayList<DDVariable> canonicalVariableOrdering = new ArrayList<DDVariable>();
	
	public DDContext(DDVariableSpace varSpace) {
		this.varSpace = new DDVariableSpace(varSpace.getVariables());
	}
	
	public DDContext(ArrayList<DDVariable> variables) {
		this.varSpace = new DDVariableSpace(variables);
	}
	
	public final DDVariableSpace getVariableSpace() {
		return varSpace;
	}
	
	public Collection<DDVariable> getVariables() {
		return varSpace.getVariables();
	}
	
	private static HashMap<DDVariable,Integer> varIndices = null;
	public static int getVariableIndex(DDVariable v) {
		if(varIndices==null) {
			varIndices = new HashMap<DDVariable,Integer>();
			for(int i=0;i<canonicalVariableOrdering.size();++i) {
				varIndices.put(canonicalVariableOrdering.get(i), i);
			}
		}
		
		return varIndices.get(v);
	}
}
