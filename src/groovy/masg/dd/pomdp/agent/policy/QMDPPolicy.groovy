package masg.dd.pomdp.agent.policy;

import java.util.HashMap;
import java.util.Map.Entry

import masg.dd.AlgebraicDD;
import masg.dd.pomdp.agent.belief.Belief
import masg.dd.pomdp.agent.belief.POMDPBelief;
import masg.dd.variables.DDVariable;

class QMDPPolicy implements Policy {

	HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> qFn;
	public QMDPPolicy(HashMap<HashMap<DDVariable,Integer>, AlgebraicDD> qFn) {
		this.qFn = qFn;
	}
	
	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		
		HashMap<DDVariable,Integer> bestAct = null;
		double bestActVal = -Double.MAX_VALUE;
		
		for(Entry<HashMap<DDVariable,Integer>, AlgebraicDD> e:qFn.entrySet()) {
			double actVal = belief.beliefFn.multiply(e.getValue()).getTotalWeight();
			
			if(actVal>bestActVal) {
				bestAct = e.getKey();
				bestActVal = actVal;
			}
		}
		
		return bestAct;
	}
	
}