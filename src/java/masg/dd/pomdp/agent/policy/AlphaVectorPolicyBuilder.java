package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.pomdp.agent.belief.BeliefRegion;
import masg.dd.representation.DDInfo;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;


public class AlphaVectorPolicyBuilder {
	double discFactor = 0.9f;
	double tolerance = 0.00001f;
	
	ArrayList<BeliefAlphaVector> bestAlphas = new ArrayList<BeliefAlphaVector>();
	
	private ExecutorService pool = null;
	
	POMDP p;
	
	public AlphaVectorPolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	public AlphaVectorPolicy buildPureStrategyAlphas() {
		//DDVariableSpace qFnVars = p.getStates().union(p.getStatesPrime());
		
		for(HashMap<DDVariable,Integer> actSpacePt:p.getActions()) {
			FactoredCondProbDD currBel = p.getInitialBelief();
			
			System.out.println("Generating pure strategy for action:" + actSpacePt);
			
			AlgebraicDD actionAlpha = new AlgebraicDD(DDBuilder.build(new DDInfo(p.getStates(),false),0.0d).getRootNode());
			double bellmanError = 20*tolerance;
			
			for(int i=0;i<50 && bellmanError>tolerance;++i) {
				System.out.println("Iteration #" + i);
				actionAlpha.prime();
				
				AlgebraicDD actionAlphaNew = p.getTransitionFunction(actSpacePt).multiply(actionAlpha);
				
				actionAlphaNew = actionAlphaNew.sumOut(p.getStatesPrime());
				actionAlphaNew = actionAlphaNew.multiply(discFactor);
				
				actionAlphaNew = new AlgebraicDD(DDBuilder.approximate(actionAlphaNew.getFunction(), bellmanError * (1.0d-discFactor)/2.0d).getRootNode());
				
				actionAlphaNew = actionAlphaNew.plus(p.getRewardFunction(actSpacePt));
				
				
				bellmanError = DDBuilder.findMaxLeaf(actionAlphaNew.absDiff(actionAlpha).getFunction()).getValue();
				System.out.println("bellmanError: " + bellmanError);
				actionAlpha = actionAlphaNew;
			}
			
			System.out.println(DDBuilder.findMaxLeaf(actionAlpha.getFunction()));
			System.out.println(DDBuilder.findMaxLeaf(actionAlpha.multiply(-1.0f).getFunction()));
			bestAlphas.add(new BeliefAlphaVector(actSpacePt, actionAlpha, currBel.toProbabilityDD()));
		}
		
		return new AlphaVectorPolicy(bestAlphas);
	}
	
