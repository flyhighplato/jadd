package masg.dd.dbn

import masg.dd.AlgebraicDD

class ValueNode extends BayesianNetworkNode {
	def parents = []
	def variables = []
	
	public ValueNode(variablesIn, int scopeId, Closure c) {
		variables = variablesIn
		fn = new AlgebraicDD(variablesIn, scopeId, c, true)
	}
	
	public ValueNode(variablesIn, AlgebraicDD fnIn) {
		variables = variablesIn
		fn = fnIn
	}
}
