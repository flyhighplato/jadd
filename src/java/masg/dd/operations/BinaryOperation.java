package masg.dd.operations;

import masg.dd.representation.DDLeaf;

public interface BinaryOperation {
	public DDLeaf invoke(DDLeaf val1, DDLeaf val2);
}
