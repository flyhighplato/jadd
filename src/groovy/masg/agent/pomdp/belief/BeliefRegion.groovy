package masg.agent.pomdp.belief

import masg.agent.pomdp.policy.RandomPolicy
import masg.dd.function.CondProbFunction
import masg.dd.pomdp.POMDP
import masg.agent.pomdp.POMDPUtils

import masg.dd.vars.DDVariable

class BeliefRegion {
	POMDP p
	protected List<CondProbFunction> beliefSamples = []
	
	public final List<CondProbFunction> getBeliefSamples() {
		return beliefSamples;
	}
	
	public BeliefRegion(int numSamples, POMDP p, RandomPolicy policy) {
		this.p = p
		
		
		def belief = p.getInitialtBelief()
		
		Random random = new Random()
		
		numSamples.times{
			
			beliefSamples << belief
			
			HashMap<DDVariable,Integer> actInstance = policy.getAction(belief)
			
			CondProbFunction restrictedTransFn = p.getTransFn().restrict(actInstance);
			CondProbFunction belTransFn = restrictedTransFn.times(belief)
			CondProbFunction summedTransFn = belTransFn.sumOut(p.states,false)
			summedTransFn.normalize()
			
			CondProbFunction restrictedObsFn = p.getObsFns().restrict(actInstance);
			CondProbFunction summedObsFn = restrictedObsFn.times(summedTransFn)
			summedObsFn = summedObsFn.sumOutAllExcept(p.obs, false);
			
			summedObsFn.normalize();
			
			
			HashMap<DDVariable,Integer> obsPt = new HashMap<DDVariable,Integer>()
			p.obs.each{DDVariable o ->
				CondProbFunction temp = summedObsFn.sumOutAllExcept([o],false)
				temp.normalize();

				double thresh = random.nextDouble();
				double weight = 0.0f;
				for(int i=0;i<o.getValueCount();i++){
					HashMap<DDVariable,Integer> tempPt = new HashMap<DDVariable,Integer>()
					tempPt[o] = i
					weight += temp.getValue(tempPt)
					if(weight>thresh) {
						obsPt[o] = i
						break
					}
				}
				
			}
			
			belief = POMDPUtils.updateBelief(p, belief, actInstance, obsPt)
		}
	}
}
