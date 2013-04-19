package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import masg.dd.alphavector.AlphaVector;
import masg.dd.alphavector.BestResponseAlphaVector;
import masg.dd.pomdp.AbstractPOMDP;
import masg.dd.pomdp.agent.belief.JointBelief;
import masg.dd.pomdp.agent.belief.JointBeliefRegion;

public class BestResponseAlphaVectorPolicyBuilder {
	
	private ExecutorService pool = null;
	private AbstractPOMDP pMe;
	private AbstractPOMDP pOther;
	private AlphaVectorPolicy responseToPolicy;
	
	private HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>> bestResponseAlphaVectors = new HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>>();
	
	public BestResponseAlphaVectorPolicyBuilder(AbstractPOMDP pMe, AbstractPOMDP pOther, AlphaVectorPolicy responseToPolicy) {
		this.pMe = pMe;
		this.pOther = pOther;
		this.responseToPolicy = responseToPolicy;
	}
	
	
	private double getMaxImprovement(
			HashMap<JointBelief, BestResponseAlphaVector> beliefResponseAlphas,
			BestResponseAlphaVector newAlpha) {
		
		double maxImprovement = -Double.MAX_VALUE;
		
		for(Entry<JointBelief, BestResponseAlphaVector> e:beliefResponseAlphas.entrySet()) {

			BestResponseAlphaVector oldAlpha = e.getValue();
			JointBelief b = e.getKey();
					
			double oldValue = b.getBeliefFunction().dotProduct(oldAlpha.getValueFunction());
			double newValue = b.getBeliefFunction().dotProduct(newAlpha.getValueFunction());
			
			if(maxImprovement < (newValue-oldValue)) {
				maxImprovement = (newValue-oldValue);
			}
		}
		
		return maxImprovement;	
	}
	
	private HashMap<JointBelief, BestResponseAlphaVector> updateBeliefValues(
			HashMap<JointBelief, BestResponseAlphaVector> beliefResponseAlphas,
			AlphaVector responseAlpha, 
			BestResponseAlphaVector newAlpha, 
			List<JointBelief> beliefsTemp) {
		
		HashMap<JointBelief, BestResponseAlphaVector> newBeliefResponseAlphas = new HashMap<JointBelief, BestResponseAlphaVector>();
		
		for(Entry<JointBelief, BestResponseAlphaVector> e:beliefResponseAlphas.entrySet()) {

			BestResponseAlphaVector oldAlpha = e.getValue();
			JointBelief b = e.getKey();
					
			double oldValue = b.getBeliefFunction().dotProduct(oldAlpha.getValueFunction());
			double newValue = b.getBeliefFunction().dotProduct(newAlpha.getValueFunction());
			
			
			if(oldValue<newValue) {
				newBeliefResponseAlphas.put(b, newAlpha);
			}
			else {
				newBeliefResponseAlphas.put(b, oldAlpha);
			}
			
			
		}
		
		return newBeliefResponseAlphas;
	}
	
