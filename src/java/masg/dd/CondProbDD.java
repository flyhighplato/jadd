package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;

import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public class CondProbDD {
	private DDVariableSpace condVars;
	private DDVariableSpace uncondVars;
	
	private AlgebraicDD fn;
	
	public CondProbDD(DDVariableSpace condVars, DDVariableSpace uncondVars, Closure<Double> c) {
		this.condVars = new DDVariableSpace(condVars);
		this.uncondVars = new DDVariableSpace(uncondVars);
		
		fn = new AlgebraicDD(this.uncondVars.union(this.condVars), c, true);
	}
	
	public CondProbDD(DDVariableSpace condVars, DDVariableSpace uncondVars, AlgebraicDD fn) {
		this.condVars = new DDVariableSpace(condVars);
		this.uncondVars = new DDVariableSpace(uncondVars);
		
		this.fn = fn;
	}
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		return fn.getValue(varSpacePoint);
	}
	
	public final DDVariableSpace getConditionalVariables() {
		return condVars;
	}
	
	public final DDVariableSpace getPosteriorVariables() {
		return uncondVars;
	}
	
	public final AlgebraicDD getFunction() {
		return fn;
	}
	
	public CondProbDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
		DDVariableSpace condVars = this.condVars.exclude(varSpacePoint.keySet());
		DDVariableSpace uncondVars = this.uncondVars.exclude(varSpacePoint.keySet());
		
		return new CondProbDD(condVars,uncondVars,fn.restrict(varSpacePoint));
	}
	
	public AlgebraicDD multiply(AlgebraicDD dd) {
		return fn.multiply(dd);
	}
	
	public CondProbDD multiply(CondProbDD cpddOther) {
		return CondProbDD.multiply(this,cpddOther);
	}
	
	private static CondProbDD multiply(CondProbDD cpdd1, CondProbDD cpdd2) {
		
		DDVariableSpace newCondVars = null;
		DDVariableSpace newUncondVars = null;
		DDVariableSpace elimVars = new DDVariableSpace();
		
		DDVariableSpace solvedConditionalVariables1 = cpdd1.getPosteriorVariables().intersect(cpdd2.getConditionalVariables());
		DDVariableSpace solvedConditionalVariables2 = cpdd2.getPosteriorVariables().intersect(cpdd1.getConditionalVariables());
		
		boolean canMultiply = false;
		if(!solvedConditionalVariables1.isEmpty()) {
			canMultiply = true;
			
			newCondVars = new DDVariableSpace(cpdd1.getConditionalVariables());
			newCondVars = newCondVars.union(cpdd2.getConditionalVariables());
			newCondVars = newCondVars.exclude(solvedConditionalVariables1);
			
			
			newUncondVars = new DDVariableSpace(cpdd1.getPosteriorVariables());
			newUncondVars = newUncondVars.union(cpdd2.getPosteriorVariables());
			newUncondVars = newUncondVars.exclude(solvedConditionalVariables1);
			
			elimVars = new DDVariableSpace(solvedConditionalVariables1);
			
		}
		else if(!solvedConditionalVariables2.isEmpty()) {
			canMultiply = true;
			
			newCondVars = new DDVariableSpace(cpdd1.getConditionalVariables());
			newCondVars = newCondVars.union(cpdd2.getConditionalVariables());
			newCondVars = newCondVars.exclude(solvedConditionalVariables2);
			
			
			newUncondVars = new DDVariableSpace(cpdd1.getPosteriorVariables());
			newUncondVars = newUncondVars.union(cpdd2.getPosteriorVariables());
			newUncondVars = newUncondVars.exclude(solvedConditionalVariables2);
			
			elimVars = new DDVariableSpace(solvedConditionalVariables2);
		}
		//Assumption of conditional independence here
		else {
			
			DDVariableSpace condVars1 = cpdd1.getConditionalVariables();
			DDVariableSpace condVars2 = cpdd2.getConditionalVariables();
			
			DDVariableSpace posteriorVariablesIntersection = cpdd1.getPosteriorVariables().intersect(cpdd2.getPosteriorVariables());
			
			
			if(posteriorVariablesIntersection.isEmpty() || condVars1.isEmpty() || condVars2.isEmpty()) {
				canMultiply = true;
				
				newCondVars = new DDVariableSpace(cpdd1.getConditionalVariables());
				newCondVars = newCondVars.union(cpdd2.getConditionalVariables());
				
				newUncondVars = new DDVariableSpace(cpdd1.getPosteriorVariables());
				newUncondVars = newUncondVars.union(cpdd2.getPosteriorVariables());

			}
			else if(condVars1.equals(condVars2)) {
				canMultiply = true;
				
				newCondVars = new DDVariableSpace(cpdd1.getConditionalVariables());
				
				newUncondVars = new DDVariableSpace(cpdd1.getPosteriorVariables());
				newUncondVars = newUncondVars.union(cpdd2.getPosteriorVariables());
			}
		}
		
		if(canMultiply) {
			return new CondProbDD(newCondVars,newUncondVars,cpdd1.getFunction().multiply(cpdd2.getFunction()).sumOut(elimVars));
		}
		
		return null;
	}
	
	public CondProbDD sumOut(DDVariableSpace vars) {
		
		ArrayList<DDVariable> normVars = new ArrayList<DDVariable>();
		
		for(DDVariable v: this.condVars.getVariables()) {
			if(vars.contains(v)) {
				normVars.add(v);
				break;
			}
		}
		
		DDVariableSpace condVars = this.condVars;
		DDVariableSpace uncondVars = this.uncondVars.exclude(vars);
		
		AlgebraicDD resultDD = fn.sumOut(vars);
		
		if(normVars.size()>0) {
			resultDD = resultDD.div(fn.sumOut(new DDVariableSpace(normVars)));
		}
		
		return new CondProbDD(condVars,uncondVars,resultDD);
	}
	
	public CondProbDD normalize() {
		DDVariableSpace condVars = this.condVars;
		DDVariableSpace uncondVars = this.uncondVars;
		
		AlgebraicDD resultDD = fn.div(fn.sumOut(this.uncondVars));
		return new CondProbDD(condVars,uncondVars,resultDD);
	}
	
	public CondProbDD unprime() {
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.condVars.getVariables()) {
			condVars.add(v.getUnprimed());
		}
		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.uncondVars.getVariables()) {
			uncondVars.add(v.getUnprimed());
		}
		
		return new CondProbDD(new DDVariableSpace(condVars),new DDVariableSpace(uncondVars),fn.unprime());
		
	}
	
	public CondProbDD prime() {
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.condVars.getVariables()) {
			condVars.add(v.getPrimed());
		}
		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>();
		for(DDVariable v:this.uncondVars.getVariables()) {
			uncondVars.add(v.getPrimed());
		}
		
		return new CondProbDD(new DDVariableSpace(condVars),new DDVariableSpace(uncondVars),fn.unprime());
		
		
	}
	
	public String toString() {
		String str = this.condVars + "\n";
		str += this.uncondVars + "\n";
		return str + fn.toString();
	}
}
