package masg.dd.rules.operations.refactored;

public class DivisionOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		if(val1==0)
			return 0.0f;
		return val1/val2;
	}

}
