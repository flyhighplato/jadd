package masg.dd

import groovy.lang.Closure;
import masg.dd.context.DDContext;
import masg.dd.rules.DecisionRule;
import masg.dd.rules.DecisionRuleCollection;
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace

class AlgebraicDDBuilder {

	static AlgebraicDD build(Map<String,Integer> vars, Closure<Double> c) {
		build(vars.collect{k,v -> new DDVariable(k,v)},c)
	}
	
	static AlgebraicDD build(List<DDVariable> vars, Closure<Double> c) {
		
		DDContext context = makeContext(vars)
		
		AlgebraicDD resDD = initializeDD(context)

		populateDD(resDD,c)
		
		return resDD		
	}
	
	static AlgebraicDD build(List<DDVariable> vars, double val) {
		
		DDContext context = makeContext(vars)
		
		AlgebraicDD resDD = initializeDD(context)

		populateDD(resDD,val)
		
		return resDD
	}
	
	protected static DDContext makeContext(List<DDVariable> vars) {
		DDContext context = new DDContext();
		context.getVariableSpace().addVariables(vars)
		return context;
	}
	
	protected static AlgebraicDD initializeDD(DDContext context) {
		return new AlgebraicDD(context);
	}
	
	protected static void populateDD(AlgebraicDD dd, double val) {
		DecisionRule r = new DecisionRule( dd.getContext().getVariableSpace().getBitCount(),val)
		dd.addRule(r)
	}
	
	protected static void populateDD(AlgebraicDD dd, Closure c) {
		
		int numRules=0;
		DDVariableSpace varSpace = dd.getContext().getVariableSpace();
		
		
		DecisionRuleCollection rules = new DecisionRuleCollection(varSpace.getBitCount())
		
		println "Current number of rules:" + dd.getRules().size();
		
		varSpace.each{ HashMap<DDVariable,Integer> varSpacePoint ->
			double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
			
			DecisionRule r = varSpace.generateRule(varSpacePoint,val)
			
			numRules++;
			rules << r
			
			if(rules.size()>100) {
				rules.compress()
			}
			
			if(rules.size()>100) {
				dd.addRules(new ArrayList(rules))
				rules = new DecisionRuleCollection(varSpace.getBitCount())
				println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
			}
			
		}
		
		dd.addRules(new ArrayList(rules))
		
		dd.compress()
		
		println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
		
		/*println "Adding negative space..."
		
		
		for(int varIx = varSpace.getVariables().size()-1;varIx>=0;varIx--) {
			rules = new DecisionRuleCollection(varSpace.getBitCount())
			HashMap<DDVariable,Range> unusedValues = new HashMap<DDVariable,Range>();
			
			DDVariable currVar = varSpace.getVariables().get(varIx)
			
			int totalValuesPossible = (int)Math.pow(2.0f, currVar.getBitCount())-1
			int firstUnusedValue = currVar.getValueCount()
			unusedValues[currVar] = (firstUnusedValue..totalValuesPossible)
			
			println "$currVar has unused range ${unusedValues[currVar]}"
			
			varSpace.each{ HashMap<DDVariable,Integer> varSpacePoint ->
				double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				
				unusedValues.each{ DDVariable var, Range valRange ->
					HashMap<DDVariable,Integer> varSpacePointCopy = varSpacePoint.clone()
					
					for(int laterVarIx=varIx;laterVarIx<varSpace.getVariables().size();laterVarIx++) {
						varSpacePointCopy.remove(varSpace.getVariables().get(laterVarIx))
					}
					valRange.each{ int varVal ->
						
						varSpacePointCopy[var] = varVal;
						DecisionRule r = varSpace.generateRule(varSpacePointCopy,val)
						numRules++;
						rules << r
					}
				}
				
				if(rules.size()>100) {
					rules.compress()
				}
				
				if(rules.size()>100) {
					dd.addRules(new ArrayList(rules))
					rules = new DecisionRuleCollection(varSpace.getBitCount())
					println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
				}
				
			}
			
			dd.compress();
			
			println "Removing unused negative space..."
			varSpace.each{ HashMap<DDVariable,Integer> varSpacePoint ->
				double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				
				unusedValues.each{ DDVariable var, Range valRange ->
					HashMap<DDVariable,Integer> varSpacePointCopy = varSpacePoint.clone()
					
					for(int laterVarIx=varIx;laterVarIx<varSpace.getVariables().size();laterVarIx++) {
						varSpacePointCopy.remove(varSpace.getVariables().get(laterVarIx))
					}
					valRange.each{ int varVal ->
						
						varSpacePointCopy[var] = varVal;
						DecisionRule r = varSpace.generateRule(varSpacePointCopy,val)
						if(dd.getRules().remove(r)) {
							numRules--
						}
					}
				}
			}
			
			dd.addRules(new ArrayList(rules))
			dd.compress();
			println "Current number of rules:" + dd.getRules().size() + "/" + numRules;
		}*/
		
	}
	
}
