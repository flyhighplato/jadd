package masg.dd.representation;

import masg.dd.variables.DDVariableSpace;

public class DDInfo {
	DDVariableSpace variables;
	boolean isMeasure = false;
	
	public DDInfo(DDVariableSpace ddVariables, boolean isMeasure) {
		updateInfo(ddVariables,isMeasure);
	}
	public DDVariableSpace getVariables() {
		return variables;
	}
	public boolean isMeasure() {
		return isMeasure;
	}
	
	public void updateInfo(DDVariableSpace ddVariables, boolean isMeasure) {
		variables = new DDVariableSpace(ddVariables);
		this.isMeasure = isMeasure;
	}
}
