package masg.dd.dbn

import masg.dd.AlgebraicDD

class ValueNodeResult extends BayesianNetworkNodeResult {
	def resolvedVariables = []
	
	public ValueNodeResult(ValueNode node) {
		this.node = node
		resultFn = node.fn
	}
	
	public ValueNode getNode() {
		return super.@node
	}
	
	public resolve(BayesianNetworkNodeResult result) {
		resolve(result.resultFn)
	}
	
	public resolve(AlgebraicDD resultFnIn) {
		def varsToResolve = resultFnIn.variables.findAll{ node.variables.contains(it)}
		
		if(!resolvedVariables.containsAll(varsToResolve)) {
			def unusedVariables = resultFnIn.variables.findAll { !node.variables.contains(it) }
			resultFnIn = resultFnIn.sumOut(unusedVariables).normalize()
			
			resultFn = resultFn.multiply(resultFnIn)
			
			resolvedVariables += resultFnIn.variables.findAll{ node.variables.contains(it)}
			resultFn = resultFn.sumOut(resolvedVariables)
		}
	}
	
	public boolean shouldRetain() {
		return true
	}
}
