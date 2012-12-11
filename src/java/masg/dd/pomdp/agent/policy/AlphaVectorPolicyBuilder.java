package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.POMDP;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.pomdp.agent.belief.BeliefRegion;
import masg.dd.representations.dag.MutableDDLeaf;
import masg.dd.representations.tables.TableDD;
import masg.dd.variables.DDVariable;


public class AlphaVectorPolicyBuilder {
	double discFactor = 0.9f;
	double tolerance = 0.00001f;
	
	ArrayList<BeliefAlphaVector> bestAlphas = new ArrayList<BeliefAlphaVector>();
	
	private ExecutorService pool = Executors.newFixedThreadPool(10);
	
	POMDP p;
	
	public AlphaVectorPolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	public AlphaVectorPolicy buildPureStrategyAlphas() {
		for(HashMap<DDVariable,Integer> actSpacePt:p.getActionSpace()) {
			CondProbDD currBel = p.getInitialBelief().toConditionalProbabilityFn();
			
			AlgebraicDD actionAlpha = p.getRewardFunction(actSpacePt).multiply(currBel);
			
			for(int i=0;i<50;++i) {
				currBel = p.getTransitionFunction(actSpacePt).multiply(currBel).sumOut(p.getStates()).normalize().unprime();
				actionAlpha = p.getRewardFunction(actSpacePt).multiply(currBel).plus(actionAlpha.multiply(discFactor));
			}
			
			//System.out.println(actionAlpha);
			bestAlphas.add(new BeliefAlphaVector(actSpacePt, actionAlpha, currBel));
		}
		
		return new AlphaVectorPolicy(bestAlphas);
	}
	
	private HashMap<Belief,BeliefAlphaVector> getNotImprovedValues(HashMap<Belief,BeliefAlphaVector> oldValues, BeliefAlphaVector newAlpha) {
		
		HashMap<Belief,BeliefAlphaVector> newValues = new HashMap<Belief,BeliefAlphaVector>();
		
		for(Entry<Belief, BeliefAlphaVector> e:oldValues.entrySet()) {

				BeliefAlphaVector oldAlpha = e.getValue();
				Belief b = e.getKey();
						
				double oldValue = oldAlpha.getValueFunction().multiply(b.getBeliefFunction()).getTotalWeight();;
				double newValue = newAlpha.getValueFunction().multiply(b.getBeliefFunction()).getTotalWeight();
				
				//System.out.println(newValue + " " + oldValue + " " + (newValue-oldValue));
				
				if(oldValue>newValue) {
					newValues.put(b, newAlpha);
				}

			
		}
		
		return newValues;	
	}
	
	public AlphaVectorPolicy build(BeliefRegion belRegion, int numIterations) {
		if(bestAlphas.size()<=0)
			buildPureStrategyAlphas();
		
		
		
		for(int i=0;i<numIterations;++i){
			System.out.println("Iteration #" + i);
			
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
			
			startTime = new Date().getTime();
			
			while(!beliefAlphas.keySet().isEmpty()) {
				
				Belief belief = beliefAlphas.keySet().iterator().next();
				BeliefAlphaVector oldBestBeliefAlpha = beliefAlphas.get(belief);
				double oldBeliefValue = oldBestBeliefAlpha.getValueFunction().multiply(belief.getBeliefFunction()).getTotalWeight();
				
				
				BeliefBackup dpBackupTask = new BeliefBackup(belief,bestAlphas);
				dpBackupTask.run();
				
				ArrayList<BeliefAlphaVector> newAlphas = new ArrayList<BeliefAlphaVector>();
				
				
				double newBeliefValue = dpBackupTask.result.getValueFunction().multiply(belief.getBeliefFunction()).getTotalWeight();
				beliefAlphas.remove(belief);

				if(newBeliefValue>=oldBeliefValue) {
					BeliefAlphaVector newAlpha = dpBackupTask.result;
					
					//beliefAlphas.put(belief,newAlpha);
					beliefAlphas = getNotImprovedValues(beliefAlphas,newAlpha);
					
					
					boolean isDominated = false;
					AlgebraicDD zeroDD = new AlgebraicDD(TableDD.build(p.getStates(), 0.0f).asDagDD());
					for(BeliefAlphaVector oldAlpha:bestAlphas) {
						
						if(!isDominated) {
							AlgebraicDD subDD = newAlpha.getValueFunction().minus(oldAlpha.getValueFunction());
							if(subDD.max(zeroDD).getTotalWeight() <= 0.0f) {
								isDominated = true;
							}
						}
						
						AlgebraicDD subDD = oldAlpha.getValueFunction().minus(newAlpha.getValueFunction());
						if(subDD.max(zeroDD).getTotalWeight() > 0.0f) {
							newAlphas.add(oldAlpha);
						}
						
					}
					if(!isDominated) {
						newAlphas.add(newAlpha);
					}
					bestAlphas = newAlphas;
				}
				
				System.out.println("Number of beliefs left: " + beliefAlphas.keySet().size());
				System.out.println("Number of alpha vectors: " + bestAlphas.size());
				
			}
			
			System.out.println("Took " + ( new Date().getTime() - startTime) + " milliseconds");
			System.out.println();
		}
		
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
			double bestActionValue = -Double.MAX_VALUE;
			BeliefAlphaVector bestActionAlpha = null;
			
			for(HashMap<DDVariable,Integer> actSpacePt:p.getActionSpace()) {
				AlgebraicDD actionAlpha = null;

				CondProbDD belFn = b.getBeliefFunction();
				//belFn = belFn.plus(tolerance);
				
				actionAlpha = p.getRewardFunction(actSpacePt).multiply(belFn);
				
				for(Entry<HashMap<DDVariable, Integer>, Double> e:b.getObservationProbabilities(actSpacePt).entrySet()) {
					HashMap<DDVariable,Integer> obsSpacePt = e.getKey();
					double obsProb = e.getValue();
					
					
					if(obsProb>0.0f) {
						CondProbDD nextBelief = b.getNextBeliefFunction(actSpacePt, obsSpacePt);
						
						double bestNextVal = -Double.MAX_VALUE;
						AlgebraicDD bestNextAlpha = null;
						
						for(BeliefAlphaVector alpha:alphas) {
							AlgebraicDD tempNextAlpha = alpha.getValueFunction().multiply(nextBelief).multiply(obsProb);
							
							double expectedValue = tempNextAlpha.getTotalWeight();
							
							if(expectedValue>=bestNextVal) {
								bestNextVal = expectedValue;
								bestNextAlpha = tempNextAlpha;
							}
							
						}
						
						if(bestNextAlpha!=null) {
							actionAlpha = actionAlpha.plus(bestNextAlpha);
						}
					}
				}
				
				double expectedActionValue = actionAlpha.multiply(discFactor).getTotalWeight();
				
				if(expectedActionValue>=bestActionValue) {
					bestActionValue = expectedActionValue;
					bestActionAlpha = new BeliefAlphaVector(actSpacePt,actionAlpha,b.getBeliefFunction());
				}
			}
			
			
			
			result = bestActionAlpha;
		}
	}
}
