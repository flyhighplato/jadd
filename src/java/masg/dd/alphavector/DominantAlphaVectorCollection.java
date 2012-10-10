package masg.dd.alphavector;

import java.util.ArrayList;

import masg.dd.function.RealValueFunction;

public class DominantAlphaVectorCollection {
	protected ArrayList<AlphaVector> alphaVectors = new ArrayList<AlphaVector>();
	protected RealValueFunction valueFunction;
	
	double tolerance = 0.001f;
	
	public boolean add(AlphaVector alphaNew) throws Exception {
		if(!isDominated(alphaNew)) {
			alphaVectors.add(alphaNew);
			updateValueFunction(alphaNew);
			return true;
		}
		
		return false;
	}
	
	private void updateValueFunction(AlphaVector alphaNew) throws Exception {
		if(valueFunction == null)
			valueFunction = alphaNew.getFn();
		else {
			valueFunction = new RealValueFunction(valueFunction.getDD().max(alphaNew.getFn().getDD()));
		}
		
		valueFunction.getDD().compress();
	}
	
	public RealValueFunction getValueFunction() {
		return valueFunction;
	}
	
	public boolean isDominated(AlphaVector alphaNew) throws Exception {
		if(valueFunction != null && valueFunction.dominates(alphaNew.getFn(), tolerance)) {
			return true;
		}
		
		return false;
	}
	
	public final ArrayList<AlphaVector> getAlphaVectors() {
		return alphaVectors;
	}
}
