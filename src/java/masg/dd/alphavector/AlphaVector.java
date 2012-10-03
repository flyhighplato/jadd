package masg.dd.alphavector;

import java.util.HashMap;

import masg.dd.function.RealValueFunction;
import masg.dd.vars.DDVariable;

public class AlphaVector {
	
	final protected HashMap<DDVariable,Integer> action;
	final protected RealValueFunction fn;
	protected HashMap<HashMap<DDVariable,Integer>,AlphaVector> condPlan = new HashMap<HashMap<DDVariable,Integer>,AlphaVector>();
	
	public AlphaVector(HashMap<DDVariable,Integer> action, RealValueFunction val) {
		this.action = action;
		this.fn = val;
	}
	
	public AlphaVector(HashMap<DDVariable,Integer> action, RealValueFunction val,HashMap<HashMap<DDVariable,Integer>,AlphaVector> condPlan) {
		this.action = action;
		this.fn = val;
		this.condPlan = condPlan;
	}
	
	public final RealValueFunction getFn() {
		return fn;
	}
	
	public void setCondPlanForObs(HashMap<DDVariable,Integer> obs, AlphaVector plan) {
		condPlan.put(obs, plan);
	}
}
