package masg.dd.representations.tables;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.variables.DDVariable;

public class ClosureFunction implements DDBuilderFunction {

	Closure<Double> c;
	
	public ClosureFunction(Closure<Double> c) {
		this.c = c;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		HashMap<String,Integer> args = new HashMap<String,Integer>();
		for(Entry<DDVariable,Integer> e:varValues.entrySet()) {
			args.put(e.getKey().getName(), e.getValue());
		}
		return  c.call(args);
	}

}
