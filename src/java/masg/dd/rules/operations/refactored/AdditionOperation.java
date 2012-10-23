package masg.dd.rules.operations.refactored;

public class AdditionOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		return val1 + val2;
	}

}
