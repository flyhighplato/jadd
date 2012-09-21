package masg.dd

import groovy.lang.Closure;

import java.util.List;

import masg.dd.function.CondProbFunction
import masg.dd.vars.DDVariable;

class CondProbFunctionBuilder {
	protected CondProbFunction fn = new CondProbFunction()
	
	public add(Map<String,Integer> varsIn, List<DDVariable> varsOut, c) {
		add(varsIn.collect{k,v -> new DDVariable(k,v)},varsOut.collect{k,v -> new DDVariable(k,v)},c)
	}
	
	public add(List<DDVariable> varsIn, List<DDVariable> varsOut, double val) {
		fn.appendDD(CondProbDDBuilder.build(varsIn,varsOut,val));
	}
	
	public add(List<DDVariable> varsIn, List<DDVariable> varsOut, Closure<Double> c) {
		fn.appendDD(CondProbDDBuilder.build(varsIn,varsOut,c));
	}
	
	public CondProbFunction build() {
		return fn;
	}
}
