package masg.dd

import groovy.lang.Closure;

import java.util.List;

import masg.dd.function.DDTransitionFunction
import masg.dd.vars.DDVariable;

class DDTransitionFunctionBuilder {
	protected DDTransitionFunction fn = new DDTransitionFunction()
	
	public add(Map<String,Integer> varsIn, List<DDVariable> varsOut, c) {
		add(varsIn.collect{k,v -> new DDVariable(k,v)},varsOut.collect{k,v -> new DDVariable(k,v)},c)
	}
	
	public add(List<DDVariable> varsIn, List<DDVariable> varsOut, Closure<Double> c) {
		fn.appendDD(CondProbADDBuilder.build(varsIn,varsOut,c));
	}
	
	public DDTransitionFunction build() {
		return fn;
	}
}
