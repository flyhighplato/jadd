package masg.dd.representation;

import java.util.Collection;
import java.util.HashSet;

import masg.dd.variables.DDVariable;

public class DDInfo {
	HashSet<DDVariable> variables;
	boolean isMeasure = false;
	
	public DDInfo(Collection<DDVariable> ddVariables, boolean isMeasure) {
		updateInfo(ddVariables,isMeasure);
	}
	public HashSet<DDVariable> getVariables() {
		return variables;
	}
	public boolean isMeasure() {
		return isMeasure;
	}
	
	public void updateInfo(Collection<DDVariable> ddVariables, boolean isMeasure) {
		variables = new HashSet<DDVariable>(ddVariables);
		this.isMeasure = isMeasure;
	}
}
