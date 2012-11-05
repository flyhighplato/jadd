package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
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
import masg.dd.variables.DDVariable;


public class PolicyBuilder {
	double discFactor = 0.9f;
	double tolerance = 0.00001f;
	
	ArrayList<BeliefAlphaVector> bestAlphas = new ArrayList<BeliefAlphaVector>();
	
	private ExecutorService pool = Executors.newFixedThreadPool(16);
	
	POMDP p;
	
	
	public PolicyBuilder(POMDP p) {
		this.p = p;
	}
	
	
	public void build(BeliefRegion belRegion, int numIterations) {
		
		
		List<Belief> beliefs = belRegion.getBeliefSamples();
		
		//pool = Executors.newCachedThreadPool();
		
		for(int i=0;i<numIterations;++i) {
			System.out.println("Iteration #" + i);
			
			long startTime = new Date().getTime();
			
			ArrayList<BeliefAlphaVector> newAlphas = new ArrayList<BeliefAlphaVector>();
			
			ArrayList<Future<BeliefBackup>> futureBackups = new ArrayList<Future<BeliefBackup>>();
			for(Belief belief:beliefs) {
				BeliefBackup getBestAlphaTask = new BeliefBackup(belief,bestAlphas);
				futureBackups.add( pool.submit(getBestAlphaTask,getBestAlphaTask) );
			}
			
			for(Future<BeliefBackup> f:futureBackups) {
				try {
					newAlphas.add(f.get().result);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println("--Backups took " + (new Date().getTime()-startTime));
			
			startTime = new Date().getTime();
			
			System.out.println("--Removing dominated alpha vectors");
			
			HashSet<BeliefAlphaVector> usedUniqAlphaVectors = new HashSet<BeliefAlphaVector>();
			
			ArrayList<Future<BeliefGetBestAlpha>> futureBestPicks = new ArrayList<Future<BeliefGetBestAlpha>>();
			for(Belief belief:beliefs) {
				BeliefGetBestAlpha getBestAlphaTask = new BeliefGetBestAlpha(belief,newAlphas);
				futureBestPicks.add( pool.submit(getBestAlphaTask,getBestAlphaTask) );
			}
			
			for(Future<BeliefGetBestAlpha> f:futureBestPicks) {
				try {
					usedUniqAlphaVectors.add(f.get().result);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println("--Removing dominated alpha vectors took " + (new Date().getTime()-startTime));

			newAlphas.clear();
			newAlphas.addAll(usedUniqAlphaVectors);
			
			System.out.println("Number of new alpha vectors:" + newAlphas.size());
			bestAlphas = newAlphas;
			
			System.out.println();
		}
		
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
			result = b.pickBestAlpha(alphas);
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
				AlgebraicDD actionAlpha = b.getImmediateValueFunction(actSpacePt);
				
				for(Entry<HashMap<DDVariable, Integer>, Double> e:b.getObservationProbabilities(actSpacePt).entrySet()) {
					HashMap<DDVariable,Integer> obsSpacePt = e.getKey();
					double obsProb = e.getValue();
					
					if(obsProb>0.0f) {
						
						CondProbDD nextBelief = b.getNextBeliefFunction(actSpacePt, obsSpacePt);
						
						double bestNextVal = -Double.MAX_VALUE;
						AlgebraicDD bestNextAlpha = null;
						
						for(BeliefAlphaVector alpha:alphas) {
							AlgebraicDD tempNextAlpha = alpha.getValueFunction().multiply(nextBelief);
							double expectedValue = tempNextAlpha.getTotalWeight();
							
							if(expectedValue>=bestNextVal) {
								bestNextVal = expectedValue;
								bestNextAlpha = tempNextAlpha;
							}
						}
						
						if(bestNextAlpha!=null) {
							bestNextAlpha = bestNextAlpha.multiply(obsProb);
							bestNextAlpha = bestNextAlpha.multiply(discFactor);
							actionAlpha = actionAlpha.plus(bestNextAlpha);
						}
					}
				}
				
				double expectedActionValue = actionAlpha.getTotalWeight();
				
				if(expectedActionValue>=bestActionValue) {
					bestActionValue = expectedActionValue;
					bestActionAlpha = new BeliefAlphaVector(actSpacePt,actionAlpha,b.getBeliefFunction());
				}
			}
			
			
			
			result = bestActionAlpha;
		}
	}
}
