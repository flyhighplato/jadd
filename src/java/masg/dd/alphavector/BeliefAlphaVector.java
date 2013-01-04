package masg.dd.alphavector;

import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.ProbDD;
import masg.dd.variables.DDVariable;

public class BeliefAlphaVector {
	final protected HashMap<DDVariable,Integer> action;
	final protected ProbDD witnessPt;
	final protected AlgebraicDD valueFn;
	
	public BeliefAlphaVector(HashMap<DDVariable,Integer> action, AlgebraicDD val, ProbDD witness) {
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
