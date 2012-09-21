package masg.dd.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.DecisionDiagram;
import masg.dd.context.CondProbDDContext;
import masg.dd.context.DecisionDiagramContext;
import masg.dd.context.ProbDD;
import masg.dd.rules.DecisionRule;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;


public class CondProbFunction implements DecisionDiagram{
	ArrayList<CondProbDD> ddList = new ArrayList<CondProbDD>();
	
	public CondProbFunction() {
		
	}
	
	public void appendDD(CondProbDD dd) throws Exception {
		ddList.add(dd);
	}
	
	public ArrayList<CondProbDD> getDDs() {
		return ddList;
	}
	
	public void compress() throws Exception {
		for(CondProbDD dd:ddList) {
			dd.compress();
		}
	}
	
	public double getValue(HashMap<DDVariable,Integer> varValues) throws Exception {
		double val = 1.0f;
		
		for(CondProbDD dd:ddList) {
			double retVal = dd.getValue(varValues);
			
			val*=retVal;
			
			if(val==0.0)
				return val;
		}
		
		return val;
	}
	
	public CondProbFunction sumOut(Collection<DDVariable> sumOutVars, boolean normalize) throws Exception {
		CondProbFunction newFn = new CondProbFunction();
		for(CondProbDD dd:ddList) {
			newFn.ddList.add(dd.sumOut(sumOutVars,normalize));
		}
		return newFn;
	}
	
	public CondProbFunction sumOutAllExcept(Collection<DDVariable> sumOutVars, boolean normalize) throws Exception {
		CondProbFunction newFn = new CondProbFunction();
		for(CondProbDD dd:ddList) {
			newFn.ddList.add(dd.sumOutAllExcept(sumOutVars,normalize));
		}
		return newFn;
	}
	
	protected HashMap<HashMap<DDVariable,Integer>, CondProbFunction> restrictCache = new HashMap<HashMap<DDVariable,Integer>, CondProbFunction>();
	
	public CondProbFunction restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		
		if(restrictCache.containsKey(varInstances)) {
			return restrictCache.get(varInstances);
		}
		
		CondProbFunction newFn = new CondProbFunction();
		for(CondProbDD dd:ddList) {
			newFn.ddList.add(dd.restrict(varInstances));
		}
		newFn = newFn.separate();
		newFn.compress();
		restrictCache.put(varInstances, newFn);
		
