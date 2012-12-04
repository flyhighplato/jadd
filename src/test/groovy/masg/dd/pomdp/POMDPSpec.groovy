package masg.dd.pomdp

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.ProbDD;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.pomdp.agent.belief.BeliefRegion;
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.AlphaVectorPolicyBuilder;
import masg.dd.pomdp.agent.policy.RandomPolicy;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;
import masg.problem.tag.TagProblem;
import spock.lang.Shared;
import spock.lang.Specification
import spock.lang.Ignore;

class POMDPSpec extends Specification {
	@Shared
	TagProblem problem = new TagProblem()

	
	def "POMDP initial belief is correct"() {
		when:
			DDVariableSpace currVarSpace = new DDVariableSpace(problem.getPOMDP().getStates());
		then:
			println problem.getPOMDP().getInitialBelief()
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
	
	@Ignore
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
	
	@Ignore
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
			obsPoint[problem.a1RowObsVar]=4
			
			HashMap<DDVariable,Integer> actPoint = new HashMap<DDVariable,Integer>();
			for(DDVariable v:problem.getPOMDP().getActions()) {
				actPoint[v]=0;
			}
			
			
			CondProbDD restrTransFn = problem.getPOMDP().getTransitionFunction()
			restrTransFn = restrTransFn.restrict(actPoint);
			
			CondProbDD restrObsFn = problem.getPOMDP().getObservationFunction().restrict(actPoint).restrict(obsPoint);
			
			def timeLimit = 100
			def timeStart = new Date().getTime()
			int numBeliefUpdates = 0;
			
			def belief = problem.getPOMDP().getInitialBelief();
			
			//while(new Date().getTime()-timeStart<timeLimit) {
			while(true) {
				CondProbDD tempRestrTransFn = restrTransFn.multiply(belief)
				tempRestrTransFn = tempRestrTransFn.sumOut(problem.getPOMDP().getStates())
				tempRestrTransFn = tempRestrTransFn.normalize()
				CondProbDD temp = restrObsFn.multiply(tempRestrTransFn);
				temp = temp.normalize()
				belief = temp.unprime();
				numBeliefUpdates++;
				
			}
			
			def timeEnd = new Date().getTime() - timeStart
			
			HashMap<DDVariable,Integer> valPoint = new HashMap<DDVariable,Integer>()
			valPoint[problem.a1RowVar]=4
			valPoint[problem.a1ColVar]=0
			valPoint[problem.wRowVar]=4
			valPoint[problem.wColVar]=0
			belief = belief.toProbabilityFn();
			
		then:
			println "$numBeliefUpdates belief updates took $timeEnd milliseconds"
			println belief;
			//println belief.getValue(valPoint);
			assert numBeliefUpdates > 10000
			assert Math.abs(belief.getValue(valPoint) - 0.0f) < 0.01f;
	}
	
	def "POMDP functions can be used for reward calculation"() {
		when:
			HashMap<DDVariable,Integer> obsPoint = new HashMap<DDVariable,Integer>();
			for(DDVariable v:problem.getPOMDP().getObservations()) {
				obsPoint[v]=0;
			}
			obsPoint[problem.a1RowObsVar]=4
			
			HashMap<DDVariable,Integer> actPoint = new HashMap<DDVariable,Integer>();
			for(DDVariable v:problem.getPOMDP().getActions()) {
				actPoint[v]=0;
			}
			
			
			CondProbDD restrTransFn = problem.getPOMDP().getTransitionFunction()
			restrTransFn = restrTransFn.restrict(actPoint);
			
			CondProbDD restrObsFn = problem.getPOMDP().getObservationFunction().restrict(actPoint).restrict(obsPoint);
			
			AlgebraicDD restrRewFn = problem.getPOMDP().getRewardFunction().restrict(actPoint)
			
			def timeLimit = 60000
			def timeStart = new Date().getTime()
			int numRewardUpdates = 0;
			
			def belief = problem.getPOMDP().getInitialBelief();
			
			
			AlgebraicDD valueFn = restrRewFn.multiply(belief)
			while(new Date().getTime()-timeStart<timeLimit) {
				CondProbDD tempRestrTransFn = restrTransFn.multiply(belief)
				tempRestrTransFn = tempRestrTransFn.sumOut(problem.getPOMDP().getStates())
				
				CondProbDD temp = restrObsFn.multiply(tempRestrTransFn);
				temp = temp.normalize()
				belief = temp.unprime();
				
				AlgebraicDD immReward = restrRewFn.multiply(belief)
				AlgebraicDD newValueFn = valueFn.multiply(0.9f)
				valueFn = newValueFn.plus(immReward)
				numRewardUpdates++;
				
			}
			
			belief = belief.toProbabilityFn();
			def timeEnd = new Date().getTime() - timeStart
		then:
			//println belief
			//println valueFn
			
			println "$numRewardUpdates value updates in $timeEnd milliseconds"
	
	}
	
	def "POMDP functions can be used for belief sampling"() {
		when:
			def timeStart = new Date().getTime()
			
			int numSamples = 1000
			RandomPolicy randPolicy = new RandomPolicy(problem.getPOMDP())
			BeliefRegion belReg = new BeliefRegion(numSamples, problem.getPOMDP(), randPolicy)
			
			def timeEnd = new Date().getTime() - timeStart
		then:
			/*belReg.getBeliefSamples().each{ CondProbDD beliefSample ->
				println beliefSample.toProbabilityFn()
			}*/
			
			println "$numSamples beliefs sampled in $timeEnd milliseconds"
	}
	
	def "POMDP functions can be used for DP backup"() {
		when:
			
			int numIterations = 10
			int numSamples = 10
			RandomPolicy randPolicy = new RandomPolicy(problem.getPOMDP())
			BeliefRegion belReg = new BeliefRegion(numSamples, problem.getPOMDP(), randPolicy)
			
			def timeStart = new Date().getTime()
			
			AlphaVectorPolicyBuilder polBuilder = new AlphaVectorPolicyBuilder(problem.getPOMDP())
			Policy pol = polBuilder.build(belReg, numIterations)
			
			def timeEnd = new Date().getTime() - timeStart
		then:
			for(BeliefAlphaVector alpha:polBuilder.bestAlphas) {
				println "${alpha.getAction()}: ${alpha.getValueFunction().getTotalWeight()}";
				println alpha.getValueFunction()
			}
			println "Took $timeEnd milliseconds to backup $numSamples samples $numIterations times"
	}
}
