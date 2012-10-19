package masg.dd.function;

import java.util.Collection;
import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.DecisionDiagram;
import masg.dd.context.DDContext;
import masg.dd.vars.DDVariable;

public class RealValueFunction implements DecisionDiagram{
	private AlgebraicDD dd;
	
	public RealValueFunction(AlgebraicDD dd) {
		this.dd = dd;
	}
	
	public double getValue(HashMap<DDVariable,Integer> varValues) throws Exception {
		return dd.getValue(varValues);
	}
	
	protected HashMap<HashMap<DDVariable,Integer>, RealValueFunction> restrictCache = new HashMap<HashMap<DDVariable,Integer>, RealValueFunction>();
	
	public RealValueFunction restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		if(restrictCache.containsKey(varInstances)) {
			return restrictCache.get(varInstances);
		}
		RealValueFunction fnNew = new RealValueFunction(dd.restrict(varInstances));
		
		restrictCache.put(varInstances, fnNew);
		
		return fnNew;
	}
	
	public RealValueFunction sumOut(Collection<DDVariable> sumOutVar) throws Exception {
		return new RealValueFunction(dd.sumOut(sumOutVar));
	}
	
	public boolean dominates(RealValueFunction fnOther, double tolerance) throws Exception {
		return dd.dominates(fnOther.getDD(), tolerance);
	}
	
	public double maxDiff(RealValueFunction fnOther) throws Exception {
		return dd.maxDiff(fnOther.getDD());
	}
	
	public RealValueFunction plus(RealValueFunction fnOther) throws Exception {
		return new RealValueFunction(dd.plus(fnOther.getDD()));
	}
	
	public RealValueFunction times(double value) throws Exception {
		return new RealValueFunction(dd.times(value));
	}
	
	public void primeAllContexts() throws Exception {
		dd.prime();
	}
	
	public void unprimeAllContexts() throws Exception {
		dd.unprime();
	}
	
	public final AlgebraicDD getDD() {
		return dd;
	}
	
	@Override
	public DDContext getContext() {
		return null;
	}
	
	public String toString() {
		return dd.toString();
	}

}
