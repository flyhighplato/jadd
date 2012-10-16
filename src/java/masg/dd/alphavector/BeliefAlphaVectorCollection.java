package masg.dd.alphavector;

import java.util.ArrayList;

import masg.dd.function.CondProbFunction;

public class BeliefAlphaVectorCollection {
	protected ArrayList<BeliefAlphaVector> alphaVectors = new ArrayList<BeliefAlphaVector>();
	
	double tolerance = 0.000001f;
	
	public void add(BeliefAlphaVector alphaNew) {
		alphaVectors.add(alphaNew);
	}
	
	
	public double getBeliefValue(CondProbFunction belief) throws Exception {
		double closestDist = -Double.MAX_VALUE;
		BeliefAlphaVector closestAlpha = null;
		
		for(BeliefAlphaVector alphaVector:alphaVectors) {
			double dist = alphaVector.getBeliefPointDistance(belief);
			if(closestAlpha == null || closestDist > dist) {
				closestAlpha = alphaVector;
				closestDist = dist;
			}
		}
		
		if(closestAlpha !=null) {
			return closestAlpha.getValue();
		}
		else {
			return Double.NaN;
		}
	}
	
	public final ArrayList<BeliefAlphaVector> getAlphaVectors() {
		return alphaVectors;
	}
}
