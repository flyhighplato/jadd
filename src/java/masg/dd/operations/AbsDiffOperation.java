package masg.dd.operations;

import masg.dd.representation.DDLeaf;

public class AbsDiffOperation implements BinaryOperation {

	@Override
	public DDLeaf invoke(DDLeaf val1, DDLeaf val2) {
		return new DDLeaf(null, Math.abs(val1.getValue()-val2.getValue()));
	}

}
