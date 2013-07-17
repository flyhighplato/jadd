package masg.dd.dbn

import masg.dd.AlgebraicDD

class BayesianNetworkResult {
	
	def resultNodes = [:]
	def resolvedResultNodes = []
	
	BayesianNetwork network
	
	public BayesianNetworkResult(BayesianNetwork network) {
		this.network = network
	}
		
	public buildNetwork() {
		def unnecessaryNodes = []
		/*resultNodes.each{ BayesianNetworkNode node, BayesianNetworkNodeResult resultNode ->
			
			if(resultNode instanceof UncertaintyNodeResult) {
				UncertaintyNodeResult unResultNode = resultNode
				
				if(!unResultNode.isCPT()) {
					def posteriorAlreadyComputedNodes = resultNodes.values().findAll{ BayesianNetworkNodeResult resultNodeTest ->
						
						if(resultNodeTest instanceof UncertaintyNodeResult) {
							UncertaintyNodeResult unResultNodeTest = resultNodeTest
							return unResultNode!=unResultNodeTest && !unnecessaryNodes.contains(unResultNodeTest) && !unResultNodeTest.isCPT() && unResultNode.resultFn.variables.containsAll(unResultNodeTest.resultFn.variables)
						}
						else if(resultNodeTest instanceof DecisionNodeResult) {
							DecisionNodeResult dnResultNodeTest = resultNodeTest
							return unResultNode.resultFn.variables.containsAll(dnResultNodeTest.resultFn.variables)
						}
					}
					
					HashSet posteriorAlreadyComputed = new HashSet()
					posteriorAlreadyComputedNodes.each { BayesianNetworkNodeResult unResultNodeTest ->
						posteriorAlreadyComputed += unResultNodeTest.resultFn.variables
					}
					
					if(posteriorAlreadyComputed.containsAll(resultNode.resultFn.variables)) {
						unnecessaryNodes << resultNode
					}
					
				}
			}
		}*/
		
		BayesianNetwork newNetwork = new BayesianNetwork()
		
		def resultNodesOld = [:]
		resultNodesOld.putAll(resultNodes)
		resultNodesOld.each{ BayesianNetworkNode node, BayesianNetworkNodeResult resultNode ->
			if(resultNode instanceof UncertaintyNodeResult && !unnecessaryNodes.contains(resultNode)) {
				UncertaintyNodeResult unResultNode = resultNode
				UncertaintyNode un = node
				
				if(!unResultNode.isCPT()) {
					HashSet fns = new HashSet()
					fns.add(resultNode.resultFn)
					
					HashSet allVars = new HashSet()
					allVars.addAll(resultNode.resultFn.variables)
					
					int oldSize = 0
					while(fns.size() != oldSize) {
					
						oldSize = fns.size()
						
						resultNodesOld.values().findAll { BayesianNetworkNodeResult otherResultNode ->
							
							if(otherResultNode!=resultNode && otherResultNode.resultFn.variables.intersect(allVars)) {
								allVars.addAll(otherResultNode.resultFn.variables)
								fns.add(otherResultNode.resultFn)
								
								unnecessaryNodes << otherResultNode
							}
							
							
						}
						
					}
					
					if(fns.size()>1) {
						
						unnecessaryNodes << resultNode
						
						AlgebraicDD newFn = new AlgebraicDD(new ArrayList(allVars),1.0d)
						fns.each {
							newFn = newFn.multiply(it)
						}
						newFn = newFn.normalize()
						
						UncertaintyNode unNew = new UncertaintyNode([],
							allVars,
							newFn
							)
						
						resultNodes[unNew] = getResultNode(unNew)
					}
				}
			}
		}
		
		
		resultNodes.each{ BayesianNetworkNode node, BayesianNetworkNodeResult resultNode ->
			if(resultNode.shouldRetain() && !unnecessaryNodes.contains(resultNode)) {
				if(resultNode instanceof UncertaintyNodeResult) {
					UncertaintyNodeResult unResultNode = resultNode
					UncertaintyNode un = node
					
					UncertaintyNode unNew
					if(unResultNode.isCPT()) {
						unNew = new UncertaintyNode(un.conditionalVariables - unResultNode.resolvedConditional, 
							un.posteriorVariables - unResultNode.resolvedPosterior,
							unResultNode.resultFn
							)
					}
					else {
						
						unNew = new UncertaintyNode([],
							unResultNode.resultFn.variables,
							unResultNode.resultFn
							)
					}
					
					newNetwork.addNode(unNew)
				}
				else if(resultNode instanceof DecisionNodeResult) {
					DecisionNodeResult dnResultNode = resultNode
					DecisionNode dn = node
					
					newNetwork.addNode( new DecisionNode(dn.variables, dnResultNode.resultFn) )
				}
				else if(resultNode instanceof ValueNodeResult) {
					ValueNodeResult vnResultNode = resultNode
					ValueNode vn = node
					
					newNetwork.addNode( new ValueNode(vn.variables - vnResultNode.resolvedVariables, vnResultNode.resultFn) )
				}
			}
		}
		
		return newNetwork
	}
	
