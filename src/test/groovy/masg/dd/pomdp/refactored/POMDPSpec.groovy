package masg.dd.pomdp.refactored

import masg.dd.pomdp.POMDP;
import masg.dd.refactored.AlgebraicDD
import masg.dd.refactored.CondProbDD
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace
import masg.problem.tag.refactored.TagProblem
import spock.lang.Shared;
import spock.lang.Specification

class POMDPSpec extends Specification {
	@Shared
	TagProblem problem = new TagProblem()

	def "POMDP initial belief is correct"() {
		when:
			DDVariableSpace currVarSpace = new DDVariableSpace(problem.getPOMDP().getStates());
		then:
			currVarSpace.each {HashMap<DDVariable,Integer> varSpacePoint ->
				assert problem.getInitBeliefClosure()(varSpacePoint.collectEntries{k,v -> [k.toString(),v]}) == problem.getPOMDP().getInitialBelief().getValue(varSpacePoint);	
			}
		
	}
	
	def "POMDP reward function is correct"() {
		when:
			DDVariableSpace currVarSpace = new DDVariableSpace(problem.getPOMDP().getStates());
		then:
			currVarSpace.each {HashMap<DDVariable,Integer> varSpacePoint ->
				assert problem.getRewardFunctionClosure()(varSpacePoint.collectEntries{k,v -> [k.toString(),v]}) == problem.getPOMDP().getRewardFunction().getValue(varSpacePoint);
			}
		
	}
	
	def "POMDP transition function is correct"() {
		when:
			ArrayList<DDVariable> vars = new ArrayList<DDVariable>(problem.getPOMDP().getStates())
			vars.addAll(problem.getPOMDP().getStatesPrime())
			vars.addAll(problem.getPOMDP().getActions())
			println vars
			DDVariableSpace currVarSpace = new DDVariableSpace(vars);
		then:
			currVarSpace.each {HashMap<DDVariable,Integer> varSpacePoint ->
				double val = 1.0f;
				problem.getTransnFunctionClosures().each { Closure c ->
					val*=c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				}
				assert val == problem.getPOMDP().getTransitionFunction().getValue(varSpacePoint);
			}
		
	}
	
	def "POMDP observation function is correct"() {
		when:
			ArrayList<DDVariable> vars = new ArrayList<DDVariable>(problem.getPOMDP().getObservations())
			vars.addAll(problem.getPOMDP().getStatesPrime())
			vars.addAll(problem.getPOMDP().getActions())
			println vars
			DDVariableSpace currVarSpace = new DDVariableSpace(vars);
		then:
			currVarSpace.each {HashMap<DDVariable,Integer> varSpacePoint ->
				double val = 1.0f;
				problem.getObervnFunctionClosures().each { Closure c ->
					val*=c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				}
				assert val == problem.getPOMDP().getObservationFunction().getValue(varSpacePoint);
			}
		
	}
	
	def "POMDP functions can be used for a belief update"() {
		when:
			HashMap<DDVariable,Integer> obsPoint = new HashMap<DDVariable,Integer>();
			for(DDVariable v:problem.getPOMDP().getObservations()) {
				obsPoint[v]=0;
			}
			HashMap<DDVariable,Integer> actPoint = new HashMap<DDVariable,Integer>();
			for(DDVariable v:problem.getPOMDP().getActions()) {
				actPoint[v]=0;
			}
			
			CondProbDD restrTransFn = problem.getPOMDP().getTransitionFunction().restrict(actPoint);
			CondProbDD restrObsFn = problem.getPOMDP().getObservationFunction().restrict(actPoint);
			
			CondProbDD temp =  problem.getPOMDP().getInitialBelief() * restrTransFn;
			temp = temp.normalize();
			temp = temp.sumOut(problem.getPOMDP().getStates())
			
			temp = restrObsFn * temp;
			
		then:
			println()
			println temp;
			
			println restrObsFn;
		
	}
	
	
}
