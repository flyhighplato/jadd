package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.alphavector.AlphaVector;
import masg.dd.alphavector.BestResponseAlphaVector;
import masg.dd.pomdp.AbstractPOMDP;
import masg.dd.pomdp.agent.belief.JointBelief;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;

public class JointBeliefBackup implements Runnable {

	public BestResponseAlphaVector result;
	
	final private Map<AlphaVector, ArrayList<BestResponseAlphaVector>> alphas;
	final private AbstractPOMDP p_me;
	final private JointBelief b_me;
	final private JointBelief b_other;
	final private AlphaVectorPolicy pol_other;
	
	private HashMap<HashMap<DDVariable, Integer>,BestResponseAlphaVector> conditionalPlans = new HashMap<HashMap<DDVariable, Integer>,BestResponseAlphaVector>();
	
	public JointBeliefBackup(AbstractPOMDP pMe, JointBelief b_me, AlphaVectorPolicy responseToPolicy, HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>> bestResponseAlphaVectors) {
		this.p_me = pMe;
		this.b_me = b_me;
		
		this.b_other = b_me.reverse();
		
		this.pol_other = responseToPolicy;
		this.alphas = Collections.unmodifiableMap(bestResponseAlphaVectors);
	}
	
	private HashMap<HashMap<DDVariable, Integer>, HashMap<AlphaVector, Double>> nextAlphaVectorDist = new HashMap<HashMap<DDVariable, Integer>, HashMap<AlphaVector, Double>>();
	private HashMap<AlphaVector, Double> getNextAlphaVectorDist(
			HashMap<DDVariable, Integer> actionMeNow,
			HashMap<DDVariable, Integer> actionOtherNow) {
		
		HashMap<DDVariable, Integer> jointAction = new HashMap<DDVariable, Integer>(actionMeNow);
		jointAction.putAll(actionOtherNow);
		
		if(nextAlphaVectorDist.containsKey(jointAction)) {
			return nextAlphaVectorDist.get(jointAction);
		}
		
		HashMap<AlphaVector, Double> nextDist = new HashMap<AlphaVector, Double>();
		
		for(Entry<HashMap<DDVariable, Integer>, Double> e:b_me.getObservationOtherProbabilities(actionMeNow, actionOtherNow).entrySet()) {
			
			HashMap<DDVariable,Integer> obsOther = e.getKey();
			double obsOtherProb = e.getValue();
			
			if(obsOtherProb>0.0d) {
				for(Entry<HashMap<DDVariable, Integer>, Double> e2:b_other.getObservationOtherProbabilities(actionOtherNow, actionMeNow).entrySet()) {
				
					HashMap<DDVariable,Integer> obsMeAccordingToOther = e2.getKey();
					double obsMeAccordingToOtherProb = e2.getValue();
					
					if(obsMeAccordingToOtherProb>0.0d) {
						AlphaVector alphaNext = pol_other.getAlphaVector(b_other.getNextBelief(actionOtherNow, actionMeNow, obsOther, obsMeAccordingToOther));
						
						if(!nextDist.containsKey(alphaNext)) {
							nextDist.put(alphaNext, obsOtherProb * obsMeAccordingToOtherProb);
						}
						else {
							nextDist.put(alphaNext, nextDist.get(alphaNext) + obsOtherProb * obsMeAccordingToOtherProb);
						}
					}
				}
			}
		}
		
		nextAlphaVectorDist.put(jointAction, nextDist);
		
		return nextDist;
	}
	
	private List<BestResponseAlphaVector> getBestResponseAlphas(AlphaVector alphaOther) {
		
		ArrayList<BestResponseAlphaVector> newAlphas = new ArrayList<BestResponseAlphaVector>();
		
		newAlphas.addAll(alphas.get(alphaOther));
		
		return newAlphas;
	}
	
