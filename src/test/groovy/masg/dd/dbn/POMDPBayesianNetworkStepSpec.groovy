package masg.dd.dbn

import java.io.BufferedWriter;

import masg.dd.AlgebraicDD
import masg.dd.CondProbDD
import masg.dd.FactoredCondProbDD
import masg.dd.pomdp.POMDP
import masg.dd.pomdp.agent.policy.RandomPolicy
import masg.dd.variables.DDVariable
import masg.problem.tag.TagProblemPOMDP;
import spock.lang.Shared;
import spock.lang.Specification

class POMDPBayesianNetworkStepSpec extends Specification {
	
	int numAgents = 2
	int height = 5, width = 5
	@Shared
	TagProblemPOMDP problem = new TagProblemPOMDP()
	
	private updateState(MultiAgentTagProblemBNBuilder builder, oldState) {
		
		def gameState = POMDPBayesianNetworkStep.sample(oldState)
		def ret = []
		def allActions = []
		builder.actions.values().each {
			allActions += it
		}
		
		def allObservations = []
		builder.observations.values().each {
			allObservations += it
		}
		
		BayesianNetwork beliefNetwork = builder.fullNetwork
		
		def actSample = []
		(numAgents + 1).times { ix ->
			def actions = builder.actions[ix]
			def varValues = 1
			actions.each { DDVariable v ->
				varValues *= v.numValues
			}
			def temp = [new AlgebraicDD(actions, 0, {argsVars -> 1.0d/varValues }, true)]
			actSample << new AlgebraicDD(actions, 0, POMDPBayesianNetworkStep.sample(temp))
			
		}
		
		println POMDPBayesianNetworkStep.sample(actSample)
		ret << actSample
		
		BayesianNetworkResult networkResult = new BayesianNetworkResult( beliefNetwork )
		oldState.each {
			networkResult.probabilityIs(it)
		}
		actSample.each {
			networkResult.decisionIs(it)
		}
		networkResult.solve()
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		def obsDist = newNetwork.getJointProbability( allObservations )
		def obs = POMDPBayesianNetworkStep.sample(obsDist)
		def obsSample = new AlgebraicDD(allObservations, 0, obs)
		
		println obs
		ret << [obsSample]
		
		networkResult = new BayesianNetworkResult( beliefNetwork )
		oldState.each {
			networkResult.probabilityIs(it)
		}
		actSample.each {
			networkResult.decisionIs(it)
		}
		
		obsSample.each {
			networkResult.probabilityIs(it)
		}
		networkResult.solve()
		newNetwork = networkResult.buildNetwork()
		
		def newGameState = newNetwork.getJointProbability( builder.states.collect{ it.getPrimed() } )
		
		gameState = POMDPBayesianNetworkStep.sample(newGameState)
		def gameStateSample = new AlgebraicDD(builder.states.collect{ it.getPrimed() }, 0, gameState)
		ret << [gameStateSample.unprime()]
		
		ret
	}
	
	private updateBelief(int agentNumber, MultiAgentTagProblemBNBuilder builder, oldBelief, action, observation) {
		
		def allActions = []
		builder.actions.values().each {
			allActions += it
		}
		
		def allObservations = []
		builder.observations.values().each {
			allObservations += it
		}
		
		BayesianNetwork beliefNetwork = builder.beliefNetworks[agentNumber]
		
		def actSample = []
		(numAgents + 1).times { ix ->
			def actions = builder.actions[ix]
			def varValues = 1
			actions.each { DDVariable v ->
				varValues *= v.numValues
			}
			if(ix == agentNumber) {
				//def temp = [new AlgebraicDD(actions, 0, {argsVars -> 1.0d/varValues }, true)]
				actSample << new AlgebraicDD(actions, 0, action)
			}
			else {
				actSample << new AlgebraicDD(actions, 0, {argsVars -> 1.0d/varValues }, true)
			}
			
		}
		
		//Sample possible observation
		/*BayesianNetworkResult networkResult = new BayesianNetworkResult( beliefNetwork )
		oldBelief.each {
			networkResult.probabilityIs(it)
		}
		actSample.each {
			networkResult.decisionIs(it)
		}
		networkResult.solve()
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		def obsDist = newNetwork.getJointProbability( builder.observations[agentNumber] )*/
		def obsSample = new AlgebraicDD(builder.observations[agentNumber], 0, observation)
		
		//Get next belief
		BayesianNetworkResult networkResult = new BayesianNetworkResult( beliefNetwork )
		oldBelief.each {
			networkResult.probabilityIs(it)
		}
		actSample.each {
			networkResult.decisionIs(it)
		}
		
		obsSample.each {
			networkResult.probabilityIs(it)
		}
		networkResult.solve()
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		newNetwork.getJointProbability( builder.states.collect{ it.getPrimed() } )
	}
	
