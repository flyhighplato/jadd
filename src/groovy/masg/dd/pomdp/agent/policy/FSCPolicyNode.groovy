package masg.dd.pomdp.agent.policy

import masg.dd.variables.DDVariable

class FSCPolicyNode {
	int index = -1;
	
	HashMap<HashMap<DDVariable,Integer>,HashMap<FSCPolicyNode,Double>> transitionFn = new HashMap<HashMap<DDVariable,Integer>,HashMap<FSCPolicyNode,Double>>()
	HashMap< HashMap<DDVariable,Integer>,Double>  actionDistributionFn = new HashMap< HashMap<DDVariable,Integer>,Double>()
	
	String toString() {
		return this.hashCode()
	}
}
