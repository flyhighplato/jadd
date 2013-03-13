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
	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFnsMe = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> >();
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> > nextBeliefFnsOther = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD> >();

	
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsMe = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> > obsProbsOther = new HashMap< HashMap<DDVariable,Integer>, HashMap<HashMap<DDVariable,Integer>, Double> >();
	
	Random random = new Random();
	
	public JointBelief(AbstractPOMDP pMe,AbstractPOMDP pOther, FactoredCondProbDD beliefFnMe, FactoredCondProbDD beliefFnOther) {
		this.pMe = pMe;
		this.pOther = pOther;
		this.beliefFnMe = beliefFnMe;
		this.beliefFnOther = beliefFnOther;
		
		for(HashMap<DDVariable,Integer> actMe:pMe.getActionSpace()) {
			for(HashMap<DDVariable,Integer> actOther:pOther.getActionSpace()) {
				
				HashMap<DDVariable,Integer> jointAction = new HashMap<DDVariable,Integer>(actMe);
				jointAction.putAll(actOther);
						
				FactoredCondProbDD tempRestrTransFn = pMe.getTransitionFunction().restrict(actMe).restrict(actOther).multiply(beliefFnMe);
				tempRestrTransFn = tempRestrTransFn.sumOut(pMe.getStates());
				tempRestrTransFn = tempRestrTransFn.normalize();
				
				FactoredCondProbDD obsProbFn = pMe.getObservationFunction().restrict(actMe).restrict(actOther).multiply(tempRestrTransFn);
				obsProbFn = obsProbFn.sumOut(pMe.getStatesPrime());
				obsProbFn = obsProbFn.normalize();
				
				nextBeliefFnsMe.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>());
				obsProbsMe.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, Double>());
				
				for(HashMap<DDVariable,Integer> obsMe:pMe.getObservationSpace()) {
					double obsProb = obsProbFn.getValue(obsMe);
					
					obsProbsMe.get(jointAction).put(obsMe, obsProb);
					
					if(obsProb>0.0f) {
						FactoredCondProbDD tempRestrObsFn =  pMe.getObservationFunction().restrict(jointAction);
						tempRestrObsFn = tempRestrObsFn.restrict(obsMe);
						
						FactoredCondProbDD nextBelief =  pMe.getTransitionFunction().restrict(jointAction);
						nextBelief = nextBelief.multiply(beliefFnMe);
						nextBelief = nextBelief.multiply(tempRestrObsFn);
						
						nextBelief = nextBelief.normalize();
						nextBelief = nextBelief.unprime();
						
						nextBeliefFnsMe.get(jointAction).put(obsMe, nextBelief);
					}
				}
				
				
				tempRestrTransFn = pOther.getTransitionFunction();
				tempRestrTransFn = tempRestrTransFn.restrict(actMe);
				tempRestrTransFn = tempRestrTransFn.restrict(actOther);
				tempRestrTransFn = tempRestrTransFn.multiply(beliefFnOther);
				tempRestrTransFn = tempRestrTransFn.sumOut(pOther.getStates());
				tempRestrTransFn = tempRestrTransFn.normalize();
				
				obsProbFn = pOther.getObservationFunction().restrict(actMe).restrict(actOther).multiply(tempRestrTransFn);
				obsProbFn = obsProbFn.sumOut(pOther.getStatesPrime());
				obsProbFn = obsProbFn.normalize();
				
				nextBeliefFnsOther.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, FactoredCondProbDD>());
				obsProbsOther.put(jointAction, new HashMap<HashMap<DDVariable,Integer>, Double>());
				for(HashMap<DDVariable,Integer> obsOther:pOther.getObservationSpace()) {
					double obsProb = obsProbFn.getValue(obsOther);
					
					obsProbsOther.get(jointAction).put(obsOther, obsProb);
					
					if(obsProb>0.0f) {
						FactoredCondProbDD tempRestrObsFn =  pMe.getObservationFunction().restrict(actMe).restrict(actOther);
						tempRestrObsFn = tempRestrObsFn.restrict(obsOther);
						
						FactoredCondProbDD nextBelief =  pMe.getTransitionFunction().restrict(actMe).restrict(actOther);
						nextBelief = nextBelief.multiply(beliefFnMe);
						nextBelief = nextBelief.multiply(tempRestrObsFn);
						
						nextBelief = nextBelief.normalize();
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

	public JointBelief reverse() {
		return new JointBelief(pOther,pMe,beliefFnOther,beliefFnMe);
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
		
		return sampleSpacePoint(pMe.getObservations(),pMe.getObservationFunction().restrict(jointAction));
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
