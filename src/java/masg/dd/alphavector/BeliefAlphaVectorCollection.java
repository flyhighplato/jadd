package masg.dd.alphavector;

import java.util.ArrayList;

import masg.dd.function.CondProbFunction;
import masg.dd.function.RealValueFunction;

public class BeliefAlphaVectorCollection {
	protected ArrayList<BeliefAlphaVector> alphaVectors = new ArrayList<BeliefAlphaVector>();
	
	double tolerance = 0.000001f;
	
	public void add(BeliefAlphaVector alphaNew) {
		alphaVectors.add(alphaNew);
	}
	
	public BeliefAlphaVector getBestAlphaVector(CondProbFunction belief) throws Exception {
		BeliefAlphaVector bestAlphaVector = null;
		double bestValue = -Double.MAX_VALUE;
		
		for(BeliefAlphaVector alphaVector:alphaVectors) {
			RealValueFunction belValueFn = belief.times(alphaVector.getValueFunction());
			
			double totalValue = belValueFn.getDD().getRules().getRuleValueSum();
			if(totalValue>bestValue) {
				bestValue = totalValue;
				bestAlphaVector = alphaVector;
			}
		}
		
		return bestAlphaVector;
	}
	
	public final ArrayList<BeliefAlphaVector> getAlphaVectors() {
		return alphaVectors;
	}
}
