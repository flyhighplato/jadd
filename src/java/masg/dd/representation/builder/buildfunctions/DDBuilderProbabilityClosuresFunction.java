package masg.dd.representation.builder.buildfunctions;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.representation.builder.DDBuilderFunction;
import masg.dd.variables.DDVariable;

public class DDBuilderProbabilityClosuresFunction implements DDBuilderFunction {

	Closure<Double>[] closures;
	
	public DDBuilderProbabilityClosuresFunction(Closure<Double>... closures) {
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
