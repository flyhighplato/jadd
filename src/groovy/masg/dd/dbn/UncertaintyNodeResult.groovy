package masg.dd.dbn

import masg.dd.AlgebraicDD

class UncertaintyNodeResult extends BayesianNetworkNodeResult {
	def resolvedConditional = []
	def resolvedPosterior = []
	
	public UncertaintyNodeResult(UncertaintyNode node) {
		this.node = node
		resultFn = node.fn
	}
	
	public resolveConditional(UncertaintyNodeResult result) {
		if(!result.isCPT()) {
			resolveConditional(result.resultFn)
		}
	}
	
	public resolveConditional(BayesianNetworkNodeResult result) {
		resolveConditional(result.resultFn)
	}
	
	boolean isCPT() {
		(node.conditionalVariables - resolvedConditional) && (node.posteriorVariables - resolvedPosterior) 
	}
	
	public resolveConditional(AlgebraicDD resultFnIn) {
		def varsToResolve = resultFnIn.variables.findAll{ node.conditionalVariables.contains(it)}
		
		if(!varsToResolve.isEmpty()) {
			def unusedVariables = resultFnIn.variables.findAll { !node.conditionalVariables.contains(it) }
			
			AlgebraicDD resultFnOld = resultFn
			
			AlgebraicDD resultFnInNorm = resultFnIn.sumOut(unusedVariables)
			
			resultFn = resultFn.multiply(resultFnIn).sumOut(unusedVariables)
			
			
			if(isCPT()) {
				
				AlgebraicDD divisor = resultFn.sumOut( node.posteriorVariables ).sumOut(varsToResolve)
				resultFn = resultFn.sumOut(varsToResolve)
				resultFn = resultFn.div(divisor)
			}
			else {
				AlgebraicDD divisor = resultFn.sumOut( node.conditionalVariables ).sumOut(varsToResolve)
				resultFn = resultFn.sumOut(varsToResolve)
				resultFn = resultFn.div(divisor)
			}
			
			if(resultFn.totalWeight<0.0001d) {
				throw new Exception("Impossible probability!")
			}
			
			resolvedConditional += varsToResolve
			
		}
	}
	
	public resolvePosterior(BayesianNetworkNodeResult result) {
		resolvePosterior(result.resultFn)
	}
	
	public resolvePosterior(AlgebraicDD resultFnIn) {
		
		def varsToResolve = resultFnIn.variables.findAll{ node.posteriorVariables.contains(it)}
		
		if(!varsToResolve.isEmpty()) {
			def unusedVariables = resultFnIn.variables.findAll { !node.posteriorVariables.contains(it) }
			AlgebraicDD resultFnInNorm = resultFnIn.sumOut(unusedVariables)
			
			resultFn = resultFn.multiply(resultFnInNorm)
			
			if(isCPT()) {
				
				AlgebraicDD divisor = resultFn.sumOut(node.conditionalVariables).sumOut(varsToResolve)
				resultFn = resultFn.sumOut(varsToResolve)
				resultFn = resultFn.div(divisor)
			}
			else {
				AlgebraicDD divisor = resultFn.sumOut(node.posteriorVariables).sumOut(varsToResolve)
				//resultFn = resultFn.sumOut(varsToResolve)
				resultFn = resultFn.div(divisor)
			}
			
			if(resultFn.totalWeight<0.0001d) {
				throw new Exception("Impossible probability!")
			}
			
			resolvedPosterior += varsToResolve
		}
		
	}
	
	public boolean shouldRetain() {
		return true
	}
	
	public UncertaintyNode getNode() {
		return super.@node
	}
}
