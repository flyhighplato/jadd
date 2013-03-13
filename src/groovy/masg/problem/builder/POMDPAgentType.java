package masg.problem.builder;

import masg.dd.pomdp.agent.policy.Policy;

public class POMDPAgentType {
	final POMDPProblemBuilder builder;
	final Policy pol;
	
	POMDPAgentType(POMDPProblemBuilder builder, Policy pol) {
		this.builder = builder;
		this.pol = pol;
	}
	
}
