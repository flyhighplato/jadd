package masg.dd.dbn

import masg.dd.AlgebraicDD

class BayesianNetwork {
	def nodes = []
	
	def getRootNodes() {
		nodes.findAll{ BayesianNetworkNode node ->
			node instanceof DecisionNode || (node instanceof UncertaintyNode && !node.parents)
		}
	}
	
	def getLeafNodes() {
		nodes.findAll{ BayesianNetworkNode node ->
			node instanceof ValueNode || (node instanceof UncertaintyNode && !node.children)
		}
	}
	
	def findUncertaintyNodesWithCondition(AlgebraicDD fn) {
		return findUncertaintyNodesWithCondition(fn.variables) 
	}
	
	def findUncertaintyNodesWithCondition(variables) {
		return nodes.findAll { BayesianNetworkNode node ->
			if(node instanceof UncertaintyNode) {
				return node.conditionalVariables.intersect(variables)
			}
		}
	}
	
	def findUncertaintyNodesWithPosterior(AlgebraicDD fn) {
		return findUncertaintyNodesWithPosterior(fn.variables)
	}
	
	def findUncertaintyNodesWithPosterior(variables) {
		return nodes.findAll { BayesianNetworkNode node ->
			if(node instanceof UncertaintyNode) {
				return node.posteriorVariables.intersect(variables)
			}
		}
	}
	
	def findResolvedUncertainityNodes(variables) {
		def nodes = findUncertaintyNodesWithPosterior(variables).findAll { UncertaintyNode un ->
			!un.conditionalVariables
		}
		
		def resolvedVariables = nodes.collect { UncertaintyNode un ->
			un.fn.variables
		}.flatten()
		
		return nodes
	}
	
	def getJointProbability(variables) {
		def nodes = findResolvedUncertainityNodes(variables) 
		nodes += findDecisionNodes(variables)
		
		nodes.collect { BayesianNetworkNode n ->
			n.fn
		}
	}
	
	def findDecisionNodes(AlgebraicDD fn) {
		return findDecisionNodes(fn.variables)
	}
	
	def findDecisionNodes(variables) {
		return nodes.findAll { BayesianNetworkNode node ->
			
			if(node instanceof DecisionNode) {
				return node.variables.intersect(variables)
			}
		}
	}
	
	def findValueNodes(AlgebraicDD fn) {
		return findValueNodes(fn.variables)
	}
	
	def findValueNodes(variables) {
		return nodes.findAll { BayesianNetworkNode node ->
			
			if(node instanceof ValueNode) {
				return node.variables.intersect(variables)
			}
		}
	}
	
	def addNode(UncertaintyNode node) {
		if(findUncertaintyNodesWithPosterior(node.posteriorVariables)) {
			println "test"
		}
		assert !findUncertaintyNodesWithPosterior(node.posteriorVariables)
		
		node.parents = findUncertaintyNodesWithPosterior(node.conditionalVariables)
		
		node.parents.each{ UncertaintyNode un ->
			un.children << node
		}
		
		def decisionParents = findDecisionNodes(node.conditionalVariables)
		
		node.parents += decisionParents
		
		decisionParents.each { DecisionNode dn ->
			dn.children << node
		}
		
		node.children = findUncertaintyNodesWithCondition(node.posteriorVariables)
		
		node.children.each { UncertaintyNode un ->
			un.parents << node	
		}
		
		def valueChildren = findValueNodes(node.posteriorVariables)
		
		valueChildren.each { ValueNode vn ->
			vn.parents << node
		}
		
		node.children += valueChildren
		
		nodes << node
	}
	
	def addNode(DecisionNode node) {
		assert !findDecisionNodes(node.variables)
		
		node.children = findUncertaintyNodesWithCondition(node.variables)
		
		node.children.each { UncertaintyNode un ->
			un.parents << node
		}
		
		nodes << node
	}
	
	def addNode(ValueNode node) {
		assert !findValueNodes(node.variables)
		
		node.parents = findUncertaintyNodesWithPosterior(node.variables)
		
		node.parents.each { UncertaintyNode un ->
			un.children << node
		}
		
		def decisionParents = findDecisionNodes(node.variables)
		
		decisionParents.each { DecisionNode dn ->
			dn.children << node
		}
		
		node.parents += decisionParents
		
		nodes << node
	}

	public String getLabel(BayesianNetworkNode node) {
		if(node instanceof UncertaintyNode) {
			UncertaintyNode un = node
			
			String varStringCond = un.conditionalVariables.collect{it.name}.join(", ")
			String varStringPost = un.posteriorVariables.collect{it.name}.join(", ")
			
			if(!varStringCond.isEmpty() && !varStringPost.isEmpty()) {
				return "$varStringCond | $varStringPost"
			}
			else if(!varStringCond.isEmpty()) {
				return "$varStringCond"
			}
			else {
				return "$varStringPost"
			}
		}
		else if(node instanceof DecisionNode) {
			DecisionNode dn = node
			
			String varString = dn.variables.collect{it.name}.join(", ")
			return "Decision $varString"
		}
		else if(node instanceof ValueNode) {
			ValueNode vn = node
			
			String varString = vn.variables.collect{it.name}.join(", ")
			
			if(varString) {
				return "Value of $varString"
			}
			else {
				return "Value = ~${Math.round(vn.fn.getTotalWeight())}"
			}
			
		}
	}
	
	public String toString() {
		String str = "";
		str += "digraph add {\n";
		str += "	rankdir=LR;\n";
		str += "	node [shape = circle];\n";

		nodes.each { BayesianNetworkNode n ->
			
			switch(n) {
				case UncertaintyNode:
					if(n.children) {
						n.children.each{ child ->
							str += "\"${getLabel(n)}\" -> \"${getLabel(child)}\";"
							str += "\n"
						}
					}
					else if(!n.parents) {
						str += "\"${getLabel(n)}\";"
						str += "\n"
					}
					break
				case DecisionNode:
					if(n.children) {
						n.children.each{ child ->
							str += "\"${getLabel(n)}\" -> \"${getLabel(child)}\";"
							str += "\n"
						}
					}
					else {
						str += "\"${getLabel(n)}\";"
						str += "\n"
					}
					break
				case ValueNode:
					if(!n.parents) {
						str += "\"${getLabel(n)}\";"
						str += "\n"
					}
					break
			}
		}
		
		nodes.each { BayesianNetworkNode n ->
			str += "\"${getLabel(n)}\""
			switch(n) {
				case UncertaintyNode:
					str+=" [shape = circle]"
					break
				case DecisionNode:
					str+=" [shape = square]"
					break
				case ValueNode:
					str+=" [shape = diamond]"
					break
			}
			str +=";\n"
		}
		
		str += "}\n";
		
		return str;
	}
}
