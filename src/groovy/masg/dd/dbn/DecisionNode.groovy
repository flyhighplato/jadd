package masg.dd.dbn

import masg.dd.AlgebraicDD
import masg.dd.variables.DDVariable

class DecisionNode extends BayesianNetworkNode {
	def variables = []
	def children = []
	
	public DecisionNode(variablesIn, int scopeId, Closure c) {
		variables = variablesIn
		fn = new AlgebraicDD(variablesIn, scopeId, c, true)
	}
	
	public DecisionNode(variablesIn, int scopeId) {
		variables = variablesIn
		
		int varValues = 1
		variablesIn.each { DDVariable v ->
			varValues *= v.numValues
		}
		fn = new AlgebraicDD(variablesIn, scopeId, {argsVars -> 1.0d/varValues }, true)
	}
	
	public DecisionNode(variablesIn, AlgebraicDD fnIn) {
		variables = variablesIn
		fn = fnIn
	}
}
