package masg.dd.dbn

import masg.dd.AlgebraicDD

class UncertaintyNode extends BayesianNetworkNode {
	
	def conditionalVariables = []
	def posteriorVariables = []
	
	def children = []
	def parents = []
	
	public UncertaintyNode(conditionalVariablesIn, posteriorVariablesIn, int scopeId, Closure c) {
		conditionalVariables = conditionalVariablesIn
		posteriorVariables = posteriorVariablesIn
		
		if(!posteriorVariables) {
			conditionalVariables = []
			posteriorVariables = conditionalVariablesIn
		}
		
		fn = new AlgebraicDD(conditionalVariables + posteriorVariables, scopeId, c, true)
		
	}
	
	public UncertaintyNode(conditionalVariablesIn, posteriorVariablesIn, AlgebraicDD fnIn) {
		conditionalVariables = conditionalVariablesIn
		posteriorVariables = posteriorVariablesIn
		fn = fnIn
	}
}
