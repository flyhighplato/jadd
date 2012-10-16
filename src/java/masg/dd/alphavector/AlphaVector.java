package masg.dd.alphavector;

import java.util.HashMap;

import masg.dd.function.CondProbFunction;
import masg.dd.function.RealValueFunction;
import masg.dd.vars.DDVariable;

public class AlphaVector {
	
	final protected HashMap<DDVariable,Integer> action;
	final protected RealValueFunction fn;
	protected CondProbFunction witnessPt;
	
	public AlphaVector(HashMap<DDVariable,Integer> action, RealValueFunction val, CondProbFunction witness) {
		this.action = action;
		this.fn = val;
		this.witnessPt = witness;
	}
	
	public final RealValueFunction getFn() {
		return fn;
	}
}
