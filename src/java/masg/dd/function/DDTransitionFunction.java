package masg.dd.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import masg.dd.AlgebraicDecisionDiagram;
import masg.dd.DecisionDiagram;
import masg.dd.DecisionDiagramContext;
import masg.dd.DecisionRule;
import masg.dd.cpt.CondProbADD;
import masg.dd.cpt.CondProbDDContext;
import masg.dd.vars.DDVariable;
import masg.dd.vars.DDVariableSpace;


public class DDTransitionFunction implements DecisionDiagram{
	ArrayList<CondProbADD> ddList = new ArrayList<CondProbADD>();
	
	public DDTransitionFunction() {
		
	}
	
	public void appendDD(CondProbADD dd) throws Exception {
		ddList.add(dd);
	}
	
	public ArrayList<CondProbADD> getDDs() {
		return ddList;
	}
	
	public void compress() throws Exception {
		for(CondProbADD dd:ddList) {
			dd.compress();
		}
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
	
	public DDTransitionFunction sumOut(Collection<DDVariable> sumOutVars, boolean normalize) throws Exception {
		DDTransitionFunction newFn = new DDTransitionFunction();
		for(CondProbADD dd:ddList) {
			newFn.ddList.add(dd.sumOut(sumOutVars,normalize));
		}
		return newFn;
	}
	
	public DDTransitionFunction restrict(HashMap<DDVariable,Integer> varInstances) throws Exception {
		DDTransitionFunction newFn = new DDTransitionFunction();
		for(CondProbADD dd:ddList) {
			newFn.ddList.add(dd.restrict(varInstances));
		}
		
		return newFn.separate();
	}
	
	private DDTransitionFunction separate() throws Exception {
		DDTransitionFunction newFn = new DDTransitionFunction();
		
		
		ArrayList<CondProbADD> ddListOld = ddList;
		
		while(true) {
			
			ArrayList<CondProbADD> ddListNew = new ArrayList<CondProbADD>();
			boolean changed = false;
			for(int i=0;i<ddListOld.size();i++) {
				CondProbADD dd1 = ddListOld.get(i);
				CondProbDDContext oldCtxt1 = (CondProbDDContext) dd1.getContext();
				ArrayList<DDVariable> vars1 = oldCtxt1.getVariableSpace().getVariables();
				
				boolean match = false;
				for(int j=0;j<ddListOld.size();j++) {
					if(i==j)
						continue;
					
					CondProbADD dd2 = ddListOld.get(j);
					CondProbDDContext oldCtxt2 = (CondProbDDContext) dd2.getContext();
					ArrayList<DDVariable> vars2 = oldCtxt2.getVariableSpace().getVariables();
					
					if(vars1.containsAll(vars2)) {
						match = true;
						changed = true;
						@SuppressWarnings("unchecked")
						ArrayList<DDVariable> varsOutNew = (ArrayList<DDVariable>) vars1.clone();
						varsOutNew.removeAll(vars2);
						CondProbDDContext newCtxt = new CondProbDDContext(new DDVariableSpace(), new DDVariableSpace(varsOutNew));
						
						CondProbADD ddNew = new CondProbADD(newCtxt);
						//TODO: This could be more efficient
						for(HashMap<DDVariable,Integer> varInstances:dd2.getContext().getVariableSpace()) {
							AlgebraicDecisionDiagram addNew = dd1.restrict(varInstances).sumOut(varInstances.keySet());
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
				for(CondProbADD dd: ddListNew) {
					newFn.appendDD(dd);
				}
				return newFn;
			}
			
			ddListOld = ddListNew;
		}
		
	}
	
	public DDTransitionFunction multiply(AlgebraicDecisionDiagram add) throws Exception {
		DDTransitionFunction newFn = new DDTransitionFunction();
		for(CondProbADD cdd:ddList) {
			newFn.ddList.add(cdd.multiply(add));
		}
		return newFn;
	}
	
	public void normalize() {
		for(CondProbADD cdd:ddList) {
			cdd.normalize();
		}
	}
	
	public DDTransitionFunction multiply(DDTransitionFunction fnOther) throws Exception {
		DDTransitionFunction newFn = new DDTransitionFunction();
		for(CondProbADD cddThis:ddList) {
			CondProbADD cddRes = null;
			for(CondProbADD cddOther:fnOther.ddList) {
				if(cddRes == null) {
					cddRes = cddThis.multiply(cddOther);
				}
				else {
					cddRes = cddRes.multiply(cddOther);
				}
			}
			newFn.ddList.add(cddRes);
		}
		return newFn;
	}
	
	public void unprimeAllContexts() throws Exception {
		for(CondProbADD cddThis:ddList) {
			cddThis.getContext().getVariableSpace().unprime();
		}
	}

	@Override
	public DecisionDiagramContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}
}
