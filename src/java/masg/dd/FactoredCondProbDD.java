package masg.dd;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import masg.dd.operations.MultiplicationOperation;
import masg.dd.representation.DDElement;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;

public class FactoredCondProbDD {
	private ArrayList<CondProbDD> indepFns = new ArrayList<CondProbDD>();
	
	public FactoredCondProbDD(ArrayList<ArrayList<DDVariableSpace>> varsLst, List<Closure<Double>> closures) {
		
		ArrayList<CondProbDD> indepFns = new ArrayList<CondProbDD>();
		for(int i=0;i<varsLst.size();++i) {
			CondProbDD cpdd = new CondProbDD(varsLst.get(i).get(0), varsLst.get(i).get(1), closures.get(i));
			indepFns.add(cpdd);
		}
		
		init(indepFns);
	}
	
	public FactoredCondProbDD(ArrayList<CondProbDD> indepFns) {
		init(indepFns);
	}
	
	private void init(ArrayList<CondProbDD> indepFns) {
		this.indepFns = new ArrayList<CondProbDD>();
		this.indepFns.addAll(indepFns);
	}
	
	
	
	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
		double val = 1.0f;
		for(CondProbDD dd:indepFns) {
			val*=dd.getValue(varSpacePoint);
		}
		return val;
	}
	
	
	public ArrayList<CondProbDD> reconcile(ArrayList<CondProbDD> restrictedIndepFns) {
		ArrayList<CondProbDD> reconciledIndepFns = new ArrayList<CondProbDD>();
		
		HashSet<CondProbDD> processedDDs = new HashSet<CondProbDD>();
		
		for(CondProbDD dd:restrictedIndepFns) {
			if(processedDDs.contains(dd)) {
				continue;
			}
			
			processedDDs.add(dd);
			
			if(dd.getPosteriorVariables().isEmpty() && !dd.getConditionalVariables().isEmpty()) {
				ArrayList<DDElement> commonDDs = new ArrayList<DDElement>();
				commonDDs.add(dd.getFunction().getFunction());
				
				DDVariableSpace condProbVars = dd.getConditionalVariables();
				
				boolean foundCommon = false;
				
				do {
					foundCommon = false;
					for(CondProbDD ddOther:restrictedIndepFns) {
						
						if(!ddOther.getPosteriorVariables().isEmpty() || processedDDs.contains(ddOther)) {
							continue;
						}
						
						DDVariableSpace tempCondVars = ddOther.getConditionalVariables().intersect(condProbVars);
						
						if(!tempCondVars.isEmpty()) {
							processedDDs.add(ddOther);
							commonDDs.add(ddOther.getFunction().getFunction());
							
							condProbVars = condProbVars.union(ddOther.getConditionalVariables());
							foundCommon = true;
						}
					}
				} while (foundCommon);
				
				dd = new CondProbDD(new DDVariableSpace(), condProbVars,new AlgebraicDD(DDBuilder.build(condProbVars, commonDDs, new MultiplicationOperation())));
				dd = dd.normalize();
			}

			reconciledIndepFns.add(dd);
			
		}

		return reconciledIndepFns;
		
	}
	
	public FactoredCondProbDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
		ArrayList<CondProbDD> restrictedIndepFns = new ArrayList<CondProbDD>();
		
		boolean needsReconcile = false;
		for(CondProbDD dd:indepFns) {
			dd = dd.restrict(varSpacePoint);
			if(dd.getPosteriorVariables().isEmpty()) {
				needsReconcile = true;
			}
			restrictedIndepFns.add(dd); 
		}
		
		if(needsReconcile) {
			restrictedIndepFns = reconcile(restrictedIndepFns);
		}
		
		
		
		return new FactoredCondProbDD(restrictedIndepFns);
	}
	
	public FactoredCondProbDD unprime() {
		ArrayList<CondProbDD> newIndepFns = new ArrayList<CondProbDD>();
		for(CondProbDD dd:indepFns) {
			newIndepFns.add(dd.unprime());
		}
		
		return new FactoredCondProbDD(newIndepFns);
	}
	
	public ProbDD toProbabilityDD() {
		ArrayList<DDElement> pertinentFns = new ArrayList<DDElement>();
		DDVariableSpace vars = new DDVariableSpace();
		for(CondProbDD ddThis:indepFns) {
			if(!ddThis.getConditionalVariables().isEmpty()) {
				return null;
			}
			vars = vars.union(ddThis.getFunction().getVariables());
			pertinentFns.add(ddThis.getFunction().getFunction());
		}
		AlgebraicDD dd = new AlgebraicDD(DDBuilder.build(vars, pertinentFns, new MultiplicationOperation()));
		
		return new ProbDD(dd);
	}
	
	public AlgebraicDD multiply(AlgebraicDD dd) {
		
		ArrayList<AlgebraicDD> pertinentFns = new ArrayList<AlgebraicDD>();
		
		for(CondProbDD ddThis:indepFns) {
			DDVariableSpace intersection = ddThis.getPosteriorVariables().intersect(dd.getVariables());
			
			if(!intersection.isEmpty()) {
				DDVariableSpace sumOutVars = ddThis.getPosteriorVariables().exclude(intersection);
				
				ddThis = ddThis.sumOut(sumOutVars);
				
				pertinentFns.add(ddThis.getFunction());
			}
		}
		
		dd = dd.oper(new MultiplicationOperation(), pertinentFns);
		return dd;
	}
	
	public FactoredCondProbDD multiply(FactoredCondProbDD cpdd) {
		
		ArrayList<CondProbDD> indepFnsConditional = null;
		ArrayList<CondProbDD> indepFnsPosterior = null;
		
		DDVariableSpace conditionalVariables1 = new DDVariableSpace();
		DDVariableSpace conditionalVariables2 = new DDVariableSpace();
		DDVariableSpace posteriorVariables1 = new DDVariableSpace();
		DDVariableSpace posteriorVariables2 = new DDVariableSpace();
		
		for(CondProbDD ddThis:indepFns) {
			conditionalVariables1 = conditionalVariables1.union(ddThis.getConditionalVariables());
			posteriorVariables1 = posteriorVariables1.union(ddThis.getPosteriorVariables());
		}
		
		for(CondProbDD ddThis:cpdd.indepFns) {
			conditionalVariables2 = conditionalVariables2.union(ddThis.getConditionalVariables());
			posteriorVariables2 = posteriorVariables2.union(ddThis.getPosteriorVariables());
		}
		
		DDVariableSpace satisfied1 =  conditionalVariables1.intersect(posteriorVariables2);
		
		DDVariableSpace satisfied2 = conditionalVariables2.intersect(posteriorVariables1);
		
		//Variables which would not be "transitioned" to the result
		DDVariableSpace danglingVariables = new DDVariableSpace();
				
		//Only conditional probabilities complicate things...
		if(!conditionalVariables1.isEmpty() || !conditionalVariables2.isEmpty()) {
			if(satisfied1.isEmpty() && satisfied2.isEmpty()) {
				return null;
			}
			
			//This could work, but return null for now
			if(!satisfied1.isEmpty() && !satisfied2.isEmpty()) {
				return null;
			}
			
			if(satisfied1.isEmpty()) {
				danglingVariables = posteriorVariables1.exclude(satisfied2);
				
			}
			else {
				danglingVariables = posteriorVariables2.exclude(satisfied1);
			}
		}
		else {
			danglingVariables = posteriorVariables1.exclude(posteriorVariables2);
		}
		
		if(satisfied1.isEmpty()) {
			indepFnsConditional = indepFns;
			indepFnsPosterior = cpdd.indepFns;
		}
		else {
			indepFnsConditional = cpdd.indepFns;
			indepFnsPosterior = indepFns;
		}
		
		
		HashMap<CondProbDD, ArrayList<CondProbDD>> childToAncestors = new HashMap<CondProbDD, ArrayList<CondProbDD>>();
		
		for(CondProbDD ddThis:indepFnsPosterior) {
			
			childToAncestors.put(ddThis, new ArrayList<CondProbDD>());
			
			for(CondProbDD ddOther:indepFnsConditional) {
				
				
				DDVariableSpace satisfiedCondVars = ddThis.getConditionalVariables().intersect(ddOther.getPosteriorVariables());
				
				if(!satisfiedCondVars.isEmpty()) {
					
					DDVariableSpace unusedPosteriorVariables = ddOther.getPosteriorVariables().exclude(satisfiedCondVars);
					
					childToAncestors.get(ddThis).add(ddOther.sumOut(unusedPosteriorVariables));
				}
				else if(ddThis.getConditionalVariables().isEmpty() && ddOther.getConditionalVariables().isEmpty()) {
					
					DDVariableSpace satisfiedPostVars = ddOther.getPosteriorVariables().intersect(ddThis.getPosteriorVariables());
					
					if(!satisfiedPostVars.isEmpty()) {
						DDVariableSpace unusedPosteriorVariables = ddOther.getPosteriorVariables().exclude(ddThis.getPosteriorVariables());
					
						childToAncestors.get(ddThis).add(ddOther.sumOut(unusedPosteriorVariables));
					}
					
				}
			}
		}
		
		ArrayList<CondProbDD> newIndepFns = new ArrayList<CondProbDD>();
		
		
		for(Entry<CondProbDD, ArrayList<CondProbDD>> e: childToAncestors.entrySet()) {
			CondProbDD temp = e.getKey();
			
			if(temp==null) {
				System.out.println("Test");
			}
			
			for(CondProbDD other:e.getValue()) {
				if(temp==null) {
					System.out.println("Test");
				}
				
				CondProbDD temp2 = temp.multiply(other);
				
				if(temp2==null) {
					System.out.println("Test");
					temp2 = temp.multiply(other);
				}
				
				temp = temp2;
				
			}
			
			
			newIndepFns.add(temp);
		}
		
		if(!danglingVariables.isEmpty()) {
			for(CondProbDD ddOther:indepFnsConditional) {
				DDVariableSpace temp = ddOther.getPosteriorVariables().intersect(danglingVariables);
				
				if(!temp.isEmpty()) {
					DDVariableSpace temp2 = ddOther.getPosteriorVariables().exclude(temp);
					CondProbDD newDD = ddOther.sumOut(temp2);
					newIndepFns.add(newDD);
				}
			}
		}
		
		return new FactoredCondProbDD(newIndepFns);
	}
	
	
	public FactoredCondProbDD sumOut(DDVariableSpace vars) {
		ArrayList<CondProbDD> newIndepFns = new ArrayList<CondProbDD>();
		
		boolean needsReconcile = false;
		for(CondProbDD dd:indepFns) {
			dd = dd.sumOut(vars);
			if(dd.getPosteriorVariables().isEmpty()) {
				needsReconcile = true;
			}
			newIndepFns.add(dd); 
		}
		
		if(needsReconcile) {
			newIndepFns = reconcile(newIndepFns);
		}
		
		return new FactoredCondProbDD(newIndepFns);
	}
	
	public FactoredCondProbDD normalize() {
		ArrayList<CondProbDD> newIndepFns = new ArrayList<CondProbDD>();
		for(CondProbDD dd:indepFns) {
			newIndepFns.add(dd.normalize());
		}
		return new FactoredCondProbDD(newIndepFns);
	}
	
	public String toString() {
		String str = "";
		for(CondProbDD dd:indepFns) {
			str+=dd.toString() + "\n";
		}
		return str;
	}
}
