package masg.dd.alphavector.refactored;

import java.util.HashMap;

import masg.dd.refactored.AlgebraicDD;
import masg.dd.refactored.CondProbDD;
import masg.dd.vars.DDVariable;

public class BeliefAlphaVector {
	final protected HashMap<DDVariable,Integer> action;
	final protected CondProbDD witnessPt;
	final protected AlgebraicDD valueFn;
	
	public BeliefAlphaVector(HashMap<DDVariable,Integer> action, AlgebraicDD val, CondProbDD witness) {
		this.action = action;
		this.witnessPt = witness;
		this.valueFn = val;
	}
	
	public final AlgebraicDD getValueFunction() {
		return valueFn;
	}
	
	public final HashMap<DDVariable,Integer> getAction() {
		return new HashMap<DDVariable,Integer>(action);
	}
}
