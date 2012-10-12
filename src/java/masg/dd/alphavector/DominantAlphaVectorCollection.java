package masg.dd.alphavector;

import java.util.ArrayList;

import masg.dd.function.RealValueFunction;

public class DominantAlphaVectorCollection {
	protected ArrayList<AlphaVector> alphaVectors = new ArrayList<AlphaVector>();
	protected RealValueFunction valueFunction;
	
	double tolerance = 0.000001f;
	
	synchronized public boolean add(AlphaVector alphaNew) throws Exception {
		
		if(!isDominated(alphaNew)) {
			synchronized(this) {
				alphaVectors.add(alphaNew);
			}
			updateValueFunction(alphaNew);
			return true;
		}

		
		return false;
	}
	
	private void prune() throws Exception {
		synchronized(this) {
			if(valueFunction!=null && alphaVectors!=null) {
				for(int i=0;i<alphaVectors.size();++i) {
					AlphaVector alphaOld = alphaVectors.get(0);
					if(valueFunction.dominates(alphaOld.getFn(), tolerance)) {
						alphaVectors.remove(i);
						--i;
					}
				}
			}
		}
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
			
			
			
			if(alphaVectors.size()%10 == 1) {
				System.out.println("  Pruning dominated alpha vectors...");
				prune();
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
		synchronized(this) {
			return alphaVectors;
		}
	}
}
