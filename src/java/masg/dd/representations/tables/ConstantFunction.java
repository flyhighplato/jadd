package masg.dd.representations.tables;

import java.util.HashMap;

import masg.dd.variables.DDVariable;

public class ConstantFunction implements DDBuilderFunction {
	double val;
	
	public ConstantFunction(double val) {
		this.val = val;
	}
	
	@Override
	public Double invoke(HashMap<DDVariable, Integer> varValues) {
		return val;
	}

}
