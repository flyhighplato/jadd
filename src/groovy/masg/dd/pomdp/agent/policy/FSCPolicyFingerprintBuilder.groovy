package masg.dd.pomdp.agent.policy

class FSCPolicyFingerprintBuilder {
	
	FSCTransition currTransition = new FSCTransition()
	
	protected class FSCTransition {
		def action = [:]
		def observation = [:]
		def prevNode, nextNode
		
		int hitCount = 0;
		
		public boolean equals(Object o) {
			if(o instanceof FSCTransition) {
				FSCTransition otherTr = o
				
				return otherTr.observation.equals(observation) && otherTr.prevNode == prevNode && otherTr.nextNode == nextNode
			}
			
			return false;
		}
		
		public int hashCode() {
			return prevNode.hashCode() + nextNode.hashCode()
		}
	}
	
	Map transitionHits = [:]
	
	public FSCPolicyFingerprintBuilder( FSCPolicyNode startNode ) {
		restartAt(startNode)
	}
	
	public void selectAction(action) {
		currTransition.action = action
	}
	
	public void selectObservation(observation) {
		currTransition.observation = observation
	}
	
	public void transition(nextNode) {
		currTransition.nextNode = nextNode
		
		if(!transitionHits.containsKey(currTransition))
			transitionHits[currTransition] = currTransition
			
		transitionHits[currTransition].hitCount+=1;
		
		currTransition = new FSCTransition()
		currTransition.prevNode = nextNode
	}
	
	public void restartAt(node) {
		currTransition = new FSCTransition()
		currTransition.prevNode = node
	}
	
	public String toString() {
		def maxEdges = 50
		def l = transitionHits.values().asList()
		def edgeCount = l.size()
		def effectiveMaxEdges = l.size()<=maxEdges?l.size():maxEdges
	
		
		
		String retStr = "";
		
		retStr += "digraph add {\n";
		retStr += "	rankdir=LR;\n";
		retStr += "	node [shape = circle];\n";
		
		l = l.sort { a, b -> b.hitCount <=> a.hitCount}.subList(0,effectiveMaxEdges)
		
		l.eachWithIndex { trans, ix ->
			
			def penwidth = Math.round( (trans.hitCount*300)/edgeCount + 1 )
			
			def color = trans.hitCount
			if(trans.hitCount>9)
				color = 9
				
			retStr += "${trans.prevNode.index} -> ${trans.nextNode.index} [label = \"${trans.action}:${trans.observation}\", colorscheme = ylorrd9, color=${color}, penwidth=${penwidth}]\n"
		}
		
		retStr += "}\n"
		
		println "Showing ${effectiveMaxEdges} edges out of ${edgeCount} edges"
		return retStr
	}
	
}
