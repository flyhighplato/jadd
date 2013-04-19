package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.alphavector.AlphaVector;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.AbstractPOMDP;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.pomdp.agent.belief.JointBelief;
import masg.dd.pomdp.agent.belief.JointBeliefRegion;
import masg.dd.pomdp.agent.belief.POMDPBelief;
import masg.dd.representation.DDInfo;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;


public class PessimisticAlphaVectorPolicyBuilder {
	double tolerance = 0.00001f;
	
	BeliefAlphaVectorPolicy bestAlphas = new BeliefAlphaVectorPolicy();
	
	private ExecutorService pool = null;
	
	AbstractPOMDP pMe;
	AbstractPOMDP pOther;
	
	public PessimisticAlphaVectorPolicyBuilder(AbstractPOMDP pMe, AbstractPOMDP pOther) {
		this.pMe = pMe;
		this.pOther = pOther;
	}
	
	public BeliefAlphaVectorPolicy buildPureStrategyAlphas() {
		ArrayList<DDVariable> qFnVars = new ArrayList<DDVariable>();
		qFnVars.addAll(pMe.getStates());
		qFnVars.addAll(pMe.getStatesPrime());
		
		for(HashMap<DDVariable,Integer> actSpacePt:pMe.getActionSpace()) {
			FactoredCondProbDD currBel = pMe.getInitialBelief();
			
			System.out.println("Generating pure strategy for action:" + actSpacePt);
			
			AlgebraicDD actionAlpha = new AlgebraicDD(DDBuilder.build(new DDInfo(pMe.getStates(),false),0.0d).getRootNode());
			double bellmanError = 20*tolerance;
			
			for(int i=0;i<50 && bellmanError>tolerance;++i) {
				System.out.println("Iteration #" + i);
				actionAlpha.prime();
				
				AlgebraicDD actionAlphaNew = null;
				
				for(HashMap<DDVariable,Integer> actSpaceOtherPt:pOther.getActionSpace()) {
					AlgebraicDD actionAlphaTemp = pMe.getTransitionFunction().restrict(actSpacePt).restrict(actSpaceOtherPt).multAndSumOut(actionAlpha, pMe.getStatesPrime());
					
					actionAlphaTemp = actionAlphaTemp.multiply(pMe.getDiscount());
					actionAlphaTemp = actionAlphaTemp.plus(pMe.getRewardFunction().restrict(actSpacePt).restrict(actSpaceOtherPt));
					
					
					if(actionAlphaNew == null)
						actionAlphaNew = actionAlphaTemp;
					else
						actionAlphaNew = actionAlphaNew.min(actionAlphaTemp);
				}
				
				bellmanError = actionAlphaNew.maxAbsDiff(actionAlpha); 
				System.out.println("bellmanError: " + bellmanError);
				
				
				actionAlpha = actionAlphaNew;
			}
			
			System.out.println(DDBuilder.findMaxLeaf(actionAlpha.getFunction()));
			System.out.println(DDBuilder.findMinLeaf(actionAlpha.getFunction()));
			
			BeliefAlphaVector alpha = new BeliefAlphaVector(actSpacePt, actionAlpha, currBel.toProbabilityDD());
			
			
			bestAlphas.addAlphaVector(alpha);
		}
		
		return bestAlphas;
	}
	
	private HashMap<Belief,AlphaVector> updateBeliefValues(HashMap<Belief, AlphaVector> beliefAlphas, BeliefAlphaVector newAlpha, List<JointBelief> beliefsTemp) {
		
		HashMap<Belief,AlphaVector> newValues = new HashMap<Belief,AlphaVector>();
		
		for(Entry<Belief, AlphaVector> e:beliefAlphas.entrySet()) {

				AlphaVector oldAlpha = e.getValue();
				Belief b = e.getKey();
				
				double oldValue = b.getBeliefFunction().dotProduct(oldAlpha.getValueFunction());
				double newValue = b.getBeliefFunction().dotProduct(newAlpha.getValueFunction());
				
				if(oldValue<newValue) {
					newValues.put(b, newAlpha);
					beliefsTemp.remove(b);
				}
				else {
					newValues.put(b, oldAlpha);
				}
		}
		
		return newValues;	
	}
	
