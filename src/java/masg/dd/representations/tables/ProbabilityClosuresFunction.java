package masg.dd.representations.tables;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.variables.DDVariable;

public class ProbabilityClosuresFunction implements DDBuilderFunction {

	Closure<Double>[] closures;
	
	public ProbabilityClosuresFunction(Closure<Double>... closures) {
		this.closures = closures;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		HashMap<String,Integer> args = new HashMap<String,Integer>();
		for(Entry<DDVariable,Integer> e:varValues.entrySet()) {
			args.put(e.getKey().getName(), e.getValue());
		}
		
		double val = 1.0d;
		
		if(closures!=null) {
			for(Closure<Double> c:closures) {
				val*=c.call(args);
			}
		}
		
		return val;
	}
}
