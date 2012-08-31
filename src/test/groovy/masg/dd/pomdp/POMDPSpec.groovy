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
			println new Date().getTime()
			
			HashMap<DDVariable, Integer> fixAt = [:]
			p.actions.each{ DDVariable a->
				fixAt[a]=0
			}
			p.obs.each{ DDVariable o->
				fixAt[o]=1
			}
			
			
			println new Date().getTime()
			
			DDTransitionFunction fixedObsFn = p.obsFns.fix(fixAt);
			DDTransitionFunction fixedTransFn = p.transFns.fix(fixAt);
		
			DDTransitionFunction temp = fixedTransFn.multiply(p.initBeliefDD)
			temp = temp.sumOut(p.initBeliefDD.getContext().getVariableSpace().getVariables())
			temp = temp.multiply(fixedObsFn)
		then:
			println new Date().getTime()
		
	}
	
}
