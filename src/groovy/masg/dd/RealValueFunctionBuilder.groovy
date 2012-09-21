package masg.dd;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import masg.dd.function.RealValueFunction;
import masg.dd.vars.DDVariable;

public class RealValueFunctionBuilder {
	static RealValueFunction build(Map<String,Integer> vars, Closure<Double> c) {
		build(vars.collect{k,v -> new DDVariable(k,v)},c)
	}
	
	static RealValueFunction build(List<DDVariable> vars, Closure<Double> c) {
		return new RealValueFunction(AlgebraicDDBuilder.build(vars, c));	
	}
	
	static RealValueFunction build(List<DDVariable> vars, double val) {
		return new RealValueFunction(AlgebraicDDBuilder.build(vars, val));
	}
}