	@Override
	public void run() {
		AlphaVector alphaOtherNow = pol_other.getAlphaVector(b_other);
		HashMap<DDVariable,Integer> actionOtherNow = alphaOtherNow.getAction();
		
		HashMap<DDVariable,Integer> bestAct = null;
		double bestActValue = -Double.MAX_VALUE;
		FactoredCondProbDD belFn = b_me.getBeliefFunction();
		
		AlgebraicDD restRewFn = p_me.getRewardFunction().restrict(actionOtherNow);
		for(HashMap<DDVariable,Integer> actionMeNow: p_me.getActionSpace()) {
			
			HashMap<HashMap<DDVariable, Integer>,BestResponseAlphaVector> conditionalPlansTemp = new HashMap<HashMap<DDVariable, Integer>,BestResponseAlphaVector>();
			double actValue = belFn.multiply(restRewFn.restrict(actionMeNow)).getTotalWeight();
			
			for(Entry<HashMap<DDVariable, Integer>, Double> e:b_me.getObservationProbabilities(actionMeNow, actionOtherNow).entrySet()) {
				
				HashMap<DDVariable,Integer> obsMe = e.getKey();
				double obsProb = e.getValue();
				
				
				if(obsProb>0.0f) {
					FactoredCondProbDD nextBelief = b_me.getNextBeliefFunction(actionMeNow, actionOtherNow, obsMe);
					
					HashMap<AlphaVector, Double> nextAlphaVectorOtherDist = getNextAlphaVectorDist(actionMeNow,actionOtherNow);
					
					double bestObsValue = -Double.MAX_VALUE;
					
					for(Entry<AlphaVector, Double> alphaDistEntry:nextAlphaVectorOtherDist.entrySet()) {
						
						AlphaVector alphaOther = alphaDistEntry.getKey();
						double alphaOtherProb = alphaDistEntry.getValue();
						
						for(BestResponseAlphaVector alpha:getBestResponseAlphas(alphaOther)) {
							double expectedValue = nextBelief.dotProduct(alpha.getValueFunction());
							
							if(expectedValue >= bestObsValue) {
								bestObsValue = expectedValue;
								conditionalPlansTemp.put(obsMe, alpha);
							}
							
						}
						
						actValue += p_me.getDiscount()*obsProb*bestObsValue*alphaOtherProb;
					}
				}
				
			}
			
			if(actValue>=bestActValue) {
				bestActValue = actValue;
				bestAct = actionMeNow;
				conditionalPlans = conditionalPlansTemp;
			}
			
		}
		
		
		AlgebraicDD nextValFn = new AlgebraicDD(p_me.getStates(),0.0d);
		for(Entry<HashMap<DDVariable, Integer>, BestResponseAlphaVector> cp: conditionalPlans.entrySet()) {
			HashMap<DDVariable, Integer> obs = cp.getKey();
			BestResponseAlphaVector alpha = cp.getValue();
			
			if(b_me.getObservationProbabilities(bestAct, alphaOtherNow.getAction()) == null || b_me.getObservationProbabilities(bestAct, alphaOtherNow.getAction()).get(obs) == null) {
				HashMap<HashMap<DDVariable, Integer>, Double> temp = b_me.getObservationProbabilities(bestAct, alphaOtherNow.getAction());
				
				System.out.println(temp);
				Double temp2  = b_me.getObservationProbabilities(bestAct, alphaOtherNow.getAction()).get(obs);
				System.out.println(temp2);
			}
			
			double obsProb = b_me.getObservationProbabilities(bestAct, alphaOtherNow.getAction()).get(obs);
			nextValFn = nextValFn.plus(alpha.getValueFunction().multiply(obsProb*p_me.getDiscount()));
		}
		
		nextValFn = nextValFn.prime();
		
		FactoredCondProbDD dd = p_me.getTransitionFunction().restrict(bestAct).restrict(actionOtherNow);
		AlgebraicDD nextAlpha = dd.multiply(nextValFn);
		
		nextAlpha = nextAlpha.sumOut(p_me.getStatesPrime());
		nextAlpha = nextAlpha.plus(p_me.getRewardFunction().restrict(bestAct).restrict(actionOtherNow));
		
		result = new BestResponseAlphaVector(bestAct, nextAlpha, alphaOtherNow, b_me.getBeliefFunction().toProbabilityDD());
		
	}

	

	

}
