package masg.dd.pomdp.agent.belief

import java.util.List;

import masg.dd.CondProbDD;
import masg.dd.ProbDD;
import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.pomdp.agent.policy.RandomPolicy;
import masg.dd.variables.DDVariable;

class BeliefRegion {
	POMDP p
	protected List<Belief> beliefSamples = []
	
	public final List<Belief> getBeliefSamples() {
		return beliefSamples;
	}
	
	public BeliefRegion(int numSamples, POMDP p, RandomPolicy policy) {
		this.p = p
		
		CondProbDD belief = p.getInitialBelief().toConditionalProbabilityFn()
		
		Random random = new Random()
		
		List<CondProbDD> beliefFnSamples = []
		
		numSamples.times{
			beliefFnSamples << belief
			
			HashMap<DDVariable,Integer> actPoint = policy.getAction(belief)
			
			println "Policy gives action $actPoint"
			
			CondProbDD restrTransFn = p.getTransitionFunction().restrict(actPoint)
			CondProbDD restrObsFn = p.getObservationFunction().restrict(actPoint)
			
			CondProbDD tempRestrTransFn = restrTransFn.multiply(belief)
			tempRestrTransFn = tempRestrTransFn.sumOut(p.getStates())
			
			CondProbDD obsProbFn = restrObsFn.multiply(tempRestrTransFn)
			obsProbFn = obsProbFn.sumOut(p.getStatesPrime())
			obsProbFn = obsProbFn.normalize()
			
			HashMap<DDVariable,Integer> obsPt = new HashMap<DDVariable,Integer>()
			p.getObservations().each{ DDVariable o ->
				ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(p.getObservations())
				sumOutVars.remove(o);
				
				ProbDD obsProbTempFn = obsProbFn.sumOut(sumOutVars).toProbabilityFn()
				
				double thresh = random.nextDouble();
				double weight = 0.0f;
				for(int i=0;i<o.getValueCount();i++){
					HashMap<DDVariable,Integer> tempPt = new HashMap<DDVariable,Integer>()
					tempPt[o] = i
					weight += obsProbTempFn.getValue(tempPt)
					if(weight>thresh) {
						obsPt[o] = i
						break
					}
				}
			}
			
			println "Sampled observation $obsPt"
			restrObsFn = restrObsFn.restrict(obsPt);
			
			CondProbDD temp = restrObsFn.multiply(tempRestrTransFn);
			temp = temp.normalize()
			belief = temp.unprime()
			println()
		}
		
		println "Initializing belief region"
		beliefFnSamples.each{ CondProbDD beliefFn ->
			beliefSamples << new Belief(p,beliefFn);
		}
	}
}
