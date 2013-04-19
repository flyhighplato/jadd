package masg.dd.pomdp.agent.belief;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import masg.dd.FactoredCondProbDD;
import masg.dd.pomdp.AbstractPOMDP;
import masg.dd.variables.DDVariable;

public class JointBelief implements Belief {

	final AbstractPOMDP pMe;
	final AbstractPOMDP pOther;
	final FactoredCondProbDD beliefFnMe;
	final FactoredCondProbDD beliefFnOther;
	
	private JointBelief reverseBelief = null;
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFnsMe = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> >();
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFnsOther = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> >();

	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsMe = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsOther = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	
	Random random = new Random();
	
	boolean cachesInit = false;
	
	private JointBelief(
			AbstractPOMDP pMe,
			AbstractPOMDP pOther, 
			FactoredCondProbDD beliefFnMe, 
			FactoredCondProbDD beliefFnOther, 
			HashMap<HashMap<DDVariable, Integer>, HashMap<HashMap<DDVariable, Integer>, FactoredCondProbDD>> nextBeliefFnsMe,
			HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFnsOther,
			HashMap<HashMap<DDVariable, Integer>, HashMap<HashMap<DDVariable, Integer>, Double>> obsProbsMe,
			HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsOther,
			JointBelief reverseBelief
			) 
	{
		this.pMe = pMe;
		this.pOther = pOther;
		
		this.beliefFnMe = beliefFnMe;
		this.beliefFnOther = beliefFnOther;
		
		this.nextBeliefFnsMe = nextBeliefFnsMe;
		this.nextBeliefFnsOther = nextBeliefFnsOther;
		
		this.obsProbsMe = obsProbsMe;
		this.obsProbsOther = obsProbsOther;
		
		this.reverseBelief = reverseBelief;
		
		cachesInit = true;
	}
	
	public JointBelief(JointBelief bMe, JointBelief bOther) {
		this.pMe = bMe.pMe;
		this.pOther = bOther.pMe;
		
		this.beliefFnMe = bMe.beliefFnMe;
		this.beliefFnOther = bOther.beliefFnMe;
		
		this.nextBeliefFnsMe = bMe.nextBeliefFnsMe;
		this.nextBeliefFnsOther = bOther.nextBeliefFnsMe;
		
		this.obsProbsMe = bMe.obsProbsMe;
		this.obsProbsOther = bOther.obsProbsMe;
		
		this.reverseBelief = null;
	}
	
	public JointBelief(AbstractPOMDP pMe,AbstractPOMDP pOther, FactoredCondProbDD beliefFnMe, FactoredCondProbDD beliefFnOther) {
		this.pMe = pMe;
		this.pOther = pOther;
		this.beliefFnMe = beliefFnMe;
		this.beliefFnOther = beliefFnOther;
	}
	
	
	private void initCaches() {
		if(cachesInit)
			return;
		
		FactoredCondProbDD transitionProbMe;
		FactoredCondProbDD transitionProbOther;
		
		HashSet<DDVariable> retainVarsMe = new HashSet<DDVariable>();
		retainVarsMe.addAll(pMe.getObservations());
		retainVarsMe.addAll(pMe.getActions());
		retainVarsMe.addAll(pOther.getActions());
		retainVarsMe.addAll(pMe.getStatesPrime());
		
		HashSet<DDVariable> retainVarsOther = new HashSet<DDVariable>();
		retainVarsOther.addAll(pOther.getObservations());
		retainVarsOther.addAll(pOther.getActions());
		retainVarsOther.addAll(pMe.getActions());
		retainVarsOther.addAll(pOther.getStatesPrime());
		
		transitionProbMe =  pMe.getTransitionFunction().multiply(beliefFnMe,retainVarsMe);
		transitionProbMe =  pMe.getObservationFunction().multiply(transitionProbMe,retainVarsMe);
		
		transitionProbOther =  pOther.getTransitionFunction().multiply(beliefFnOther,retainVarsOther);
		transitionProbOther =  pOther.getObservationFunction().multiply(transitionProbOther,retainVarsOther);
		
		for(HashMap<DDVariable,Integer> actMe:pMe.getActionSpace()) {
			FactoredCondProbDD transitionProbMeActMeRestr = transitionProbMe.restrict(actMe);
			FactoredCondProbDD transitionProbOtherActMeRestr = transitionProbOther.restrict(actMe);
			
			for(HashMap<DDVariable,Integer> actOther:pOther.getActionSpace()) {

				HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
				jointAction.putAll(actOther);

				//System.out.println("Action:" + jointAction);
				
				FactoredCondProbDD transitionProbMeActRestr = transitionProbMeActMeRestr.restrict(actOther);
				HashSet<HashMap<DDVariable,Integer>> uniqueObservationsMe = transitionProbMeActRestr.getAllUniqueNonZeroPaths(pMe.getObservations());
				
				
				HashMap<HashMap<DDVariable,Integer>,Double> obsProbs = new HashMap<HashMap<DDVariable,Integer>,Double>();
				HashMap<HashMap<DDVariable,Integer>,FactoredCondProbDD> obsBeliefs = new HashMap<HashMap<DDVariable,Integer>,FactoredCondProbDD>();
				
				double totalObsWeight = 0.0d;
				for(HashMap<DDVariable,Integer> obs:uniqueObservationsMe) {
					
					FactoredCondProbDD condBelief = transitionProbMeActRestr.restrict(obs, false);
					
					double obsWeight = condBelief.sumOut(pMe.getStatesPrime(), false).toProbabilityDD().getFunction().getTotalWeight();
					totalObsWeight += obsWeight;	
					obsProbs.put(obs, obsWeight);
					
					FactoredCondProbDD newBelief = transitionProbMeActRestr.restrict(obs).normalize().unprime();
					
					obsBeliefs.put(obs, newBelief);
					
				}
				
				for(HashMap<DDVariable,Integer> obs:uniqueObservationsMe) {
					obsProbs.put(obs, obsProbs.get(obs)/totalObsWeight);
				}
				
				
				//System.out.println(" 1:Obs:" + uniqueObservationsMe.size());
				obsProbsMe.put(jointAction, obsProbs);
				nextBeliefFnsMe.put(jointAction, obsBeliefs);
				
				FactoredCondProbDD transitionProbOtherActRestr = transitionProbOtherActMeRestr.restrict(actOther);
				HashSet<HashMap<DDVariable,Integer>> uniqueObservationsOther = transitionProbOtherActRestr.getAllUniqueNonZeroPaths(pOther.getObservations());
				
				obsProbs = new HashMap<HashMap<DDVariable,Integer>,Double>();
				obsBeliefs = new HashMap<HashMap<DDVariable,Integer>,FactoredCondProbDD>();
				
				
				totalObsWeight = 0.0d;
				for(HashMap<DDVariable,Integer> obs:uniqueObservationsOther) {
					
					FactoredCondProbDD condBelief = transitionProbOtherActRestr.restrict(obs, false);
					
					FactoredCondProbDD obsWeightFn = condBelief.sumOut(pOther.getStatesPrime(), false);
					
					double obsWeight = obsWeightFn.toProbabilityDD().getFunction().getTotalWeight();
					totalObsWeight += obsWeight;	
					obsProbs.put(obs, obsWeight);
					
					FactoredCondProbDD newBelief = transitionProbOtherActRestr.restrict(obs).normalize().unprime();
					
					obsBeliefs.put(obs, newBelief);
					
				}
				
				for(HashMap<DDVariable,Integer> obs:uniqueObservationsOther) {
					obsProbs.put(obs, obsProbs.get(obs)/totalObsWeight);
				}
				
				//System.out.println(" 2:Obs:" + uniqueObservationsOther.size());
				obsProbsOther.put(jointAction, obsProbs);
				nextBeliefFnsOther.put(jointAction, obsBeliefs);
				
			}
		}
		
		
		cachesInit = true;
	}
	
