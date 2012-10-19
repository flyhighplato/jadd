package masg.dd.context;

import java.util.ArrayList;

import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;

public class DDContext {
	protected DDVariableSpace varSpace = new DDVariableSpace();
	double tolerance = 0.0001f;
	
	public static ArrayList<DDVariable> canonicalVariableOrdering = new ArrayList<DDVariable>();
	
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
}
