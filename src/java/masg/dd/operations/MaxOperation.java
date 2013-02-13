package masg.dd.operations;

import masg.dd.representation.DDLeaf;

public class MaxOperation implements BinaryOperation {

	@Override
	public DDLeaf invoke(DDLeaf val1, DDLeaf val2) {
		return new DDLeaf(null,Math.max(val1.getValue(), val2.getValue()));
	}

}
