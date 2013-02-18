package masg.dd.pomdp.agent.policy;

import java.util.HashMap;
import java.util.HashMap.Entry

import masg.dd.AlgebraicDD;
import masg.dd.pomdp.agent.belief.Belief
import masg.dd.pomdp.agent.belief.POMDPBelief;
import masg.dd.variables.DDVariable;

class IQMDPPolicy implements Policy {

	HashMap<HashMap<DDVariable, Integer>, HashMap<HashMap<DDVariable, Integer>, AlgebraicDD>> qFn;
	
	public IQMDPPolicy(HashMap<HashMap<DDVariable, Integer>, HashMap<HashMap<DDVariable, Integer>, AlgebraicDD>> qFn) {
		this.qFn = qFn;
	}
	
	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		return null;
	}
	
}