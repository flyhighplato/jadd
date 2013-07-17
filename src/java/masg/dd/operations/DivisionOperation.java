package masg.dd.operations;

public class DivisionOperation implements BinaryOperation {

	@Override
	public double invoke(Double val1, Double val2) {
		if(val1==0)
			return 0.0d;
		
		double res = val1/val2;
		if(Double.isInfinite(res)) {
			System.out.println("Infinite result for division");
			return 0.0d;
		}
		return val1/val2;
	}
}
