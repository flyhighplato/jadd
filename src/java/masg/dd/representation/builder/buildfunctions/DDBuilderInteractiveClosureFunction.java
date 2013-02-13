package masg.dd.representation.builder.buildfunctions;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import masg.dd.representation.DDElement;
import masg.dd.representation.DDInteractiveLeaf;
import masg.dd.representation.DDLeaf;
import masg.dd.variables.DDVariable;

public class DDBuilderInteractiveClosureFunction implements DDBuilderFunction {

	@SuppressWarnings("rawtypes")
	Closure<List> c;
	
	@SuppressWarnings("rawtypes")
	public DDBuilderInteractiveClosureFunction( Closure<List> c) {
		this.c = c;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DDLeaf invoke(HashMap<DDVariable, Integer> varValues) {
		HashMap<String,Integer> args = new HashMap<String,Integer>();
		for(Entry<DDVariable,Integer> e:varValues.entrySet()) {
			args.put(e.getKey().getName(), e.getValue());
		}
		List result = c.call(args);
		double value = (Double) result.get(0);
		HashMap<DDElement, Double> interactiveFunctions = (HashMap<DDElement, Double>) result.get(1);
		return new DDInteractiveLeaf(null,value,interactiveFunctions);
	}

}
