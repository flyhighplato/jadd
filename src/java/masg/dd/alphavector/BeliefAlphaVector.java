package masg.dd.alphavector;

import java.util.HashMap;

import masg.dd.function.CondProbFunction;
import masg.dd.function.RealValueFunction;
import masg.dd.vars.DDVariable;

public class BeliefAlphaVector {
	
	final protected HashMap<DDVariable,Integer> action;
	final protected CondProbFunction witnessPt;
	final protected RealValueFunction value;
	
	public BeliefAlphaVector(HashMap<DDVariable,Integer> action, RealValueFunction val, CondProbFunction witness) {
		this.action = action;
		this.witnessPt = witness;
		this.value = val;
	}
	
	public double getBeliefPointDistance(CondProbFunction beliefOther) throws Exception {
		return witnessPt.euclideanDistance(beliefOther);
	}
	
	public RealValueFunction getValueFunction() {
		return value;
	}

}