		return newFn;
	}
	
	private CondProbFunction separate() throws Exception {
		CondProbFunction newFn = new CondProbFunction();
		
		
		ArrayList<CondProbDD> ddListOld = ddList;
		
		while(true) {
			
			ArrayList<CondProbDD> ddListNew = new ArrayList<CondProbDD>();
			boolean changed = false;
			for(int i=0;i<ddListOld.size();i++) {
				CondProbDD dd1 = ddListOld.get(i);
				CondProbDDContext oldCtxt1 = (CondProbDDContext) dd1.getContext();
				ArrayList<DDVariable> vars1 = oldCtxt1.getVariableSpace().getVariables();
				
				boolean match = false;
				for(int j=0;j<ddListOld.size();j++) {
					if(i==j)
						continue;
					
					CondProbDD dd2 = ddListOld.get(j);
					CondProbDDContext oldCtxt2 = (CondProbDDContext) dd2.getContext();
					ArrayList<DDVariable> vars2 = oldCtxt2.getVariableSpace().getVariables();
					
					if(vars1.containsAll(vars2)) {
						match = true;
						changed = true;
						@SuppressWarnings("unchecked")
						ArrayList<DDVariable> varsOutNew = (ArrayList<DDVariable>) vars1.clone();
						varsOutNew.removeAll(vars2);
						CondProbDDContext newCtxt = new CondProbDDContext(new DDVariableSpace(), new DDVariableSpace(varsOutNew));
						
						CondProbDD ddNew = new CondProbDD(newCtxt);
						//TODO: This could be more efficient
						for(HashMap<DDVariable,Integer> varInstances:dd2.getContext().getVariableSpace()) {
							ProbDD addNew = dd1.restrict(varInstances).sumOut(varInstances.keySet());
							double mult = dd2.getValue(varInstances);
							
							for(DecisionRule r: addNew.getRules()) {
								r.value = r.value * mult;
								ddNew.getRules().add(r);
									
							}
						}
						
						ddNew.compress();
						ddNew.normalize();
						ddListNew.add(ddNew);
						break;
					}
				}
				
				if(!match) {
					ddListNew.add(dd1);
				}
			}
			
			if(!changed) {
				for(CondProbDD dd: ddListNew) {
					newFn.appendDD(dd);
				}
				return newFn;
			}
			
			ddListOld = ddListNew;
		}
		
	}
	
	public void normalize() {
		for(CondProbDD cdd:ddList) {
			cdd.normalize();
		}
	}
	
	public CondProbFunction times(double value){
		CondProbFunction newFn = new CondProbFunction();
		for(CondProbDD cddThis:ddList) {
			newFn.ddList.add(cddThis.times(value));
		}
		
		return newFn;
	}
	
	public CondProbFunction max(CondProbFunction fnOther) throws Exception {
		CondProbFunction newFn = new CondProbFunction();
		for(CondProbDD cddThis:ddList) {
			CondProbDD cddRes = null;
			for(CondProbDD cddOther:fnOther.ddList) {
				if(cddRes == null) {
					cddRes = cddThis.max(cddOther);
				}
				else {
					cddRes = cddRes.max(cddOther);
				}
			}
			newFn.ddList.add(cddRes);
		}
		return newFn;
	}
	
	public CondProbFunction plus(CondProbFunction fnOther) throws Exception {
		CondProbFunction newFn = new CondProbFunction();
		for(CondProbDD cddThis:ddList) {
			CondProbDD cddRes = null;
			for(CondProbDD cddOther:fnOther.ddList) {
				if(cddRes == null) {
					cddRes = cddThis.plus(cddOther);
				}
				else {
					cddRes = cddRes.plus(cddOther);
				}
			}
			newFn.ddList.add(cddRes);
		}
		return newFn;
	}
	
	public CondProbFunction times(CondProbFunction fnOther) throws Exception {
		CondProbFunction newFn = new CondProbFunction();
		for(CondProbDD cddThis:ddList) {
			CondProbDD cddRes = null;
			for(CondProbDD cddOther:fnOther.ddList) {
				if(cddRes == null) {
					cddRes = cddThis.times(cddOther);
				}
				else {
					cddRes = cddRes.times(cddOther);
				}
			}
			newFn.ddList.add(cddRes);
		}
		return newFn;
	}
	
	public RealValueFunction timesAndSumOut(RealValueFunction fnOther,Collection<DDVariable> sumOutVars) throws Exception {
		HashSet<DDVariable> vars = new HashSet<DDVariable>();
		
		for(CondProbDD cddThis:ddList) {
			vars.addAll(cddThis.getContext().getVariableSpace().getVariables());
		}
		
		AlgebraicDD dd = fnOther.getDD().expandRules(vars);
		
		for(CondProbDD cddThis:ddList) {
			ArrayList<DDVariable> varsTemp = new ArrayList<DDVariable>(cddThis.getContext().getVariableSpace().getVariables());
			varsTemp.retainAll(sumOutVars);
			dd = dd.times(cddThis);
			System.out.println("SizeTimes:" + dd.getRules().size());
			dd = dd.sumOut(varsTemp);
			System.out.println("SizeSum:" + dd.getRules().size());
			dd.compress();
			System.out.println("SizeCompress:" + dd.getRules().size());
			System.out.println();
		}
		
		return new RealValueFunction(dd);
	}
	
	public void primeAllContexts() throws Exception {
		for(CondProbDD cddThis:ddList) {
			CondProbDDContext cpContext = (CondProbDDContext) cddThis.getContext();
			if(cpContext.getInputVarSpace().getVariableCount()>0)
				throw new Exception("Can't prime.  This is still a conditional probability.");
			cpContext.getVariableSpace().prime();
			cpContext.getOutputVarSpace().prime();
		}
	}
	
	public void unprimeAllContexts() throws Exception {
		for(CondProbDD cddThis:ddList) {
			CondProbDDContext cpContext = (CondProbDDContext) cddThis.getContext();
			if(cpContext.getInputVarSpace().getVariableCount()>0)
				throw new Exception("Can't unprime.  This is still a conditional probability.");
			cpContext.getVariableSpace().unprime();
			cpContext.getOutputVarSpace().unprime();
		}
	}

	@Override
	public DecisionDiagramContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString() {
		return ddList==null?null:ddList.toString();
	}
}
