package masg.dd.alphavector;

import java.util.ArrayList;
import java.util.Date;

import masg.dd.function.RealValueFunction;

public class DominantAlphaVectorCollection {
	protected ArrayList<AlphaVector> alphaVectors = new ArrayList<AlphaVector>();
	protected RealValueFunction valueFunction = null;
	
	double tolerance = 0.000001f;
	
	int insertsSincePrune = 0;
	
	public boolean add(AlphaVector alphaNew) throws Exception {
		
		if(alphaVectors.size()==0 || !isDominated(alphaNew)) {
			synchronized(alphaVectors) {
				alphaVectors.add(alphaNew);
				insertsSincePrune++;
			}
			updateValueFunction(alphaNew);
			
			return true;
		}

		return false;
	}
	
	
	private void updateValueFunction(AlphaVector alphaNew) throws Exception {
		synchronized(this) {
			if(valueFunction == null)
				valueFunction = alphaNew.getFn();
			else {
				valueFunction = new RealValueFunction(valueFunction.getDD().max(alphaNew.getFn().getDD()));
			}
			
			valueFunction.getDD().compress();
			
			System.out.println("  Value function has " + valueFunction.getDD().getRules().size() + " rules");
			System.out.println("  Value function total sum: " + valueFunction.getDD().getRules().getRuleValueSum());
			
			
			
			if(insertsSincePrune > 10) {
				//prune();
			}
		}
	}
	
	public final RealValueFunction getValueFunction() {
		synchronized(this) {
			return valueFunction;
		}
	}
	
	private boolean isDominated(AlphaVector alphaNew) throws Exception {
		synchronized(this) {
			if(valueFunction != null && valueFunction.dominates(alphaNew.getFn(), tolerance)) {
				return true;
			}
			
			return false;
		}
		
	}
	
	public final ArrayList<AlphaVector> getAlphaVectors() {
		return alphaVectors;
	}
}
