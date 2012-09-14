package masg.agent.pomdp.belief

import masg.agent.pomdp.policy.RandomPolicy
import masg.dd.function.DDTransitionFunction
import masg.dd.pomdp.POMDP
import masg.dd.vars.DDVariable

class BeliefRegion {
	POMDP p
	List beliefSamples = []
	
	
	public BeliefRegion(int numSamples, POMDP p, RandomPolicy policy) {
		this.p = p
		
		
		def belief = p.getInitialtBelief()
		
		Random random = new Random()
		
		numSamples.times{
			
			beliefSamples << belief
			
			HashMap<DDVariable,Integer> actInstance = policy.getAction(belief)
			
			
			//println "action: $actInstance"
			
			DDTransitionFunction restrictedTransFn = p.getTransFns().restrict(actInstance);
			DDTransitionFunction belTransFn = restrictedTransFn.multiply(belief)
			DDTransitionFunction summedTransFn = belTransFn.sumOut(p.states,false)
			summedTransFn.normalize()
			
			DDTransitionFunction restrictedObsFn = p.getObsFns().restrict(actInstance);
			DDTransitionFunction summedObsFn = restrictedObsFn.multiply(summedTransFn)
			summedObsFn = summedObsFn.sumOutAllExcept(p.obs, false);
			
			summedObsFn.normalize();
			
			
			HashMap<DDVariable,Integer> obsPt = new HashMap<DDVariable,Integer>()
			p.obs.each{DDVariable o ->
				DDTransitionFunction temp = summedObsFn.sumOutAllExcept([o],false)
				temp.normalize();

				double thresh = random.nextDouble();
				double weight = 0.0f;
				for(int i=0;i<o.getValueCount();i++){
					HashMap<DDVariable,Integer> tempPt = new HashMap<DDVariable,Integer>()
					tempPt[o] = i
					weight += temp.getValue(tempPt)
					if(weight>thresh) {
						obsPt[o] = i
						//println "observation: $o = $i"
						break
					}
				}
				
			}
			
			
			
			belief = POMDP.updateBelief(p, belief, actInstance, obsPt)
		}
	}
}
