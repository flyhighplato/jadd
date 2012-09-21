package masg.dd
import java.util.List;
import java.util.Map;

import masg.dd.context.CondProbDDContext;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace
import groovy.lang.Closure;

class CondProbDDBuilder extends AlgebraicDDBuilder {
	
	static CondProbDD build(Map<String,Integer> varsIn, Map<String,Integer> varsOut, Closure<Double> c) {
		build(varsIn.collect{k,v -> new DDVariable(k,v)},varsOut.collect{k,v -> new DDVariable(k,v)},c)
	}
	
	static CondProbDD build(List<DDVariable> varsIn, List<DDVariable> varsOut, Closure<Double> c) {
		
		CondProbDDContext context = makeContext(varsIn, varsOut)
		
		CondProbDD resDD = initializeDD(context)

		populateDD(resDD,c)
		
		return resDD
	}
	
	static CondProbDD build(List<DDVariable> varsIn, List<DDVariable> varsOut, double val) {
		
		CondProbDDContext context = makeContext(varsIn, varsOut)
		
		CondProbDD resDD = initializeDD(context)

		populateDD(resDD,val)
		
		return resDD
	}
	
	
	protected static CondProbDDContext makeContext(List<DDVariable> varsIn, List<DDVariable> varsOut) {
		DDVariableSpace inVarSpace = new DDVariableSpace();
		inVarSpace.addVariables(varsIn)
		
		DDVariableSpace outVarSpace = new DDVariableSpace();
		outVarSpace.addVariables(varsOut)
		
		return new CondProbDDContext(inVarSpace,outVarSpace)
	}
	
	protected static CondProbDD initializeDD(CondProbDDContext context) {
		return new CondProbDD(context)
	}
}
