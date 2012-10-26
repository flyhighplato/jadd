package masg.dd.rules.operations.refactored;

public class MultiplicationOperation implements BinaryOperation {
	public double invoke(Double val1, Double val2) {
		if(val1==null || val2==null) {
			System.out.println("NULL");
		}
		return val1*val2;
	}
}
