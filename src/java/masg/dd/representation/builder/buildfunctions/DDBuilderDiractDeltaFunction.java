package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;

import masg.dd.variables.DDVariable;

public class DDBuilderDiractDeltaFunction implements DDBuilderFunction {

	int defaultScopeId = 0;
	HashMap<DDVariable,Integer> defaultPt;
	public DDBuilderDiractDeltaFunction(int defaultScopeId, HashMap<DDVariable,Integer> pt) {
		defaultPt = pt;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		if(varValues.equals(defaultPt)) {
			return 1.0d;
		}
		
		return 0.0d;
	}

}