	private double getBeliefValue(
			HashMap<JointBelief, BestResponseAlphaVector> beliefResponseAlphas,
			JointBelief belief
			) {
		
		BestResponseAlphaVector bestResponseAlpha = beliefResponseAlphas.get(belief);
		
		double beliefValue = belief.getBeliefFunction().dotProduct(bestResponseAlpha.getValueFunction());
		
		
		return beliefValue;
	}
	public BestResponseAlphaVectorPolicy build(JointBeliefRegion belRegion, int numIterations) {
		
		pool = Executors.newFixedThreadPool(20);
		
		if(bestResponseAlphaVectors.size()<=0) {
			PessimisticAlphaVectorPolicyBuilder pessPolBuilder = new PessimisticAlphaVectorPolicyBuilder(pMe,pOther);
			BeliefAlphaVectorPolicy pessPol = pessPolBuilder.build(belRegion, numIterations);
			
			for(AlphaVector alpha: responseToPolicy.getAlphaVectors()) {
				
				ArrayList<BestResponseAlphaVector> alphaResponses = new ArrayList<BestResponseAlphaVector>();
				for(AlphaVector alpha2: pessPol.getAlphaVectors()) {
					alphaResponses.add(new BestResponseAlphaVector(alpha2));
				}
				
				bestResponseAlphaVectors.put(alpha, alphaResponses);
			}
		}
			
		List<JointBelief> beliefs = belRegion.getBeliefSamples();
		
		ArrayList<Future<JointBeliefGetBestAlpha>> futureBestPicks = new ArrayList<Future<JointBeliefGetBestAlpha>>();
		for(JointBelief belief:beliefs) {
			JointBeliefGetBestAlpha getBestAlphaTask = new JointBeliefGetBestAlpha(belief,bestResponseAlphaVectors,responseToPolicy);
			futureBestPicks.add( pool.submit(getBestAlphaTask,getBestAlphaTask) );
		}
		
		long startTime = new Date().getTime();
		HashMap<JointBelief,BestResponseAlphaVector> beliefResponseAlphas = new HashMap<JointBelief,BestResponseAlphaVector>();
		
		System.out.println("Picking best initial alpha vectors");
		for(Future<JointBeliefGetBestAlpha> f:futureBestPicks) {
			try {
				JointBeliefGetBestAlpha task = f.get();
				BestResponseAlphaVector bestAlpha = task.result;
	
				beliefResponseAlphas.put(task.b,bestAlpha);
				
				
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
		int numImprovements = 1;
		for(int i=0;i<numIterations && numImprovements>0;++i){
			numImprovements = 0;
			
			System.out.println("Best response iteration #" + i);
			
			bestImprovement = 0.001d;
			
			startTime = new Date().getTime();
			
			Random r = new Random();
			
			
			List<JointBelief> beliefsTemp = new ArrayList<JointBelief>(beliefs);
			
			while(beliefsTemp.size()>0) {
				
				int ix = r.nextInt(beliefsTemp.size());
				JointBelief belief = beliefsTemp.remove(ix);
				
				AlphaVector responseAlpha = responseToPolicy.getAlphaVector(belief.reverse());
				
				double oldBeliefValue = getBeliefValue(beliefResponseAlphas,belief);
				
				
				JointBeliefBackup dpBackupTask = new JointBeliefBackup(pMe, belief, responseToPolicy, bestResponseAlphaVectors);
				dpBackupTask.run();
				
				double newBeliefValue = belief.getBeliefFunction().dotProduct(dpBackupTask.result.getValueFunction());
				
				System.out.println(" Belief value:" + oldBeliefValue + " -> " + newBeliefValue);
				if(newBeliefValue>=oldBeliefValue) {
					BestResponseAlphaVector newAlpha = dpBackupTask.result;
					
					bestImprovement = getMaxImprovement(beliefResponseAlphas, newAlpha);
					
					System.out.println(" Best improvement:" + bestImprovement);
					
					if(bestImprovement > pMe.getTolerance()) {

						boolean dominated = false;
						
						if(bestResponseAlphaVectors.containsKey(responseAlpha)) {
							for(BestResponseAlphaVector alpha:bestResponseAlphaVectors.get(responseAlpha)) {
								if(newAlpha.getValueFunction().maxAbsDiff(alpha.getValueFunction()) < pMe.getTolerance()) {
									dominated = true;
									break;
								}
							}
							
						}
						else {
							bestResponseAlphaVectors.put(responseAlpha, new ArrayList<BestResponseAlphaVector>());
						}
						
						if(!dominated) {
							
							bestResponseAlphaVectors.get(responseAlpha).add(newAlpha);
							beliefResponseAlphas = updateBeliefValues(beliefResponseAlphas, responseAlpha, newAlpha, beliefsTemp);
							System.out.println("Num response alphas for this strategy:" + bestResponseAlphaVectors.get(responseAlpha).size());
							numImprovements++;
						}
						
						
					}
					
				}
				
				System.out.println(" Beliefs left:" + beliefsTemp.size());
				
			}
			
			System.out.println("Took " + ( new Date().getTime() - startTime) + " milliseconds");
			System.out.println();
		}
		
		pool.shutdownNow();
		return new BestResponseAlphaVectorPolicy(bestResponseAlphaVectors, responseToPolicy);
	}

	private class JointBeliefGetBestAlpha implements Runnable {
		
		public BestResponseAlphaVector result;
		
		final private HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>> responseAlphas;
		final private AlphaVectorPolicy responseToPolicy;
		
		final private JointBelief b;
		
		public JointBeliefGetBestAlpha(JointBelief b, HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>> bestResponseAlphaVectors, AlphaVectorPolicy responseToPolicy) {
			this.b = b;
			this.responseAlphas = bestResponseAlphaVectors;
			this.responseToPolicy = responseToPolicy;
		}
		
		@Override
		public void run() {
			
			JointBelief b2 = b.reverse();
			AlphaVector otherAlpha = responseToPolicy.getAlphaVector(b2);
			double bestVal = -Double.MAX_VALUE;
			
			for(BestResponseAlphaVector alpha:responseAlphas.get(otherAlpha)) {
				double tempVal = b.getBeliefFunction().dotProduct(alpha.getValueFunction());
				
				if(tempVal>=bestVal) {
					bestVal = tempVal;
					result = alpha;
				}
			}
		}
		
	}
}