	private HashMap<Belief,BeliefAlphaVector> updateBeliefValues(HashMap<Belief,BeliefAlphaVector> oldValues, BeliefAlphaVector newAlpha, List<Belief> beliefsTemp) {
		
		HashMap<Belief,BeliefAlphaVector> newValues = new HashMap<Belief,BeliefAlphaVector>();
		
		for(Entry<Belief, BeliefAlphaVector> e:oldValues.entrySet()) {

				BeliefAlphaVector oldAlpha = e.getValue();
				Belief b = e.getKey();
				
				AlgebraicDD oldValueDD = b.getBeliefFunction().multiply(oldAlpha.getValueFunction());
				AlgebraicDD newValueDD = b.getBeliefFunction().multiply(newAlpha.getValueFunction());
				double oldValue = oldValueDD.getTotalWeight();
				double newValue = newValueDD.getTotalWeight();
				
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
	
	private double getMaxImprovement(HashMap<Belief,BeliefAlphaVector> oldValues, BeliefAlphaVector newAlpha) {
		
		double maxImprovement = -Double.MAX_VALUE;
		
		for(Entry<Belief, BeliefAlphaVector> e:oldValues.entrySet()) {

				BeliefAlphaVector oldAlpha = e.getValue();
				Belief b = e.getKey();
						
				AlgebraicDD oldValueDD = b.getBeliefFunction().multiply(oldAlpha.getValueFunction());
				AlgebraicDD newValueDD = b.getBeliefFunction().multiply(newAlpha.getValueFunction());
				double oldValue = oldValueDD.getTotalWeight();
				double newValue = newValueDD.getTotalWeight();
				
				if(maxImprovement < (newValue-oldValue)) {
					maxImprovement = (newValue-oldValue);
				}

			
		}
		
		return maxImprovement;	
	}
	
	public AlphaVectorPolicy build(BeliefRegion belRegion, int numIterations) {
		pool = Executors.newFixedThreadPool(20);
		
		if(bestAlphas.size()<=0)
			buildPureStrategyAlphas();
			
		List<Belief> beliefs = belRegion.getBeliefSamples();

		ArrayList<Future<BeliefGetBestAlpha>> futureBestPicks = new ArrayList<Future<BeliefGetBestAlpha>>();
		for(Belief belief:beliefs) {
			BeliefGetBestAlpha getBestAlphaTask = new BeliefGetBestAlpha(belief,bestAlphas);
			futureBestPicks.add( pool.submit(getBestAlphaTask,getBestAlphaTask) );
		}
		
		long startTime = new Date().getTime();
		HashMap<Belief,BeliefAlphaVector> beliefAlphas = new HashMap<Belief,BeliefAlphaVector>();
		System.out.println("Picking best initial alpha vectors");
		for(Future<BeliefGetBestAlpha> f:futureBestPicks) {
			try {
				BeliefGetBestAlpha task = f.get();
				BeliefAlphaVector bestAlpha = task.result;
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
		
		double bestImprovement = 1.0d;
		for(int i=0;i<numIterations && bestImprovement>0.001d && bestAlphas.size()<numIterations;++i){
			System.out.println("Iteration #" + i);
			
			bestImprovement = 0.001d;
			
			startTime = new Date().getTime();
			
			Random r = new Random();
			
			
			List<Belief> beliefsTemp = new ArrayList<Belief>(beliefs);
			while(beliefsTemp.size()>0) {
				
				int ix = r.nextInt(beliefsTemp.size());
				Belief belief = beliefsTemp.remove(ix);
				
				BeliefAlphaVector oldBestBeliefAlpha = beliefAlphas.get(belief);
				double oldBeliefValue = belief.getBeliefFunction().multiply(oldBestBeliefAlpha.getValueFunction()).getTotalWeight();
				
				
				BeliefBackup dpBackupTask = new BeliefBackup(belief,bestAlphas);
				dpBackupTask.run();
				
				double newBeliefValue = belief.getBeliefFunction().multiply(dpBackupTask.result.getValueFunction()).getTotalWeight();
				
				System.out.println(" Belief value:" + oldBeliefValue + " -> " + newBeliefValue);
				if(newBeliefValue>=oldBeliefValue) {
					BeliefAlphaVector newAlpha = dpBackupTask.result;
					
					bestImprovement = getMaxImprovement(beliefAlphas,newAlpha);
					
					System.out.println(" Best improvement:" + bestImprovement);
					
					if(bestImprovement>tolerance) {
						
						boolean dominated = false;
						for(BeliefAlphaVector alpha:bestAlphas) {
							if(DDBuilder.findMaxLeaf(newAlpha.getValueFunction().minus(alpha.getValueFunction()).getFunction()).getValue()<tolerance) {
								dominated = true;
								break;
							}
						}
						
						if(!dominated) {
							bestAlphas.add(newAlpha);
							beliefAlphas = updateBeliefValues(beliefAlphas, newAlpha, beliefsTemp);
							System.out.println("Num alphas:" + bestAlphas.size());
						}
						
					}
					
				}
				
				System.out.println(" Beliefs left:" + beliefsTemp.size());
				
			}
			
			System.out.println("Took " + ( new Date().getTime() - startTime) + " milliseconds");
			System.out.println();
		}
		
		pool.shutdownNow();
		return new AlphaVectorPolicy(bestAlphas);
	}
	
	private class BeliefGetBestAlpha implements Runnable {
		
		public BeliefAlphaVector result;
		
		final private List<BeliefAlphaVector> alphas;
		final private Belief b;
		
		public BeliefGetBestAlpha(Belief b, List<BeliefAlphaVector> alphas) {
			this.b = b;
			this.alphas = Collections.unmodifiableList(alphas);
		}
		@Override
		public void run() {
			result= b.pickBestAlpha(alphas);
		}
		
	}
	
	private class BeliefBackup implements Runnable {
		public BeliefAlphaVector result;
		
		final private List<BeliefAlphaVector> alphas;
		final private Belief b;
		
		public BeliefBackup(Belief b, List<BeliefAlphaVector> alphas) {
			this.b = b;
			this.alphas = Collections.unmodifiableList(alphas);
		}
		
		@Override
		public void run() {
			HashMap<HashMap<DDVariable, Integer>,BeliefAlphaVector> conditionalPlans = new HashMap<HashMap<DDVariable, Integer>,BeliefAlphaVector>();
			HashMap<DDVariable,Integer> bestAct = null;
			double bestActValue = -Double.MAX_VALUE;
			
			FactoredCondProbDD belFn = b.getBeliefFunction();
			
			for(HashMap<DDVariable,Integer> actSpacePt:p.getActions()) {
				double actValue = belFn.multiply(p.getRewardFunction(actSpacePt)).getTotalWeight();
				
				for(Entry<HashMap<DDVariable, Integer>, Double> e:b.getObservationProbabilities(actSpacePt).entrySet()) {
					HashMap<DDVariable,Integer> obsSpacePt = e.getKey();
					double obsProb = e.getValue();
					
					if(obsProb>0.0f) {
						FactoredCondProbDD nextBelief = b.getNextBeliefFunction(actSpacePt, obsSpacePt);
						
						double bestObsValue = -Double.MAX_VALUE;
						
						for(BeliefAlphaVector alpha:alphas) {
							AlgebraicDD tempNextAlpha = nextBelief.multiply(alpha.getValueFunction());
							
							double expectedValue = tempNextAlpha.getTotalWeight();
							
							if(expectedValue>=bestObsValue) {
								bestObsValue = expectedValue;
								conditionalPlans.put(obsSpacePt, alpha);
							}
							
						}
						
						actValue += discFactor*obsProb*bestObsValue;
					}
				}
				
				if(actValue>=bestActValue) {
					bestActValue = actValue;
					bestAct = actSpacePt;
				}
				
			}
			
			AlgebraicDD nextValFn = new AlgebraicDD(p.getStates(),0.0d);
			for(Entry<HashMap<DDVariable, Integer>,BeliefAlphaVector> cp: conditionalPlans.entrySet()) {
				HashMap<DDVariable, Integer> obs = cp.getKey();
				BeliefAlphaVector alpha = cp.getValue();
				double obsProb = b.getObservationProbabilities(bestAct).get(obs);
				nextValFn = nextValFn.plus(alpha.getValueFunction().multiply(obsProb*discFactor));
				
			}
			
			nextValFn = nextValFn.prime();
			
			
			FactoredCondProbDD dd = p.getTransitionFunction(bestAct);
			AlgebraicDD nextAlpha = dd.multiply(nextValFn);
			
			nextAlpha = new AlgebraicDD(DDBuilder.approximate(nextAlpha.getFunction(), tolerance).getRootNode());
			
			nextAlpha = nextAlpha.sumOut(p.getStatesPrime());
			
			
			nextAlpha = nextAlpha.plus(p.getRewardFunction(bestAct));
			
			result = new BeliefAlphaVector(bestAct,nextAlpha,b.getBeliefFunction().toProbabilityDD());
			
			
		}
	}
}
