package masg.dd.representation.builder.buildfunctions;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.representation.DDLeaf;
import masg.dd.variables.DDVariable;

public class DDBuilderClosureFunction implements DDBuilderFunction {

	Closure<Double> c;
	
	public DDBuilderClosureFunction(Closure<Double> c) {
		this.c = c;
	}
	
	@Override
	public DDLeaf invoke(HashMap<DDVariable, Integer> varValues) {
		HashMap<String,Integer> args = new HashMap<String,Integer>();
		for(Entry<DDVariable,Integer> e:varValues.entrySet()) {
			args.put(e.getKey().getName(), e.getValue());
		}
		return  new DDLeaf(null,c.call(args));
	}

}
