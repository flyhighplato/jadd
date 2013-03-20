package masg.dd.pomdp.agent.belief;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
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

	HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> obsProbFnsMe = new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>();
	HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> obsProbFnsOther = new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>();
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsMe = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsOther = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	
	Random random = new Random();
	
	private JointBelief(
			AbstractPOMDP pMe,
			AbstractPOMDP pOther, 
			FactoredCondProbDD beliefFnMe, 
			FactoredCondProbDD beliefFnOther, 
			HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFnsMe,
			HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFnsOther,
			HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> obsProbFnsMe,
			HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> obsProbFnsOther,
			HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsMe,
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
		
		this.obsProbFnsMe = obsProbFnsMe;
		this.obsProbFnsOther = obsProbFnsOther;
		
		this.obsProbsMe = obsProbsMe;
		this.obsProbsOther = obsProbsOther;
		
		this.reverseBelief = reverseBelief;
		
	}
	
	public JointBelief(JointBelief bMe, JointBelief bOther) {
		this.pMe = bMe.pMe;
		this.pOther = bOther.pMe;
		
		this.beliefFnMe = bMe.beliefFnMe;
		this.beliefFnOther = bOther.beliefFnMe;
		
		this.nextBeliefFnsMe = bMe.nextBeliefFnsMe;
		this.nextBeliefFnsOther = bOther.nextBeliefFnsMe;
		
		this.obsProbFnsMe = bMe.obsProbFnsMe;
		this.obsProbFnsOther = bOther.obsProbFnsMe;
		
		this.obsProbsMe = bMe.obsProbsMe;
		this.obsProbsOther = bOther.obsProbsMe;
		
		this.reverseBelief = null;
	}
	
	public JointBelief(AbstractPOMDP pMe,AbstractPOMDP pOther, FactoredCondProbDD beliefFnMe, FactoredCondProbDD beliefFnOther) {
		this.pMe = pMe;
		this.pOther = pOther;
		this.beliefFnMe = beliefFnMe;
		this.beliefFnOther = beliefFnOther;
		
		//P(s'|a,s)*P(s) = P(s'|a)
		FactoredCondProbDD restrTransBeliefFnMe = pMe.getTransitionFunction().multiply(beliefFnMe);
		restrTransBeliefFnMe = restrTransBeliefFnMe.sumOut(pMe.getStates());
		restrTransBeliefFnMe = restrTransBeliefFnMe.normalize();
		restrTransBeliefFnMe = restrTransBeliefFnMe.approximate(pMe.getTolerance());
		
		//P(o|a,s')*P(s'|a) = P(o|a)
		FactoredCondProbDD restrObsFnMe = pMe.getObservationFunction().multiply(restrTransBeliefFnMe);
		restrObsFnMe = restrObsFnMe.approximate(pMe.getTolerance());
		
		FactoredCondProbDD restrTransBeliefFnOther = pOther.getTransitionFunction().multiply(beliefFnOther);
		restrTransBeliefFnOther = restrTransBeliefFnOther.sumOut(pOther.getStates());
		restrTransBeliefFnOther = restrTransBeliefFnOther.normalize();
		restrTransBeliefFnOther = restrTransBeliefFnOther.approximate(pOther.getTolerance());
		
		FactoredCondProbDD restrObsFnOther = pOther.getObservationFunction().multiply(restrTransBeliefFnOther);
		restrObsFnOther = restrObsFnOther.approximate(pOther.getTolerance());
		
		for(HashMap<DDVariable,Integer> actMe:pMe.getActionSpace()) {
			FactoredCondProbDD tempRestrTransBeliefFnMe = restrTransBeliefFnMe.restrict(actMe);
			FactoredCondProbDD tempRestrTransBeliefFnOther = restrTransBeliefFnOther.restrict(actMe);
			
			FactoredCondProbDD restrObsProbFnMe = pMe.getObservationFunction().restrict(actMe);
			FactoredCondProbDD restrObsProbFnOther = pOther.getObservationFunction().restrict(actMe);
			
			for(HashMap<DDVariable,Integer> actOther:pOther.getActionSpace()) {
				
				HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
				jointAction.putAll(actOther);
				
				FactoredCondProbDD obsProbFn = restrObsFnMe.restrict(jointAction);
				obsProbFn = obsProbFn.sumOut(pMe.getStatesPrime());
				obsProbFn = obsProbFn.normalize();
				obsProbFn = obsProbFn.approximate(pOther.getTolerance());
				
				obsProbFnsMe.put(jointAction, obsProbFn);
				
				nextBeliefFnsMe.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>());
				obsProbsMe.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, Double>());
				
				FactoredCondProbDD tempRestrTransFn = tempRestrTransBeliefFnMe.restrict(actOther);
				FactoredCondProbDD tempRestrObsProbFnMe = restrObsProbFnMe.restrict(actOther);
				
				for(HashMap<DDVariable,Integer> obsMe:pMe.getObservationSpace()) {
					double obsProb = obsProbFn.getValue(obsMe);
					
					obsProbsMe.get(jointAction).put(obsMe, obsProb);
					
					if(obsProb>0.0d) {
						
						FactoredCondProbDD tempRestrObsFn =  tempRestrObsProbFnMe.restrict(obsMe);

						FactoredCondProbDD nextBelief =  tempRestrTransFn.multiply(tempRestrObsFn);
						nextBelief = nextBelief.normalize();
						nextBelief = nextBelief.approximate(pMe.getTolerance());
						nextBelief = nextBelief.unprime();
						
						
						nextBeliefFnsMe.get(jointAction).put(obsMe, nextBelief);
						
					}
				}

				obsProbFn = restrObsFnOther.restrict(jointAction);
				obsProbFn = obsProbFn.sumOut(pOther.getStatesPrime());
				obsProbFn = obsProbFn.normalize();
				obsProbFn = obsProbFn.approximate(pOther.getTolerance());
				
				obsProbFnsOther.put(jointAction, obsProbFn);
				
				nextBeliefFnsOther.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>());
				obsProbsOther.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, Double>());
				
				tempRestrTransFn = tempRestrTransBeliefFnOther.restrict(actOther);
				FactoredCondProbDD tempObsProbFnOther = restrObsProbFnOther.restrict(actOther);
					
				for(HashMap<DDVariable,Integer> obsOther:pOther.getObservationSpace()) {
					double obsProb = obsProbFn.getValue(obsOther);
					
					obsProbsOther.get(jointAction).put(obsOther, obsProb);
					
					if(obsProb>0.0d) {
						FactoredCondProbDD tempRestrObsFn =  tempObsProbFnOther.restrict(obsOther);
						
						FactoredCondProbDD nextBelief = tempRestrTransFn.multiply(tempRestrObsFn);
						nextBelief = nextBelief.normalize();
						nextBelief = nextBelief.approximate(pOther.getTolerance());
						nextBelief = nextBelief.unprime();
						
						nextBeliefFnsOther.get(jointAction).put(obsOther, nextBelief);
					}
				}
				
			}
			
		}
	}
	
	@Override
	public FactoredCondProbDD getBeliefFunction() {
		return beliefFnMe;
	}

	public FactoredCondProbDD getObservationProbabilityFunction(HashMap<DDVariable,Integer> jointAction) {
		return obsProbFnsMe.get(jointAction);
	}
	
	public JointBelief reverse() {
		if(reverseBelief==null)
			reverseBelief = new JointBelief(
					pOther,
					pMe,
					beliefFnOther,
					beliefFnMe, 
					nextBeliefFnsOther, 
					nextBeliefFnsMe, 
					obsProbFnsOther, 
					obsProbFnsMe,
					obsProbsOther, 
					obsProbsMe, 
					this);
		
		return reverseBelief;
	}
	
	public JointBelief getNextBelief(HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther,
			HashMap<DDVariable, Integer> obsMe, 
			HashMap<DDVariable, Integer> obsOther) {
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		
		return new JointBelief(pMe, pOther,  nextBeliefFnsMe.get(jointAction).get(obsMe), nextBeliefFnsOther.get(jointAction).get(obsOther));
	}

	public FactoredCondProbDD getNextBeliefFunction(
			HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther,
			HashMap<DDVariable, Integer> obsMe) {
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		return  nextBeliefFnsMe.get(jointAction).get(obsMe);
	}

	public HashMap<HashMap<DDVariable, Integer>, Double> getObservationOtherProbabilities(
			HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther) {
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		
		return obsProbsOther.get(jointAction);
	}

	public HashMap<HashMap<DDVariable, Integer>, Double> getObservationProbabilities(
			HashMap<DDVariable, Integer> actMe,
			HashMap<DDVariable, Integer> actOther) {
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		return obsProbsMe.get(jointAction);
	}
	
	public HashMap<DDVariable,Integer> sampleNextObservation(HashMap<DDVariable,Integer> actMe, HashMap<DDVariable,Integer> actOther) {
		
		HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
		jointAction.putAll(actOther);
		
		return sampleSpacePoint(pMe.getObservations(),getObservationProbabilityFunction(jointAction));
	}
	
	public HashMap<DDVariable,Integer> sampleSpacePoint(ArrayList<DDVariable> variables, FactoredCondProbDD probFn) {
		HashMap<DDVariable,Integer> point = new HashMap<DDVariable,Integer>();
		
		for(DDVariable variable:variables) {
			ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(variables);
			sumOutVars.remove(variable);
			
			ProbDD probTempFn = probFn.sumOut(sumOutVars).toProbabilityDD();
			
			double thresh = random.nextDouble();
			double weight = 0.0f;
			
			for(int i=0;i<variable.getValueCount();i++){
				HashMap<DDVariable,Integer> tempPt = new HashMap<DDVariable,Integer>();
				tempPt.put(variable,i);
				weight += probTempFn.getValue(tempPt);
				if(weight>thresh) {
					point.put(variable,i);
					break;
				}
			}
		}
		
		return point;
	}

}
