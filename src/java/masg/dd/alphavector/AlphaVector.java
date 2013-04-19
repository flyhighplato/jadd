package masg.dd.alphavector;

import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.ProbDD;
import masg.dd.variables.DDVariable;

public interface AlphaVector {
	
	public AlgebraicDD getValueFunction();
	public ProbDD getWitnessPoint();
	public HashMap<DDVariable,Integer> getAction();
}
