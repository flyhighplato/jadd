package masg.dd.context;

import masg.dd.vars.DDVariableSpace;

public class DecisionDiagramContext {
	protected DDVariableSpace varSpace = new DDVariableSpace();
	double tolerance = 0.0001f;
	
	protected DecisionDiagramContext() {
		
	}
	
	public DecisionDiagramContext(DDVariableSpace varSpace) {
		this.varSpace = varSpace;
	}
	
	public final DDVariableSpace getVariableSpace() {
		return varSpace;
	}
}