	def "pomdp bn step can create action distribution"() {
		when:
			POMDP p = problem.getPOMDP()
			
			MultiAgentTagProblemBNBuilder builder = new MultiAgentTagProblemBNBuilder(numAgents)
			
			def beliefs = []
			numAgents.times {
				int varValues = 1
				builder.states.each { DDVariable v ->
					varValues *= v.numValues
				}
				
				beliefs << [new AlgebraicDD(builder.states, 0, {argsVars -> 1.0d/varValues }, true)]
			}
			
			def gameState = beliefs[0]
			
			
			
			BayesianNetwork agt1BeliefNetwork = builder.beliefNetworks[0]
			
			def allActions = []
			builder.actions.values().each {
				allActions += it
			}
			
			def allObservations = []
			builder.observations.values().each {
				allObservations += it
			}
			
			POMDPBayesianNetworkStep step = new POMDPBayesianNetworkStep(builder.trackingNetworks[0][0], builder.beliefNetworks[1], beliefs[0], allActions, builder.actions[1], builder.states, allObservations, builder.observations[1])
			
			100.times {
				
				println "step #$it"
				
				def res = updateState(builder,gameState)
				
				def act = res[0]
				def obs = res[1]
				gameState = res[2]
				
				def agents = []
				
				def state = POMDPBayesianNetworkStep.sample(gameState)
				
				println state
				(numAgents + 1).times { ix ->
					int row = state[new DDVariable(0,"agt${ix}_row",height)]
					int col = state[new DDVariable(0,"agt${ix}_col",width)]
					
					agents << ["row":row, "column":col]
				}
				
				draw(agents[0],agents[1],agents[2])
				
				def beliefsNew = []
				beliefs.eachWithIndex { b, index ->
					
					def thisAct = POMDPBayesianNetworkStep.sample(act,builder.actions[index])
					def thisObs = POMDPBayesianNetworkStep.sample(obs,builder.observations[index])
					
					def newBelief = updateBelief(index, builder, b, thisAct, thisObs)
					beliefsNew << newBelief
				}
				beliefs = beliefsNew
				
				step = step.revise(beliefs[0])
				
				beliefs = beliefs.collect { b ->
					b.collect { it.unprime() }
				}
			}
			
		then:
			10.times {
				println step.sampleBelief()
			}
	}
	
	public void draw(agent1, agent2, wumpus, BufferedWriter w = new BufferedWriter(new PrintWriter(System.out)))
	{
		
		for(int j=0;j<width;j++)
		{
			w.write("-----");
		}
		
		w.newLine();
		
		height.times { int i ->
			
			width.times { int j ->
				w.write "|"
				
				if(agent1.column == j && agent1.row == i)
					w.write "1"
				else
					w.write " "
				
				if(wumpus.column == j && wumpus.row == i)
					w.write "W"
				else
					w.write " ";
				
				w.write "  "
			}
			
			w.write "|"
			w.newLine()
			
			width.times { int j ->
				w.write "|"
				if(agent2.column == j && agent2.row == i)
					w.write "2"
				else
					w.write " "
				
				w.write " "
				
				w.write "  "
			}
			
			w.write "|"
			w.newLine()
			
			width.times {
				w.write "-----"
			}
			
			w.newLine()
		}
		
		w.flush()
	}
	
}
