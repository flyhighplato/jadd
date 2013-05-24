package masg.dd.dbn

import masg.dd.AlgebraicDD
import masg.dd.variables.DDVariable

class BayesianNetworkNodeResult {
	BayesianNetworkNode node
	
	AlgebraicDD resultFn
	
	public boolean shouldRetain() {
		return false
	}
}
