package masg.dd.function;

import java.util.ArrayList;
import java.util.Collection;
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
			
			val*=retVal;
			
			if(val==0.0)
				return val;
			
		}
		
		return val;
	}
	
	public DDTransitionFunction sumOut(Collection<DDVariable> sumOutVars) throws Exception {
		DDTransitionFunction newFn = new DDTransitionFunction();
		for(CondProbADD dd:ddList) {
			newFn.ddList.add(dd.sumOut(sumOutVars));
		}
		return newFn;
	}
	
	public DDTransitionFunction fix(HashMap<DDVariable,Integer> varInstances) throws Exception {
		DDTransitionFunction newFn = new DDTransitionFunction();
		for(CondProbADD dd:ddList) {
			newFn.ddList.add(dd.fix(varInstances));
		}
		return newFn;
	}
}
