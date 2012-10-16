package masg.dd.pomdp

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.List;

import masg.agent.pomdp.belief.BeliefRegion
import masg.agent.pomdp.policy.PolicyBuilder
import masg.agent.pomdp.policy.RandomPolicy;
import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.alphavector.DominantAlphaVectorCollection;
import masg.dd.context.DecisionDiagramContext;
import masg.dd.function.CondProbFunction
import masg.dd.function.RealValueFunction
import masg.dd.rules.DecisionRule;
import masg.dd.rules.DecisionRuleCollectionIndex
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
			
			DDVariableSpace currVarSpace = new DDVariableSpace();
			
			
		then:
		
			p.getInitialtBelief().getDDs().each{ AlgebraicDD dd ->
				currVarSpace = new DDVariableSpace();
				currVarSpace.addVariables(dd.getContext().getVariableSpace().getVariables())
				
				println "Testing varspace: ${currVarSpace.getVariables()}"
				currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
					double valClosure = 1.0f
					
					
					problem.initBelief.each{Closure<Double> c ->
						try{
							double temp = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
							valClosure = valClosure * temp
						} catch (Exception e) {
							//println "Transition function"
							//println varSpacePoint.collectEntries{k,v -> [k.toString(),v]}
							//println e
						}
					}
					
					double valDD = p.getInitialtBelief().getValue(varSpacePoint)
					
					if(valClosure != valDD) {
						println "Invalid value for $varSpacePoint"
					}
					assert valClosure == valDD
					
				}
			}
	}
	
	def "POMDP reward function is correct"() {
		when:
			
			DDVariableSpace currVarSpace = new DDVariableSpace();

		then:
		

				currVarSpace.addVariables(p.getRewardFn().getDD().getContext().getVariableSpace().getVariables())
				
				RealValueFunction rewFn = p.getRewardFn();
				
				println "Testing varspace: ${currVarSpace.getVariables()}"
				currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
					double valClosure = 1.0f
					
					
					problem.rewFn.each{Closure<Double> c ->
						try{
							double temp = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
							valClosure = valClosure * temp
						} catch (Exception e) {
							//println "Transition function"
							//println varSpacePoint.collectEntries{k,v -> [k.toString(),v]}
							//println e
						}
					}
					
					double valDD = p.getRewardFn().getValue(varSpacePoint)
					
					if(valClosure != valDD) {
						println "Invalid value for $varSpacePoint"
					}
					assert valClosure == valDD
					
				}
			
	}
	
	
	def "POMDP transition function is correct"() {
		when:
			
			DDVariableSpace currVarSpace = new DDVariableSpace();
			
			
		then:
		
			p.getTransFn().getDDs().each{ AlgebraicDD dd ->
				currVarSpace = new DDVariableSpace();
				currVarSpace.addVariables(dd.getContext().getVariableSpace().getVariables())
				
				println "Testing varspace: ${currVarSpace.getVariables()}"
				currVarSpace.each { HashMap<DDVariable,Integer> varSpacePoint ->
					double valClosure = 1.0f
					
					
					problem.transFns.each{Closure<Double> c ->
						try{
							double temp = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
							valClosure = valClosure * temp
						} catch (Exception e) {
							//println "Transition function"
							//println varSpacePoint.collectEntries{k,v -> [k.toString(),v]}
							//println e
						}
					}
					
					double valDD = p.getTransFn().getValue(varSpacePoint)
					
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
		
			p.getObsFns().getDDs().each{ AlgebraicDD dd ->
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
	
	/*def "POMDP can be saved to disk"() {
		when:
			File f = new File(fileName)
		then:
			f.withWriter{ Writer out ->
				p.writeOut(out)
			}
		
	}*/
	
	
	def "rule indexing works"() {
		when:
			DecisionRuleCollectionIndex index = p.getRewardFn().getDD().getRules().getIndex()
			DDVariableSpace currVarSpace = new DDVariableSpace();
		then:
			currVarSpace.addVariables(p.getRewardFn().getDD().getContext().getVariableSpace().getVariables())
			
			HashMap<DDVariable,Integer> pt = currVarSpace.iterator().next()
			DecisionRule refRule = currVarSpace.generateRule(pt,0.0f)
			
			
			for(List<DecisionRule> lst:index.getCandidateMatches(refRule)) {
				for(DecisionRule r:lst) {
					println r
				}
			}
			
	}
	
	def "belief updating works"() {
		when:
			HashMap<DDVariable,Integer> acts = [:]
			HashMap<DDVariable,Integer> obs = [:]
			
			acts[problem.actVar]=0
			obs[problem.wPresenceObsVar]=1
			obs[problem.a1ColObsVar]=0
			obs[problem.a1RowObsVar]=4
			
		then:
			Date startTime = new Date()
			
			long start = startTime.time

			int numUpdates = 0
			while(new Date().time - start < 60000) {
				numUpdates ++
				p.updateBelief(acts,obs)
			}
			
			println "Number of updates in a minute: $numUpdates"
	}
	
	def "belief region gets populated"() {
		when:
			RandomPolicy policy = new RandomPolicy(p)
		then:
			BeliefRegion bReg = new BeliefRegion(100, p, policy)
	}
	
	def "policy gets built"() {
		when:
			PolicyBuilder polBuilder = new PolicyBuilder(p)
			RandomPolicy policy = new RandomPolicy(p)
		then:
			BeliefRegion bReg = new BeliefRegion(100, p, policy)
			
			
			
			10.times {
				DominantAlphaVectorCollection bestAlphas = new DominantAlphaVectorCollection();
				
				bReg.getBeliefSamples().eachWithIndex { CondProbFunction b, i ->
					println "Computing for sample #$i"
					polBuilder.dpBackup(b,bestAlphas);
				}
				
				polBuilder.bestAlphas = bestAlphas;
			}
	}
	
}
