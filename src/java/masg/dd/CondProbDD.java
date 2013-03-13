package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import masg.dd.variables.DDVariable;

public class CondProbDD {
	private ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
	private ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>();
	
	private AlgebraicDD fn;
	
	public CondProbDD(ArrayList<DDVariable> condVars, ArrayList<DDVariable> uncondVars, int defaultScopeId, Closure<Double> c) {
		this.condVars.addAll(new HashSet<DDVariable>(condVars));
		this.uncondVars.addAll(new HashSet<DDVariable>(uncondVars));
		
		ArrayList<DDVariable> allVariables = new ArrayList<DDVariable>(this.uncondVars);
		allVariables.addAll(this.condVars);
		
		fn = new AlgebraicDD(allVariables, defaultScopeId, c, true);
	}
	
	public CondProbDD(ArrayList<DDVariable> condVars, ArrayList<DDVariable> uncondVars, AlgebraicDD fn) {
		
		this.condVars.addAll(new HashSet<DDVariable>(condVars));
		this.uncondVars.addAll(new HashSet<DDVariable>(uncondVars));
		
		this.fn = fn;
	}
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		return fn.getValue(varSpacePoint);
	}
	
	public final ArrayList<DDVariable> getConditionalVariables() {
		return new ArrayList<DDVariable>(condVars);
	}
	
	public final ArrayList<DDVariable> getPosteriorVariables() {
		return new ArrayList<DDVariable>(uncondVars);
	}
	
	public final AlgebraicDD getFunction() {
		return fn;
	}
	
	public CondProbDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>(this.condVars);
		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>(this.uncondVars);
		
		condVars.removeAll(varSpacePoint.keySet());
		uncondVars.removeAll(varSpacePoint.keySet());
		
		return new CondProbDD(condVars,uncondVars,fn.restrict(varSpacePoint));
	}
	
	public AlgebraicDD multiply(AlgebraicDD dd) {
		return fn.multiply(dd);
	}
	
	public CondProbDD multiply(CondProbDD cpddOther) {
		return CondProbDD.multiply(this,cpddOther);
	}
	
	private static CondProbDD multiply(CondProbDD cpdd1, CondProbDD cpdd2) {
		
		HashSet<DDVariable> newCondVars = new HashSet<DDVariable>();
		HashSet<DDVariable> newUncondVars = new HashSet<DDVariable>();
		HashSet<DDVariable> elimVars = new HashSet<DDVariable>();
		
		HashSet<DDVariable> solvedConditionalVariables1 = new HashSet<DDVariable>(cpdd1.getPosteriorVariables());
		solvedConditionalVariables1.retainAll(cpdd2.getConditionalVariables());
		
		HashSet<DDVariable> solvedConditionalVariables2 = new HashSet<DDVariable>(cpdd2.getPosteriorVariables());
		solvedConditionalVariables2.retainAll(cpdd1.getConditionalVariables());
		
		
		boolean canMultiply = false;
		if(!solvedConditionalVariables1.isEmpty()) {
			canMultiply = true;
			
			newCondVars.addAll(cpdd1.getConditionalVariables());
			newCondVars.addAll(cpdd2.getConditionalVariables());
			newCondVars.removeAll(solvedConditionalVariables1);
			
			
			newUncondVars.addAll(cpdd1.getPosteriorVariables());
			newUncondVars.addAll(cpdd2.getPosteriorVariables());
			newUncondVars.removeAll(solvedConditionalVariables1);
			
			elimVars.addAll(solvedConditionalVariables1);
			
		}
		else if(!solvedConditionalVariables2.isEmpty()) {
			canMultiply = true;
			
			newCondVars.addAll(cpdd1.getConditionalVariables());
			newCondVars.addAll(cpdd2.getConditionalVariables());
			newCondVars.removeAll(solvedConditionalVariables2);
			
			
			newUncondVars.addAll(cpdd1.getPosteriorVariables());
			newUncondVars.addAll(cpdd2.getPosteriorVariables());
			newUncondVars.removeAll(solvedConditionalVariables2);
			
			elimVars.addAll(solvedConditionalVariables2);
		}
		//Assumption of conditional independence here
		else {
			
			HashSet<DDVariable> condVars1 = new HashSet<DDVariable>(cpdd1.getConditionalVariables());
			HashSet<DDVariable> condVars2 = new HashSet<DDVariable>(cpdd2.getConditionalVariables());
			
			HashSet<DDVariable> posteriorVariablesIntersection = new HashSet<DDVariable>(cpdd1.getPosteriorVariables());
			posteriorVariablesIntersection.retainAll(cpdd2.getPosteriorVariables());
			
			
			if(posteriorVariablesIntersection.isEmpty()) {
				canMultiply = true;
				
				newCondVars.addAll(cpdd1.getConditionalVariables());
				newCondVars.addAll(cpdd2.getConditionalVariables());
				newUncondVars.addAll(cpdd1.getPosteriorVariables());
				newUncondVars.addAll(cpdd2.getPosteriorVariables());
			}
			else if(condVars1.equals(condVars2)) {
				canMultiply = true;
				
				newCondVars.addAll(cpdd1.getConditionalVariables());
				newUncondVars.addAll(cpdd1.getPosteriorVariables());
				newUncondVars.addAll(cpdd2.getPosteriorVariables());
			}
		}
		
		if(canMultiply) {
			return new CondProbDD(new ArrayList<DDVariable>(newCondVars),new ArrayList<DDVariable>(newUncondVars),cpdd1.getFunction().multiply(cpdd2.getFunction()).sumOut(new ArrayList<DDVariable>(elimVars)));
		}
		
		return null;
	}
	
	public CondProbDD sumOut(ArrayList<DDVariable> vars) {
		
		ArrayList<DDVariable> normVars = new ArrayList<DDVariable>();
		
		for(DDVariable v: this.condVars) {
			if(vars.contains(v)) {
				normVars.add(v);
				break;
			}
		}
		
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>(this.condVars);
		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>(this.uncondVars);
		
		uncondVars.removeAll(vars);
		
		AlgebraicDD resultDD = fn.sumOut(vars);
		
		if(normVars.size()>0) {
			resultDD = resultDD.div(fn.sumOut(normVars));
		}
		
		return new CondProbDD(condVars,uncondVars,resultDD);
	}
	
	public CondProbDD normalize() {
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>(this.condVars);
		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>(this.uncondVars);
		
		AlgebraicDD resultDD = fn.div(fn.sumOut(this.uncondVars));
		return new CondProbDD(condVars,uncondVars,resultDD);
	}
	public CondProbDD unprime() {
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.condVars) {
			condVars.add(v.getUnprimed());
		}
		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.uncondVars) {
			uncondVars.add(v.getUnprimed());
		}
		
		return new CondProbDD(condVars,uncondVars,fn.unprime());
		
	}
	
	public CondProbDD prime() {
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.condVars) {
			condVars.add(v.getPrimed());
		}
		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.uncondVars) {
			uncondVars.add(v.getPrimed());
		}
		
		return new CondProbDD(condVars,uncondVars,fn.prime());
		
	}
	
	public String toString() {
		String str = this.condVars + "\n";
		str += this.uncondVars + "\n";
		return str + fn.toString();
	}
}
