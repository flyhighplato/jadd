package masg.dd.refactored;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import masg.dd.context.DDContext;
import masg.dd.rules.refactored.MutableDDElement;
import masg.dd.vars.DDVariable;

public class CondProbDD {
	private ArrayList<AlgebraicDD> indepFns = new ArrayList<AlgebraicDD>();
	private ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
	private ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>();
	
	public CondProbDD(ArrayList<ArrayList<ArrayList<DDVariable>>> varsLst, List<Closure<Double>> closures) {
		for(int i=0;i<varsLst.size();++i) {
			ArrayList<DDVariable> conditionalVars = varsLst.get(i).get(0);
			ArrayList<DDVariable> vars = varsLst.get(i).get(1);
			vars.removeAll(conditionalVars);
			
			
			HashSet<DDVariable> temp = new HashSet<DDVariable>(conditionalVars);
			temp.addAll(this.condVars);
			this.condVars = new ArrayList<DDVariable>();
			
			for(DDVariable v: DDContext.canonicalVariableOrdering) {
				if(temp.contains(v)) {
					this.condVars.add(v);
				}
			}
			this.uncondVars = new ArrayList<DDVariable>(new HashSet<DDVariable>(uncondVars));
			
			temp = new HashSet<DDVariable>(vars);
			temp.addAll(this.uncondVars);
			for(DDVariable v: DDContext.canonicalVariableOrdering) {
				if(temp.contains(v)) {
					this.uncondVars.add(v);
				}
			}
			
			//Conditional variables have to come first
			ArrayList<DDVariable> allVariables = new ArrayList<DDVariable>(vars);
			allVariables.addAll(conditionalVars);
			
			MutableDDElement rulesTemp = MutableDDElementBuilder.build(allVariables, closures.get(i), true);
			
			//This is only a measure given conditional variables are set
			//rulesTemp.setIsMeasure(conditionalVars, false);
			
			
			AlgebraicDD dd = new AlgebraicDD(rulesTemp);
			System.out.println(dd.ruleCollection.getIsMeasure());
			indepFns.add(dd);
			
			
		}
	}
	
	public CondProbDD(ArrayList<DDVariable> condVars, ArrayList<DDVariable> uncondVars, ArrayList<AlgebraicDD> indepFns) {
		this.condVars = new ArrayList<DDVariable>();
		
		HashSet<DDVariable> temp = new HashSet<DDVariable>(condVars);
		for(DDVariable v: DDContext.canonicalVariableOrdering) {
			if(temp.contains(v)) {
				this.condVars.add(v);
			}
		}
		this.uncondVars = new ArrayList<DDVariable>();
		
		temp = new HashSet<DDVariable>(uncondVars);
		for(DDVariable v: DDContext.canonicalVariableOrdering) {
			if(temp.contains(v)) {
				this.uncondVars.add(v);
			}
		}
		
		this.indepFns = indepFns;
	}
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		double val = 1.0f;
		val = toProbabilityFn().getValue(varSpacePoint);
		return val;
	}
	
	public ProbDD toProbabilityFn() {
		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(condVars);
		vars.addAll(uncondVars);
		ProbDD prob  = new ProbDD(new AlgebraicDD(MutableDDElementBuilder.buildProbability(vars, (Closure<Double>[])null)), vars);
		prob = prob.multiply(this);
		
		prob = prob.div(prob.sumOut(vars));
		return prob;
	}
	
	public ArrayList<DDVariable> getVariables() {
		ArrayList<DDVariable> allVars = new ArrayList<DDVariable>(condVars);
		allVars.addAll(uncondVars);
		return allVars;
	}
	
	public CondProbDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
		ArrayList<AlgebraicDD> restrictedIndepFns = new ArrayList<AlgebraicDD>();
		for(AlgebraicDD dd:indepFns) {
			restrictedIndepFns.add(dd.restrict(varSpacePoint));
		}
		
		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
		newCondVars.removeAll(varSpacePoint.keySet());
		
		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
		newUncondVars.removeAll(varSpacePoint.keySet());
		
		return new CondProbDD(newCondVars,newUncondVars, restrictedIndepFns);
	}
	
	public CondProbDD multiply(ProbDD pdd) {
		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
		for(AlgebraicDD dd:indepFns) {
			newIndepFns.add(dd.multiply(pdd));
		}
		
		ArrayList<DDVariable> resolvedCondVars = pdd.getVariables();
		resolvedCondVars.retainAll(condVars);
		
		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
		newCondVars.removeAll(resolvedCondVars);
		
		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
		newUncondVars.addAll(resolvedCondVars);
		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
		
		
	}
	
	public CondProbDD unprime() {
		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
		for(AlgebraicDD dd:indepFns) {
			newIndepFns.add(dd.unprime());
		}
		
		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>();
		for(DDVariable v:condVars) {
			newCondVars.add(v.getUnprimed());
		}
		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>();
		for(DDVariable v:uncondVars) {
			newUncondVars.add(v.getUnprimed());
		}
		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
	}
	
	public CondProbDD multiply(CondProbDD pdd) {
		if(!pdd.condVars.isEmpty()) {
			return null;
		}
		
		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
		
		for(AlgebraicDD dd:indepFns) {
			newIndepFns.add(dd.multiply(pdd));
		}
		
		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
		newCondVars.removeAll(pdd.uncondVars);
		newCondVars.addAll(pdd.condVars);
		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
		newUncondVars.addAll(pdd.uncondVars);

		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
	}
	
	public CondProbDD sumOut(ArrayList<DDVariable> vars) {
		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
		for(AlgebraicDD dd:indepFns) {
			newIndepFns.add(dd.sumOut(vars));
		}
		
		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
		newCondVars.removeAll(vars);
		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
		newUncondVars.removeAll(vars);
		
		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
	}
	
	public AlgebraicDD multiply(AlgebraicDD dd) {
		return dd.multiply(this);
	}
	
	public CondProbDD normalize() {
		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
		for(AlgebraicDD dd:indepFns) {
			newIndepFns.add( dd.div(dd.sumOut(uncondVars)) );
		}
		
		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
		
		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
	}
	
	public final ArrayList<AlgebraicDD> getComponentFunctions() {
		return new ArrayList<AlgebraicDD>(indepFns);
	}
	
	public String toString() {
		String str = "";
		str += "Conditional:" + condVars + "\n";
		str += "Non-conditonal:" + uncondVars + "\n";
		for(AlgebraicDD dd:indepFns) {
			str+=dd.toString() + "\n";
		}
		return str;
	}
	static public CondProbDD build(ArrayList<ArrayList<ArrayList<DDVariable>>> varsLst, List<Closure<Double>> closures) {
		return new CondProbDD(varsLst,closures);
	}
}
