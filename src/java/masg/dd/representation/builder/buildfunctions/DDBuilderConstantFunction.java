package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;

import masg.dd.variables.DDVariable;

public class DDBuilderConstantFunction implements DDBuilderFunction {
	double val;
	
	public DDBuilderConstantFunction(double val) {
		this.val = val;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		return val;
	}

}
