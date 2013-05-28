package masg.dd.dbn

import masg.dd.AlgebraicDD

class DecisionNodeResult extends BayesianNetworkNodeResult {

	def resolvedVariables = []
	
	public DecisionNodeResult(DecisionNode node) {
		this.node = node
		resultFn = node.fn
	}
	
	public DecisionNode getNode() {
		return super.@node
	}
	
	public resolve(BayesianNetworkNodeResult result) {
		resolve(result.resultFn)
	}
	
	public resolve(UncertaintyNodeResult result) {
		resolve(result.resultFn)
		
		if(!result.isCPT()) {
			def varsToResolve = result.resultFn.variables.findAll{ node.variables.contains(it)}
			
			result.resolvedConditional += varsToResolve
			result.resultFn = result.resultFn.sumOut(varsToResolve)
		}
	}
	
	public resolve(AlgebraicDD resultFnIn) {
		def varsToResolve = resultFnIn.variables.findAll{ node.variables.contains(it)}
		
		AlgebraicDD resIn
		AlgebraicDD resOld = resultFn
		AlgebraicDD resOldTemp
		if(varsToResolve) {
			def unusedVariables = resultFnIn.variables.findAll { !node.variables.contains(it) }
			
			resIn = resultFnIn.sumOut(unusedVariables).normalize()
			resOldTemp = resultFn.multiply(resIn)
			resultFn = resultFn.multiply(resIn).normalize()
		}
		
		if(!resolvedVariables.containsAll(varsToResolve)) {
			resolvedVariables += resultFnIn.variables.findAll{ node.variables.contains(it)}			
		}
	}
	
	public boolean shouldRetain() {
		return true
	}
}
