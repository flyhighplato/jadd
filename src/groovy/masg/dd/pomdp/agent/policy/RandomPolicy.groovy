package masg.dd.pomdp.agent.policy

import java.util.HashMap;

import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.variables.DDVariable;

class RandomPolicy implements Policy {
	protected POMDP p
	Random random = new Random()
	
	public RandomPolicy(POMDP p) {
		assert p
		this.p = p
	}

	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		HashMap<DDVariable,Integer> rndActs = []
		p.actions.each{DDVariable a ->
			rndActs[a] = random.nextInt(a.getValueCount())
		}
		return rndActs;
	}
}
