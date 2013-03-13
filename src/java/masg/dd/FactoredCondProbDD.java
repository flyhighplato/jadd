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

public class FactoredCondProbDD {
	private ArrayList<CondProbDD> indepFns = new ArrayList<CondProbDD>();
	
	public FactoredCondProbDD(ArrayList<ArrayList<ArrayList<DDVariable>>> varsLst, int defaultScopeId, List<Closure<Double>> closures) {
		
		ArrayList<CondProbDD> indepFns = new ArrayList<CondProbDD>();
		for(int i=0;i<varsLst.size();++i) {
			CondProbDD cpdd = new CondProbDD(varsLst.get(i).get(0), varsLst.get(i).get(1), defaultScopeId, closures.get(i));
			indepFns.add(cpdd);
		}
		
		init(indepFns);
	}
	
	public FactoredCondProbDD(ArrayList<CondProbDD> indepFns) {
		init(indepFns);
	}
	
	public FactoredCondProbDD(CondProbDD fn) {
		ArrayList<CondProbDD> indepFns = new ArrayList<CondProbDD>();
		indepFns.add(fn);
		init(indepFns);
	}
	
	private void init(ArrayList<CondProbDD> indepFns) {
		this.indepFns = new ArrayList<CondProbDD>();
		this.indepFns.addAll(indepFns);
	}
	
	public final ArrayList<CondProbDD> getFunctions() {
		return new ArrayList<CondProbDD>(indepFns);
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
				
				HashSet<DDVariable> condProbVars = new HashSet<DDVariable>();
				condProbVars.addAll(dd.getConditionalVariables());
				
				boolean foundCommon = false;
				
				do {
					foundCommon = false;
					for(CondProbDD ddOther:restrictedIndepFns) {
						
						if(!ddOther.getPosteriorVariables().isEmpty() || processedDDs.contains(ddOther)) {
							continue;
						}
						
						HashSet<DDVariable> tempCondVars = new HashSet<DDVariable>(ddOther.getConditionalVariables());
						tempCondVars.retainAll(condProbVars);
						
						if(!tempCondVars.isEmpty()) {
							processedDDs.add(ddOther);
							commonDDs.add(ddOther.getFunction().getFunction());
							
							condProbVars.addAll(ddOther.getConditionalVariables());
							foundCommon = true;
						}
					}
				} while (foundCommon);
				
				dd = new CondProbDD(new ArrayList<DDVariable>(), new ArrayList<DDVariable>(condProbVars),new AlgebraicDD(DDBuilder.build(new ArrayList<DDVariable>(condProbVars), commonDDs, new MultiplicationOperation())));
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
		HashSet<DDVariable> vars = new HashSet<DDVariable>();
		for(CondProbDD ddThis:indepFns) {
			if(ddThis.getConditionalVariables().size()>0) {
				return null;
			}
			vars.addAll(ddThis.getFunction().getVariables());
			pertinentFns.add(ddThis.getFunction().getFunction());
		}
		AlgebraicDD dd = new AlgebraicDD(DDBuilder.build(new ArrayList<DDVariable>(vars), pertinentFns, new MultiplicationOperation()));
		
		return new ProbDD(dd);
	}
	
