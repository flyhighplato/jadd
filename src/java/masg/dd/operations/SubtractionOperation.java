package masg.dd.operations;

import masg.dd.representation.DDLeaf;

public class SubtractionOperation implements BinaryOperation {

	@Override
	public DDLeaf invoke(DDLeaf val1, DDLeaf val2) {
		return new DDLeaf(null,val1.getValue()-val2.getValue());
	}

}