	@Override
	public FactoredCondProbDD getBeliefFunction() {
		return beliefFnMe;
	}

	public JointBelief reverse() {
		initCaches();
		if(reverseBelief==null)
			reverseBelief = new JointBelief(
					pOther,
					pMe,
					beliefFnOther,
					beliefFnMe, 
					nextBeliefFnsOther,
					nextBeliefFnsMe,
					obsProbsOther, 
					obsProbsMe, 
					this);
		
		return reverseBelief;
	}
	
	public JointBelief getNextBelief(HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther,
			HashMap<DDVariable, Integer> obsMe, 
			HashMap<DDVariable, Integer> obsOther) {
		
		initCaches();
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		
		FactoredCondProbDD nextBeliefFnMe = nextBeliefFnsMe.get(jointAction).get(obsMe);
		FactoredCondProbDD nextBeliefFnOther = nextBeliefFnsOther.get(jointAction).get(obsOther);
		
		if(nextBeliefFnMe == null || nextBeliefFnOther == null)
			System.out.println("Impossible observation");
		
		return new JointBelief(pMe, pOther,  nextBeliefFnMe, nextBeliefFnOther);
	}

	public FactoredCondProbDD getNextBeliefFunction(
			HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther,
			HashMap<DDVariable, Integer> obsMe) {
		initCaches();
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		return nextBeliefFnsMe.get(jointAction).get(obsMe);
	}

	public HashMap<HashMap<DDVariable, Integer>, Double> getObservationOtherProbabilities(
			HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther) {
		initCaches();
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		
		return obsProbsOther.get(jointAction);
	}

	public HashMap<HashMap<DDVariable, Integer>, Double> getObservationProbabilities(
			HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther) {
		
		initCaches();
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		return obsProbsMe.get(jointAction);
	}
	
	public HashMap<DDVariable,Integer> sampleNextObservation(HashMap<DDVariable,Integer> actMe, HashMap<DDVariable,Integer> actOther) {
		initCaches();
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		
		double thresh = random.nextDouble();
		double weight = 0.0f;
		
		if(obsProbsMe.containsKey(jointAction)) {
			for(Entry<HashMap<DDVariable, Integer>, Double> e:obsProbsMe.get(jointAction).entrySet()) {
				
				weight += e.getValue();
				
				if(weight>=thresh) {
					return e.getKey();
				}
			}
		}
		
		System.out.println("SAMPLING PROBLEM");
		
		return null;
	}

}
