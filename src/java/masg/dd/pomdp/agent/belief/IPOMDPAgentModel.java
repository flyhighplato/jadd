package masg.dd.pomdp.agent.belief;

import masg.dd.ipomdp.IPOMDP;
import masg.dd.pomdp.agent.policy.Policy;

public class IPOMDPAgentModel {
	Belief currentBelief;
	IPOMDP iPOMDP;
	Policy policy;
	
	IPOMDPAgentModel(Belief belief, IPOMDP iPOMDP, Policy policy) {
		this.currentBelief = belief;
		this.iPOMDP = iPOMDP;
		this.policy = policy;
	}
}
