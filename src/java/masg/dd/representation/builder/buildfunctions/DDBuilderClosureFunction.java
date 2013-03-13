package masg.dd.representation.builder.buildfunctions;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map.Entry;

import masg.dd.variables.DDVariable;

public class DDBuilderClosureFunction implements DDBuilderFunction {

	Closure<Double> c;
	int defaultScopeId = 0;
	public DDBuilderClosureFunction(int defaultScopeId, Closure<Double> c) {
		this.c = c;
		this.defaultScopeId = defaultScopeId;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		HashMap args = new HashMap();
		for(Entry<DDVariable,Integer> e:varValues.entrySet()) {
			
			if(e.getKey().getScope()!=defaultScopeId) {
				HashMap val = new HashMap();
				val.put(e.getKey().getName(), e.getValue());
				args.put(e.getKey().getScope(), val);
			}
			else {
				args.put(e.getKey().getName(), e.getValue());
			}
		}
		return  c.call(args);
	}

}