	public AlgebraicDD multiply(AlgebraicDD dd) {
		
		ArrayList<AlgebraicDD> pertinentFns = new ArrayList<AlgebraicDD>();
		
		for(CondProbDD ddThis:indepFns) {
			HashSet<DDVariable> intersection = new HashSet<DDVariable>(ddThis.getPosteriorVariables());
			intersection.retainAll(dd.getVariables());
			
			if(intersection.size()>0) {
				ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(ddThis.getPosteriorVariables());
				sumOutVars.removeAll(intersection);
				
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
		
		HashSet<DDVariable> conditionalVariables1 = new HashSet<DDVariable>();
		HashSet<DDVariable> conditionalVariables2 = new HashSet<DDVariable>();
		HashSet<DDVariable> posteriorVariables1 = new HashSet<DDVariable>();
		HashSet<DDVariable> posteriorVariables2 = new HashSet<DDVariable>();
		
		for(CondProbDD ddThis:indepFns) {
			conditionalVariables1.addAll(ddThis.getConditionalVariables());
			posteriorVariables1.addAll(ddThis.getPosteriorVariables());
		}
		
		for(CondProbDD ddThis:cpdd.indepFns) {
			conditionalVariables2.addAll(ddThis.getConditionalVariables());
			posteriorVariables2.addAll(ddThis.getPosteriorVariables());
		}
		
		HashSet<DDVariable> satisfied1 = new HashSet<DDVariable>(conditionalVariables1);
		satisfied1.retainAll(posteriorVariables2);
		
		HashSet<DDVariable> satisfied2 = new HashSet<DDVariable>(conditionalVariables2);
		satisfied2.retainAll(posteriorVariables1);
		
		//Variables which would not be "transitioned" to the result
		HashSet<DDVariable> danglingVariables = new HashSet<DDVariable>();
				
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
				danglingVariables = new HashSet<DDVariable>(posteriorVariables1);
				danglingVariables.removeAll(satisfied2);
				
			}
			else {
				danglingVariables = new HashSet<DDVariable>(posteriorVariables2);
				danglingVariables.removeAll(satisfied1);
			}
		}
		else {
			danglingVariables = new HashSet<DDVariable>(posteriorVariables1);
			danglingVariables.removeAll(posteriorVariables2);
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
				
				
				HashSet<DDVariable> satisfiedCondVars = new HashSet<DDVariable>(ddThis.getConditionalVariables());
				satisfiedCondVars.retainAll(ddOther.getPosteriorVariables());
				
				if(!satisfiedCondVars.isEmpty()) {
					
					HashSet<DDVariable> unusedPosteriorVariables = new HashSet<DDVariable>(ddOther.getPosteriorVariables());
					unusedPosteriorVariables.removeAll(satisfiedCondVars);
					
					childToAncestors.get(ddThis).add(ddOther.sumOut(new ArrayList<DDVariable>(unusedPosteriorVariables)));
				}
				else if(ddThis.getConditionalVariables().isEmpty() && ddOther.getConditionalVariables().isEmpty()) {
					
					HashSet<DDVariable> satisfiedPostVars = new HashSet<DDVariable>(ddOther.getPosteriorVariables());
					satisfiedPostVars.retainAll(ddThis.getPosteriorVariables());
					
					if(!satisfiedPostVars.isEmpty()) {
						HashSet<DDVariable> unusedPosteriorVariables = new HashSet<DDVariable>(ddOther.getPosteriorVariables());
						unusedPosteriorVariables.removeAll(ddThis.getPosteriorVariables());
					
						childToAncestors.get(ddThis).add(ddOther.sumOut(new ArrayList<DDVariable>(unusedPosteriorVariables)));
					}
					
				}
			}
		}
		
		ArrayList<CondProbDD> newIndepFns = new ArrayList<CondProbDD>();
		
		
		for(Entry<CondProbDD, ArrayList<CondProbDD>> e: childToAncestors.entrySet()) {
			CondProbDD temp = e.getKey();
			for(CondProbDD other:e.getValue()) {
				temp = temp.multiply(other);
			}
			
			newIndepFns.add(temp);
		}
		
		if(!danglingVariables.isEmpty()) {
			for(CondProbDD ddOther:indepFnsConditional) {
				HashSet<DDVariable> temp = new HashSet<DDVariable>(ddOther.getPosteriorVariables());
				temp.retainAll(danglingVariables);
				
				if(!temp.isEmpty()) {
					HashSet<DDVariable> temp2 = new HashSet<DDVariable>(ddOther.getPosteriorVariables());
					temp2.removeAll(temp);
					CondProbDD newDD = ddOther.sumOut(new ArrayList<DDVariable>(temp2));
					newIndepFns.add(newDD);
				}
			}
		}
		
		return new FactoredCondProbDD(newIndepFns);
	}
	
	
	public FactoredCondProbDD sumOut(ArrayList<DDVariable> vars) {
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
