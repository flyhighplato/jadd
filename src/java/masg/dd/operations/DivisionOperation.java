package masg.dd.operations;

import masg.dd.representation.DDLeaf;

public class DivisionOperation implements BinaryOperation {

	@Override
	public DDLeaf invoke(DDLeaf val1, DDLeaf val2) {
		if(val1.getValue()==0)
			return new DDLeaf(null,0.0f);
		return new DDLeaf(null,val1.getValue()/val2.getValue());
	}

}
