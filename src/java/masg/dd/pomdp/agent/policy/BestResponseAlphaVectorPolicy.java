package masg.dd.pomdp.agent.policy;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.alphavector.AlphaVector;
import masg.dd.alphavector.BestResponseAlphaVector;
import masg.dd.pomdp.agent.belief.Belief;
import masg.dd.pomdp.agent.belief.JointBelief;
import masg.dd.variables.DDVariable;

public class BestResponseAlphaVectorPolicy implements AlphaVectorPolicy {
	AlphaVectorPolicy responseToPolicy;
	private HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>> bestResponseAlphaVectors = new HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>>();

	public BestResponseAlphaVectorPolicy(HashMap<AlphaVector, ArrayList<BestResponseAlphaVector>> bestResponseAlphaVectors, AlphaVectorPolicy responseToPolicy) {
		this.bestResponseAlphaVectors = bestResponseAlphaVectors;
		this.responseToPolicy = responseToPolicy;
	}
	
	public BestResponseAlphaVector getAlphaVector(Belief b) {
		double bestVal = -Double.MAX_VALUE;
		BestResponseAlphaVector bestAlpha = null;
		
		JointBelief b2 = ((JointBelief) b).reverse();
		AlphaVector otherAlpha = responseToPolicy.getAlphaVector(b2);
		
		for(BestResponseAlphaVector alpha:bestResponseAlphaVectors.get(otherAlpha)) {
			double tempVal;
			AlgebraicDD valFn = b.getBeliefFunction().multiply(alpha.getValueFunction());
			tempVal = valFn.getTotalWeight();
			
			if(tempVal>=bestVal) {
				bestVal = tempVal;
				bestAlpha = alpha;
			}
		}
		
		return bestAlpha;
	}

	
	@Override
	public HashMap<DDVariable, Integer> getAction(Belief belief) {
		if(belief instanceof JointBelief) {
			return (getAlphaVector((JointBelief)belief)).getAction();
		}
		return null;
	}

	@Override
	public ArrayList<AlphaVector> getAlphaVectors() {
		ArrayList<AlphaVector> alphasAll = new ArrayList<AlphaVector>();
		
		for(ArrayList<BestResponseAlphaVector> alphas:bestResponseAlphaVectors.values()) {
			alphasAll.addAll(alphas);
		}
		
		return alphasAll;
	}
}
