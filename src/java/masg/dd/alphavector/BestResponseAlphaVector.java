package masg.dd.alphavector;

import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.ProbDD;
import masg.dd.variables.DDVariable;

public class BestResponseAlphaVector implements AlphaVector {
	final protected HashMap<DDVariable,Integer> action;
	final protected ProbDD witnessPt;
	final protected AlgebraicDD valueFn;
	final protected AlphaVector bestResponseTo;
	
	public BestResponseAlphaVector(AlphaVector alphaVector) {
		this.action = alphaVector.getAction();
		this.witnessPt = alphaVector.getWitnessPoint();
		this.valueFn = alphaVector.getValueFunction();
		this.bestResponseTo = null;
	}
	
	public BestResponseAlphaVector(HashMap<DDVariable,Integer> action, AlgebraicDD val, AlphaVector bestResponseTo, ProbDD witness) {
		this.action = action;
		this.witnessPt = witness;
		this.valueFn = val;
		this.bestResponseTo = bestResponseTo;
	}
	
	public final AlgebraicDD getValueFunction() {
		return valueFn;
	}

	public final ProbDD getWitnessPoint() {
		return witnessPt;
	}
	
	public final HashMap<DDVariable,Integer> getAction() {
		return new HashMap<DDVariable,Integer>(action);
	}

	public AlphaVector getAlphaOther() {
		return bestResponseTo;
	}
}
