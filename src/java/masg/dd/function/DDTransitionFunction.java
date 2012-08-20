package masg.dd.function;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.cpt.CondProbADD;
import masg.dd.vars.DDVariable;


public class DDTransitionFunction {
	ArrayList<CondProbADD> ddList = new ArrayList<CondProbADD>();
	
	public DDTransitionFunction() {
		
	}
	
	public void appendDD(CondProbADD dd) throws Exception {
		ddList.add(dd);
	}
	
	public ArrayList<CondProbADD> getDDs() {
		return ddList;
	}
	
	public double getValue(HashMap<DDVariable,Integer> varValues) throws Exception {
		double val = 1.0f;
		
		for(CondProbADD dd:ddList) {
			double retVal = dd.getValue(varValues);
			System.out.println("retVal:" + retVal);
			val*=retVal;
			
			if(val==0.0)
				return val;
			
		}
		
		return val;
	}
}
