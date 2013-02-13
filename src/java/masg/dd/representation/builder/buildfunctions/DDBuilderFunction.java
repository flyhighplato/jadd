package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;

import masg.dd.representation.DDLeaf;
import masg.dd.variables.DDVariable;

public interface DDBuilderFunction {
	public DDLeaf invoke(HashMap<DDVariable,Integer> varValues);
}
