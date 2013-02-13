package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;

import masg.dd.representation.DDLeaf;
import masg.dd.variables.DDVariable;

public class DDBuilderConstantFunction implements DDBuilderFunction {
	double val;
	
	public DDBuilderConstantFunction(double val) {
		this.val = val;
	}
	
	@Override
	public DDLeaf invoke(HashMap<DDVariable, Integer> varValues) {
		return new DDLeaf(null,val);
	}

}
