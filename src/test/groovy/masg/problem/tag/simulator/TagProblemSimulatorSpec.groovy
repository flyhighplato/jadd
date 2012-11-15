package masg.problem.tag.simulator

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import masg.dd.CondProbDD;
import masg.dd.ProbDD;
import masg.dd.alphavector.BeliefAlphaVector
import masg.dd.pomdp.agent.belief.BeliefRegion
import masg.dd.pomdp.agent.policy.Policy
import masg.dd.pomdp.agent.policy.PolicyBuilder
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.dd.variables.DDVariable
import masg.problem.tag.TagProblem;
import spock.lang.Shared;
import spock.lang.Specification

class TagProblemSimulatorSpec extends Specification {

	@Shared
	TagProblem problem = new TagProblem()

	@Shared
	Random random = new Random();
	
	def "agents are simulated correctly"() {
		when:
			
			DDVariable a1_row = new DDVariable("a1_row",5)
			DDVariable a1_col = new DDVariable("a1_col",5)
			DDVariable a2_row = new DDVariable("a2_row",5)
			DDVariable a2_col = new DDVariable("a2_col",5)
			DDVariable w_row = new DDVariable("w_row",5)
			DDVariable w_col = new DDVariable("w_col",5)
			
			DDVariable a1_row_loc = new DDVariable("a1_row_loc",5)
			DDVariable a1_col_loc = new DDVariable("a1_col_loc",5)
			DDVariable w_pres = new DDVariable("w_pres",2)
			
			
			Policy pol = new RandomPolicy(problem.getPOMDP())
			
			
			int numSamples = 1000
			int numIterations = 10
			
			BeliefRegion belReg = new BeliefRegion(numSamples, problem.getPOMDP(), pol)
			PolicyBuilder polBuilder = new PolicyBuilder(problem.getPOMDP())
			pol = polBuilder.build(belReg, numIterations)
			
			int numColocations = 0;
			int totalColocations = 0;
			int numSteps = 100;
			int numTrials = 10;
			
		then:
			for(BeliefAlphaVector alpha:polBuilder.bestAlphas) {
				println "${alpha.getAction()}: ${alpha.getValueFunction().getTotalWeight()}";
			}
			
			numTrials.times {
				TagAgent agent1 = new TagAgent(problem.getPOMDP(),pol)
				TagAgent agent2 = new TagAgent(problem.getPOMDP(),pol)
				TagWumpus wumpus = new TagWumpus(problem.getPOMDP())
				TagGrid grid = new TagGrid(5,5, agent1, agent2, wumpus)
				
				numColocations = 0;
				numSteps.times {
					
					HashMap<DDVariable,Integer> action1 = pol.getAction(agent1.currBelief);
					
					HashMap<DDVariable,Integer> actualStateAgt1 = new HashMap<DDVariable,Integer>()
					actualStateAgt1[a1_row] = agent1.row
					actualStateAgt1[a1_col] = agent1.column
					actualStateAgt1[a2_row] = agent2.row
					actualStateAgt1[a2_col] = agent2.column
					actualStateAgt1[w_row] = wumpus.row
					actualStateAgt1[w_col] = wumpus.column
					
					HashMap<DDVariable,Integer> action2 = pol.getAction(agent2.currBelief);
					HashMap<DDVariable,Integer> actualStateAgt2 = new HashMap<DDVariable,Integer>()
					actualStateAgt2[a1_row] = agent2.row
					actualStateAgt2[a1_col] = agent2.column
					actualStateAgt2[a2_row] = agent1.row
					actualStateAgt2[a2_col] = agent1.column
					actualStateAgt2[w_row] = wumpus.row
					actualStateAgt2[w_col] = wumpus.column
					
					if((wumpus.row==agent1.row && wumpus.column==agent1.column) || (wumpus.row==agent2.row && wumpus.column==agent2.column)) {
						++numColocations;
					}
					
					CondProbDD restrTransFn1 = problem.getPOMDP().getTransitionFunction(action1)
					restrTransFn1 = restrTransFn1.restrict(actualStateAgt1)
					restrTransFn1 = restrTransFn1.normalize();
					
					HashMap<DDVariable,Integer> actualStateAgt1New = sampleSpacePoint(problem.getPOMDP().getStatesPrime(), restrTransFn1)
					
					wumpus.moveRandomly(5, 5)
					
					actualStateAgt1New[w_row.getPrimed()] = wumpus.row
					actualStateAgt1New[w_col.getPrimed()] = wumpus.column
					
					CondProbDD restrTransFn2 = problem.getPOMDP().getTransitionFunction(action2)
					restrTransFn2 = restrTransFn2.restrict(actualStateAgt2)
					restrTransFn2 = restrTransFn2.normalize();
					
					HashMap<DDVariable,Integer> actualStateAgt2New = sampleSpacePoint(problem.getPOMDP().getStatesPrime(), restrTransFn2)
					
					HashMap<DDVariable,Integer> actualStateNewAg1Primed = new HashMap<DDVariable,Integer>();
					actualStateNewAg1Primed[a1_row.getPrimed()] = actualStateAgt1New[a1_row.getPrimed()]
					actualStateNewAg1Primed[a1_col.getPrimed()] = actualStateAgt1New[a1_col.getPrimed()]
					actualStateNewAg1Primed[a2_row.getPrimed()] = actualStateAgt2New[a1_row.getPrimed()]
					actualStateNewAg1Primed[a2_col.getPrimed()] = actualStateAgt2New[a1_col.getPrimed()]
					actualStateNewAg1Primed[w_row.getPrimed()] = actualStateAgt1New[w_row.getPrimed()]
					actualStateNewAg1Primed[w_col.getPrimed()] = actualStateAgt1New[w_col.getPrimed()]
					
					HashMap<DDVariable,Integer> actualStateNewAg2Primed = new HashMap<DDVariable,Integer>();
					actualStateNewAg2Primed[a1_row.getPrimed()] = actualStateAgt2New[a1_row.getPrimed()]
					actualStateNewAg2Primed[a1_col.getPrimed()] = actualStateAgt2New[a1_col.getPrimed()]
					actualStateNewAg2Primed[a2_row.getPrimed()] = actualStateAgt1New[a1_row.getPrimed()]
					actualStateNewAg2Primed[a2_col.getPrimed()] = actualStateAgt1New[a1_col.getPrimed()]
					actualStateNewAg2Primed[w_row.getPrimed()] = actualStateAgt1New[w_row.getPrimed()]
					actualStateNewAg2Primed[w_col.getPrimed()] = actualStateAgt1New[w_col.getPrimed()]
					
					
					CondProbDD restrObsFn1 = problem.getPOMDP().getObservationFunction().restrict(action1)
					restrObsFn1 = restrObsFn1.restrict(actualStateNewAg1Primed)
					restrObsFn1 = restrObsFn1.normalize()
					
					CondProbDD restrObsFn2 = problem.getPOMDP().getObservationFunction().restrict(action2)
					restrObsFn2 = restrObsFn2.restrict(actualStateNewAg2Primed)
					restrObsFn2 = restrObsFn2.normalize()
					
					HashMap<DDVariable,Integer> obs1 = sampleSpacePoint(problem.getPOMDP().getObservations(), restrObsFn1);
					HashMap<DDVariable,Integer> obs2 = sampleSpacePoint(problem.getPOMDP().getObservations(), restrObsFn2);
					
					println "Agent1"
					println "     action: $action1"
					println "     actual new state: $actualStateNewAg1Primed"
					println "     observation: $obs1"
					println "Agent2"
					println "     action: $action2"
					println "     actual new state: $actualStateNewAg2Primed"
					println "     observation: $obs2"
					println "Colocations: $numColocations"
					
					agent1.currBelief = agent1.currBelief.getNextBelief(action1, obs1)
					agent1.row = actualStateNewAg1Primed[a1_row.getPrimed()]
					agent1.column = actualStateNewAg1Primed[a1_col.getPrimed()]
					
					agent2.currBelief = agent2.currBelief.getNextBelief(action2, obs2)
					agent2.row = actualStateNewAg1Primed[a2_row.getPrimed()]
					agent2.column = actualStateNewAg1Primed[a2_col.getPrimed()]
					
					wumpus.row = actualStateNewAg1Primed[w_row.getPrimed()]
					wumpus.column = actualStateNewAg1Primed[w_col.getPrimed()]
					
					grid.draw()
					
				}
				
				totalColocations+=numColocations;
				
				println "Total colocations: $totalColocations (in ${it+1} runs)"
			}
	}
	
	private HashMap<DDVariable,Integer> sampleSpacePoint(ArrayList<DDVariable> variables, CondProbDD probFn) {
		HashMap<DDVariable,Integer> point = new HashMap<DDVariable,Integer>();
		
		for(DDVariable variable:variables) {
			ArrayList<DDVariable> sumOutVars = new ArrayList<DDVariable>(variables);
			sumOutVars.remove(variable);
			
			ProbDD probTempFn = probFn.sumOut(sumOutVars).toProbabilityFn();
			
			double thresh = random.nextDouble();
			double weight = 0.0f;
			
			for(int i=0;i<variable.getValueCount();i++){
				HashMap<DDVariable,Integer> tempPt = new HashMap<DDVariable,Integer>();
				tempPt.put(variable,i);
				weight += probTempFn.getValue(tempPt);
				if(weight>thresh) {
					point.put(variable,i);
					break;
				}
			}
		}
		return point;
	}
}
