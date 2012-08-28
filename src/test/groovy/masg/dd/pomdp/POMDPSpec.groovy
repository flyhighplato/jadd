package masg.dd.pomdp

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.List;

import masg.dd.AlgebraicDecisionDiagram;
import masg.dd.DecisionDiagramContext
import masg.dd.DecisionRule
import masg.dd.cpt.CondProbADD
import masg.dd.function.DDTransitionFunction
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace
import masg.problem.tag.TagProblem

import spock.lang.Specification
import spock.lang.Shared

class POMDPSpec extends Specification {
	@Shared
	TagProblem problem = new TagProblem()
	
	@Shared
	POMDP p
	
	static String fileName = "tag-problem-pomdp.txt"
	
	def setupSpec() {
		
		File f = new File(fileName)
		
		if(!f.exists()) {
			p = problem.getPOMDP()
		}
		else {
			f.withReader { Reader rIn ->
				p = POMDP.readIn(rIn)
			}
		}
	}
	
	def "POMDP initial belief is correct"() {
		when:
			
			DDVariableSpace currVarSpace = p.initBeliefDD.getContext().getVariableSpace()
		then:
			
			currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
				double valClosure = problem.initBelief(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				double valDD = p.initBeliefDD.getValue(currVarSpace.generateRule(varSpacePoint,0.0f))
				
				assert valClosure == valDD
				
			}
	}
	
	def "POMDP reward function is correct"() {
		when:
		
			DDVariableSpace currVarSpace = p.rewFnDD.getContext().getVariableSpace()
		then:
			
			currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
			double valClosure = problem.rewFn(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
			double valDD = p.rewFnDD.getValue(currVarSpace.generateRule(varSpacePoint,0.0f))
			
			assert valClosure == valDD
			
		}
	}
	
	
	def "POMDP transition function is correct"() {
		when:
			
			DDVariableSpace currVarSpace = new DDVariableSpace();
			
			
		then:
		
			p.getTransFns().getDDs().each{ AlgebraicDecisionDiagram dd ->
				currVarSpace = new DDVariableSpace();
				currVarSpace.addVariables(dd.getContext().getVariableSpace().getVariables())
				
				println "Testing varspace: ${currVarSpace.getVariables()}"
				currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
					double valClosure = 1.0f
					
					
					problem.transFns.each{Closure<Double> c ->
						try{
							valClosure *= c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
						} catch (Exception e) {
							println "Transition function"
							println varSpacePoint.collectEntries{k,v -> [k.toString(),v]}
							println e
						}
					}
					
					double valDD = p.getTransFns().getValue(varSpacePoint)
					
					if(valClosure != valDD) {
						println "Invalid value for $varSpacePoint"
					}
					assert valClosure == valDD
					
				}
			}
	}
	
	def "POMDP observation function is correct"() {
		when:
			
			DDVariableSpace currVarSpace = new DDVariableSpace();
			
			
		then:
		
			p.getObsFns().getDDs().each{ AlgebraicDecisionDiagram dd ->
				currVarSpace = new DDVariableSpace();
				currVarSpace.addVariables(dd.getContext().getVariableSpace().getVariables())
				
				println "Testing varspace: ${currVarSpace.getVariables()}"
				currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
					double valClosure = 1.0f
					
					
					problem.obsFns.each{Closure<Double> c ->
						try{
							valClosure *= c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
						} catch (Exception e) {
							
						}
					}
					
					double valDD = p.getObsFns().getValue(varSpacePoint)
					
					if(valClosure != valDD) {
						println "Invalid value for $varSpacePoint"
					}
					assert valClosure == valDD
					
				}
			}
	}
	
	def "POMDP can be saved to disk"() {
		when:
			File f = new File(fileName)
		then:
			f.withWriter{ Writer out ->
				p.writeOut(out)
			}
		
	}
	
	def "belief updating works"() {
		when:
			DDVariable act = p.actions[0]
			DDVariable obs = p.obs[0]
			
			HashMap<DDVariable, Integer> fixAt = [:]
			fixAt[act]=0
			fixAt[obs]=0
			
			DDTransitionFunction fixedObsFn = p.obsFns.fix(fixAt);
			DDTransitionFunction fixedTransFn = p.transFns.fix(fixAt);
		
		then:
			
			p.initBeliefDD.context.getVariableSpace().each{ HashMap<DDVariable,Integer> varSpacePoint ->
				
				HashMap<DDVariable,Integer> varSpacePointPrime = [:]
				varSpacePoint.each { DDVariable var, Integer varValue ->
					varSpacePointPrime[var.getPrime()] = varValue
				}
				
				
				double obsProb = fixedObsFn.getValue(varSpacePointPrime)
				
				DDTransitionFunction fixedTransFnTemp = fixedTransFn.fix(varSpacePointPrime)
				fixedTransFnTemp = fixedTransFnTemp.sumOut(varSpacePoint.keySet())
				
				if(obsProb>0.0f) {
					double sPrimeProb = 0.0f
					fixedTransFnTemp.getDDs().each{ CondProbADD dd ->
						assert dd.getRules().size()==1
						sPrimeProb = dd.getRules().rules.first().value
						
					}
					
					println sPrimeProb*obsProb
				}
				else {
					println 0.0f
				}
			}
		
	}
	
}
