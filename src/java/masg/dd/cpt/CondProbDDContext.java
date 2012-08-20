package masg.dd.cpt;

import masg.dd.DecisionDiagramContext;
import masg.dd.vars.DDVariableSpace;

public class CondProbDDContext extends DecisionDiagramContext {

	DDVariableSpace inVarSpace, outVarSpace;
	
	public CondProbDDContext(DDVariableSpace inVarSpace, DDVariableSpace outVarSpace) {
		this.inVarSpace = inVarSpace;
		this.outVarSpace = outVarSpace;
		this.varSpace = inVarSpace.plus(outVarSpace);
	}
	
	public DDVariableSpace getInputVarSpace() {
		return inVarSpace;
	}
	
	public DDVariableSpace getOutputVarSpace() {
		return outVarSpace;
	}

}