	private class MaxImprovementResult
	{
		double maxImprovement = -Double.MAX_VALUE;
		List<Belief> improvedBeliefs = new ArrayList<Belief>();
	}
	
	private MaxImprovementResult getMaxImprovement(HashMap<Belief, AlphaVector> beliefAlphas, BeliefAlphaVector newAlpha) {

		MaxImprovementResult result = new MaxImprovementResult();
		
		for(Entry<Belief, AlphaVector> e:beliefAlphas.entrySet()) {

				AlphaVector oldAlpha = e.getValue();
				Belief b = e.getKey();
						
				double oldValue = b.getBeliefFunction().dotProduct(oldAlpha.getValueFunction());
				double newValue = b.getBeliefFunction().dotProduct(newAlpha.getValueFunction());
				
				if(newValue>oldValue) {
					result.improvedBeliefs.add(b);
				}
				
				if(result.maxImprovement < (newValue-oldValue)) {
					result.maxImprovement = (newValue-oldValue);
				}

			
		}

		return result;	
	}
	
	public HashMap<Belief, AlphaVector> pickBestInitialAlphas(List<JointBelief> beliefs,  BeliefAlphaVectorPolicy alphas) {
		
		ArrayList<Future<BeliefGetBestAlpha>> futureBestPicks = new ArrayList<Future<BeliefGetBestAlpha>>();
		for(JointBelief belief:beliefs) {
			BeliefGetBestAlpha getBestAlphaTask = new BeliefGetBestAlpha(belief,alphas);
			futureBestPicks.add( pool.submit(getBestAlphaTask,getBestAlphaTask) );
		}
		
		long startTime = new Date().getTime();
		HashMap<Belief, AlphaVector> beliefAlphas = new HashMap<Belief,AlphaVector>();
		System.out.println("Picking best initial alpha vectors");
		for(Future<BeliefGetBestAlpha> f:futureBestPicks) {
			try {
				BeliefGetBestAlpha task = f.get();
				AlphaVector bestAlpha = task.result;
				beliefAlphas.put(task.b, bestAlpha);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Took " + ( new Date().getTime() - startTime) + " milliseconds");
		
		return beliefAlphas;
	}
	public BeliefAlphaVectorPolicy build(JointBeliefRegion belRegion, int numIterations) {
		pool = Executors.newFixedThreadPool(20);
		
		if(bestAlphas.getAlphaVectors().size()<=0)
			buildPureStrategyAlphas();
		
		List<JointBelief> beliefs = belRegion.getBeliefSamples();

		
		for(HashMap<DDVariable,Integer> statePtMe:pMe.getStateSpace()){
			FactoredCondProbDD beliefFnMe = new FactoredCondProbDD(new CondProbDD(new ArrayList<DDVariable>(),new ArrayList<DDVariable>(pMe.getStates()),0,statePtMe));
			
			HashMap<DDVariable,Integer> statePtOther = new HashMap<DDVariable,Integer>();
			for(Entry<DDVariable,Integer> e: statePtMe.entrySet()) {
				statePtOther.put(new DDVariable(1,e.getKey().getName(),e.getKey().getValueCount()), e.getValue());
			}
			
			FactoredCondProbDD beliefFnOther = new FactoredCondProbDD(new CondProbDD(new ArrayList<DDVariable>(),new ArrayList<DDVariable>(pOther.getStates()),1,statePtOther));
			
			
			JointBelief b = new JointBelief(pMe,pOther,beliefFnMe,beliefFnOther);
			beliefs.add(b);

		}
		
		
		
		double bestImprovement = 1.0d;
		int numImprovements = 1;
		HashMap<Belief, AlphaVector> beliefAlphas = pickBestInitialAlphas(beliefs, bestAlphas);
		
		BeliefAlphaVectorPolicy alphasLast = bestAlphas;
		BeliefAlphaVectorPolicy alphasNext = new BeliefAlphaVectorPolicy();
		for(int i=0;i<numIterations && numImprovements>0;++i){
			
			HashSet<AlphaVector> keepAlphas = new HashSet<AlphaVector>();
			numImprovements = 0;
			
			System.out.println("Pessimistic policy iteration #" + i);
			
			bestImprovement = 0.001d;
			
			long startTime = new Date().getTime();
			
			Random r = new Random();
			
			List<JointBelief> beliefsTemp = new ArrayList<JointBelief>(beliefs);
			while(beliefsTemp.size()>0) {
				
				int ix = r.nextInt(beliefsTemp.size());
				JointBelief belief = beliefsTemp.remove(ix);
				
				AlphaVector oldBestBeliefAlpha = beliefAlphas.get(belief);
				double oldBeliefValue = belief.getBeliefFunction().dotProduct(oldBestBeliefAlpha.getValueFunction());
				
				
				BeliefBackup dpBackupTask = new BeliefBackup(belief,alphasLast,oldBeliefValue);
				dpBackupTask.run();
				
				if(dpBackupTask.result!=null) {
				
					double newBeliefValue = belief.getBeliefFunction().dotProduct(dpBackupTask.result.getValueFunction());
				
					System.out.println(" Belief value:" + oldBeliefValue + " -> " + newBeliefValue);
					if(newBeliefValue>oldBeliefValue) {
						BeliefAlphaVector newAlpha = dpBackupTask.result;
						
						MaxImprovementResult result =  getMaxImprovement(beliefAlphas,newAlpha);
						bestImprovement = result.maxImprovement;
	
						System.out.println(" Best improvement:" + bestImprovement);
						
						if(bestImprovement>tolerance) {

							if(alphasNext.addAlphaVector(newAlpha)) {
								beliefAlphas = updateBeliefValues(beliefAlphas, newAlpha, beliefsTemp);
								System.out.println("Num alphas:" + alphasNext.getAlphaVectors().size());
								numImprovements++;
							}
							
							beliefsTemp.removeAll(result.improvedBeliefs);
						}
						
					}		
				}
				else {
					keepAlphas.add(oldBestBeliefAlpha);
				}
				
				
				System.out.println(" Beliefs left:" + beliefsTemp.size());
				
			}
			
			System.out.println("Took " + ( new Date().getTime() - startTime) + " milliseconds");
			System.out.println();
			
			bestAlphas = alphasLast;
			alphasLast = alphasNext;
			alphasNext = new BeliefAlphaVectorPolicy();
			
			System.out.println("Passing through " + keepAlphas.size() + " alphas to the next iteration");
			
			int passThroughVectorIx = 0;
			for(AlphaVector alpha:keepAlphas) {
				passThroughVectorIx ++;
				System.out.println("Adding pass-through vector #" + passThroughVectorIx);
				alphasLast.addAlphaVector(alpha);
			}
		}
		
		pool.shutdownNow();
		return bestAlphas;
	}
	
	private class BeliefGetBestAlpha implements Runnable {
		
		public AlphaVector result;
		
		final private BeliefAlphaVectorPolicy alphas;
		final private JointBelief b;
		
		public BeliefGetBestAlpha(JointBelief b, BeliefAlphaVectorPolicy alphas) {
			this.b = b;
			this.alphas = alphas;
		}
		@Override
		public void run() {
			result = alphas.getAlphaVector(b);
		}
		
	}
	
	private class BeliefBackup implements Runnable {
		public BeliefAlphaVector result;
		
		final private BeliefAlphaVectorPolicy alphas;
		final private JointBelief b;
		final private double currValue;
		
		private HashMap<HashMap<DDVariable, Integer>,AlphaVector> conditionalPlans = new HashMap<HashMap<DDVariable, Integer>,AlphaVector>();
		
		public BeliefBackup(JointBelief b, BeliefAlphaVectorPolicy bestAlphas, double currValue) {
			this.b = b;
			this.alphas = bestAlphas;
			this.currValue = currValue;
		}
		
		@Override
		public void run() {
			
			HashMap<DDVariable,Integer> bestAct = null;
			HashMap<DDVariable,Integer> worstActOther = null;
			double bestActValue = -Double.MAX_VALUE;
			
			FactoredCondProbDD belFn = b.getBeliefFunction();
			
			for(HashMap<DDVariable,Integer> actSpacePt:pMe.getActionSpace()) {
				HashMap<HashMap<DDVariable, Integer>,AlphaVector> conditionalPlansTemp = new HashMap<HashMap<DDVariable, Integer>,AlphaVector>();
				
				double worstCaseActValue = Double.MAX_VALUE;
				
				AlgebraicDD restrRewFn = pMe.getRewardFunction().restrict(actSpacePt);
				for(HashMap<DDVariable,Integer> actSpaceOtherPt:pOther.getActionSpace()) {
					
					HashMap<HashMap<DDVariable, Integer>,AlphaVector> conditionalPlansTemp2 = new HashMap<HashMap<DDVariable, Integer>,AlphaVector>();
					
					
					double actValue = belFn.dotProduct(restrRewFn.restrict(actSpaceOtherPt));
					
					for(Entry<HashMap<DDVariable, Integer>, Double> e:b.getObservationProbabilities(actSpacePt, actSpaceOtherPt).entrySet()) {
						HashMap<DDVariable,Integer> obsSpacePt = e.getKey();
						double obsProb = e.getValue();
						
						//if(obsProb>0.0f) {
							FactoredCondProbDD nextBelief = b.getNextBeliefFunction(actSpacePt, actSpaceOtherPt, obsSpacePt);
							
							AlphaVector bestNextAlpha = alphas.getAlphaVector(nextBelief);
							
							conditionalPlansTemp2.put(obsSpacePt, bestNextAlpha);
							
							double obsValue = nextBelief.dotProduct(bestNextAlpha.getValueFunction());
							actValue += pMe.getDiscount()*obsProb*obsValue;
						//}
					}
					
					if(actValue<worstCaseActValue) {
						worstCaseActValue = actValue;
						worstActOther = actSpaceOtherPt;
						conditionalPlansTemp = conditionalPlansTemp2;
					}
				}
				
				if(worstCaseActValue>bestActValue) {
					bestActValue = worstCaseActValue;
					bestAct = actSpacePt;
					conditionalPlans = conditionalPlansTemp;
				}
				
			}
			
			if(bestActValue > currValue) {
				AlgebraicDD nextValFn = new AlgebraicDD(pMe.getStates(),0.0d);
				for(Entry<HashMap<DDVariable, Integer>,AlphaVector> cp: conditionalPlans.entrySet()) {
					HashMap<DDVariable, Integer> obs = cp.getKey();
					AlphaVector alpha = cp.getValue();
					double obsProb = b.getObservationProbabilities(bestAct, worstActOther).get(obs);
					nextValFn = nextValFn.plus(alpha.getValueFunction().multiply(obsProb*pMe.getDiscount()));
					
				}
				
				nextValFn = nextValFn.prime();
				
				FactoredCondProbDD dd = pMe.getTransitionFunction().restrict(bestAct).restrict(worstActOther);
				AlgebraicDD nextAlpha = dd.multiply(nextValFn).sumOut(pMe.getStatesPrime());
				nextAlpha = nextAlpha.plus(pMe.getRewardFunction().restrict(bestAct).restrict(worstActOther));
				
				result = new BeliefAlphaVector(bestAct,nextAlpha,b.getBeliefFunction().toProbabilityDD());
			}
		}
	}
}
