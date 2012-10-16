package masg.dd.alphavector;

import java.util.HashMap;

import masg.dd.function.CondProbFunction;
import masg.dd.vars.DDVariable;

public class BeliefAlphaVector {
	
	final protected HashMap<DDVariable,Integer> action;
	protected CondProbFunction witnessPt;
	final protected double value;
	
	public BeliefAlphaVector(HashMap<DDVariable,Integer> action, double val, CondProbFunction witness) {
		this.action = action;
		this.witnessPt = witness;
		this.value = val;
	}
	
	public double getBeliefPointDistance(CondProbFunction beliefOther) throws Exception {
		return witnessPt.euclideanDistance(beliefOther);
	}
	
	public double getValue() {
		return value;
	}
}
