package masg.dd.representation.builder.buildfunctions;

import java.util.HashMap;

import masg.dd.representation.DDElement;
import masg.dd.representation.DDLeaf;
import masg.dd.variables.DDVariable;

public class DDBuilderDDFunction implements DDBuilderFunction {

	DDElement el;
	
	public DDBuilderDDFunction(DDElement el) {
		this.el = el;
	}
	@Override
	public DDLeaf invoke(HashMap<DDVariable, Integer> varValues) {
		return new DDLeaf(null,el.getValue(varValues));
	}

}
