package masg.dd.pomdp

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.List;

import masg.dd.AlgebraicDecisionDiagram;
import masg.dd.DecisionDiagramContext
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace
import masg.problem.tag.TagProblem

import spock.lang.Specification

class POMDPSpec extends Specification {
	
	TagProblem problem = new TagProblem()
	POMDP p = problem.getPOMDP()
	
	def "POMDP initial belief is correct"() {
		when:
			
			DDVariableSpace currVarSpace = problem.getPOMDP().initBeliefDD.getContext().getVariableSpace()
		then:
			
			currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
				double valClosure = problem.initBelief(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				double valDD = problem.getPOMDP().initBeliefDD.getValue(currVarSpace.generateRule(varSpacePoint,0.0f))
				
				assert valClosure == valDD
				
			}
	}
	
	
	def "POMDP transition function is correct"() {
		when:
			
			DDVariableSpace currVarSpace = new DDVariableSpace();
			
			
		then:
		
			problem.getPOMDP().getTransFns().getDDs().each{ AlgebraicDecisionDiagram dd ->
				currVarSpace = new DDVariableSpace();
				currVarSpace.addVariables(dd.getContext().getVariableSpace().getVariables())
				
				println "Testing varspace: ${currVarSpace.getVariables()}"
				currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
					double valClosure = 1.0f
					
					
					problem.transFns.each{Closure<Double> c ->
						try{
							valClosure *= c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
						} catch (Exception e) {
							
						}
					}
					
					double valDD = problem.getPOMDP().getTransFns().getValue(varSpacePoint)
					
					if(valClosure != valDD) {
						println "Invalid value for $varSpacePoint"
					}
					assert valClosure == valDD
					
				}
			}
	}
	
}
