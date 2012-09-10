package masg.dd
import java.util.List;
import java.util.Map;

import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace
import groovy.lang.Closure;
import masg.dd.cpt.CondProbADD
import masg.dd.cpt.CondProbDDContext

class CondProbADDBuilder extends AlgebraicDecisionDiagramBuilder {
	
	static CondProbADD build(Map<String,Integer> varsIn, Map<String,Integer> varsOut, Closure<Double> c) {
		build(varsIn.collect{k,v -> new DDVariable(k,v)},varsOut.collect{k,v -> new DDVariable(k,v)},c)
	}
	
	static CondProbADD build(List<DDVariable> varsIn, List<DDVariable> varsOut, Closure<Double> c) {
		
		CondProbDDContext context = makeContext(varsIn, varsOut)
		
		CondProbADD resDD = initializeDD(context)

		populateDD(resDD,c)
		
		return resDD
	}
	
	
	
	protected static CondProbDDContext makeContext(List<DDVariable> varsIn, List<DDVariable> varsOut) {
		DDVariableSpace inVarSpace = new DDVariableSpace();
		inVarSpace.addVariables(varsIn)
		
		DDVariableSpace outVarSpace = new DDVariableSpace();
		outVarSpace.addVariables(varsOut)
		
		return new CondProbDDContext(inVarSpace,outVarSpace)
	}
	
	protected static CondProbADD initializeDD(CondProbDDContext context) {
		return new CondProbADD(context)
	}
}
