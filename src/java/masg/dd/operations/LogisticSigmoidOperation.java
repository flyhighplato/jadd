package masg.dd.operations;

public class LogisticSigmoidOperation implements UnaryOperation {

	@Override
	public double invoke(Double val) {
		double result = 1.0d/(1.0d + Math.exp(-val));
		assert !Double.isNaN(result) && !Double.isInfinite(result);
		return result;
	}

}
