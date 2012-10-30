package masg.agent.pomdp.policy.refactored

import masg.dd.pomdp.refactored.POMDP
import masg.dd.vars.DDVariable

class RandomPolicy {
	protected POMDP p
	Random random = new Random()
	
	public RandomPolicy(POMDP p) {
		assert p
		this.p = p
	}
	
	public HashMap<DDVariable,Integer> getAction(def belief) {
		HashMap<DDVariable,Integer> rndActs = []
		p.actions.each{DDVariable a ->
			rndActs[a] = random.nextInt(a.getValueCount())
		}
		return rndActs;
	}
}
