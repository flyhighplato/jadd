package masg.dd.operations;

public class IsEqualOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		return Math.abs(val1 - val2)<0.0001f? 1.0f:0.0f;
	}

}
