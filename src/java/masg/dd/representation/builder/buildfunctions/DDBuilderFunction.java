package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;

import masg.dd.variables.DDVariable;

public interface DDBuilderFunction {
	public Double invoke(HashMap<DDVariable,Integer> varValues);
}