	public solve() {
		
		network.nodes.each {
			getResultNode(it)
		}
		
		List q = [] + new HashSet(resolvedResultNodes)
		
		while(q) {
			BayesianNetworkNodeResult resultNode = q.pop()
			
			if(!(resultNode instanceof ValueNodeResult)) {
				HashSet h = new HashSet(q + resultNode.node.children.collect{getResultNode(it)})
				q = [] + h
				
				resultNode.node.children.each{ BayesianNetworkNode childNode ->
					BayesianNetworkNodeResult childResult = getResultNode(childNode)
					
					if(childResult instanceof UncertaintyNodeResult) {
						childResult.resolveConditional(resultNode)
					}
					else if(childResult instanceof ValueNodeResult) {
						childResult.resolve(resultNode)
					}
				}
			}
		}
		
		
		q = [] + new HashSet(resolvedResultNodes)
		
		while(q) {
			BayesianNetworkNodeResult resultNode = q.pop()
			
			if(! (resultNode instanceof DecisionNodeResult) ) {
				HashSet h = new HashSet(q + resultNode.node.parents.collect{getResultNode(it)})
				q = [] + h
				
				if(resultNode instanceof UncertaintyNodeResult) {
					resultNode.node.parents.each {BayesianNetworkNode parentNode ->
						BayesianNetworkNodeResult parentResult = getResultNode(parentNode)
						
						if(parentResult instanceof UncertaintyNodeResult) {
							parentResult.resolvePosterior(resultNode)
						}
						else if(parentResult instanceof DecisionNodeResult) {
							parentResult.resolve(resultNode)
						}
					}
				}
			}
		}
		
		
	}
	
	public probabilityIs(AlgebraicDD fn) {
		
		def foundNodes = network.findUncertaintyNodesWithCondition(fn)
		foundNodes.each{ BayesianNetworkNode node ->
			UncertaintyNodeResult resultNode = getResultNode(node)
			resultNode.resolveConditional(fn)
			resolvedResultNodes << resultNode
		}
		
		network.findUncertaintyNodesWithPosterior(fn).each{ BayesianNetworkNode node ->
			UncertaintyNodeResult resultNode = getResultNode(node)
			resultNode.resolvePosterior(fn)
			resolvedResultNodes << resultNode
		}
		
		/*network.findValueNodes(fn).each{ BayesianNetworkNode node ->
			ValueNodeResult resultNode = getResultNode(node)
			resultNode.resolve(fn)
			resolvedResultNodes << resultNode
		}*/
		
		resolvedResultNodes = [] + new HashSet(resolvedResultNodes)
	}
	
	public decisionIs(AlgebraicDD fn) {
		
		network.findDecisionNodes(fn).each { BayesianNetworkNode node ->
			DecisionNodeResult resultNode = getResultNode(node)
			resultNode.resolve(fn)
			resolvedResultNodes << resultNode
		}
		
		resolvedResultNodes = [] + new HashSet(resolvedResultNodes)
	}
	
	public BayesianNetworkNodeResult getResultNode(UncertaintyNode node) {
		if(!resultNodes.containsKey(node)) {
			UncertaintyNodeResult resultNode = new UncertaintyNodeResult(node)
			resultNodes[node] = resultNode
			return resultNode
		}
		else {
			return resultNodes[node]
		}
	}
	
	public BayesianNetworkNodeResult getResultNode(DecisionNode node) {
		if(!resultNodes.containsKey(node)) {
			DecisionNodeResult resultNode = new DecisionNodeResult(node)
			resultNodes[node] = resultNode
			return resultNode
		}
		else {
			return resultNodes[node]
		}
	}
	
	public BayesianNetworkNodeResult getResultNode(ValueNode node) {
		if(!resultNodes.containsKey(node)) {
			ValueNodeResult resultNode = new ValueNodeResult(node)
			resultNodes[node] = resultNode
			return resultNode
		}
		else {
			return resultNodes[node]
		}
	}
}
