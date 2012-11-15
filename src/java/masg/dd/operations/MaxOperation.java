package masg.dd.operations;

public class MaxOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		return Math.max(val1, val2);
	}

}
