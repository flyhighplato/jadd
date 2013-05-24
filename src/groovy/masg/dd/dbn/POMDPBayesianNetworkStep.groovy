package masg.dd.dbn

import masg.dd.AlgebraicDD
import masg.dd.CondProbDD
import masg.dd.FactoredCondProbDD
import masg.dd.pomdp.POMDP
import masg.dd.pomdp.agent.belief.POMDPBelief
import masg.dd.variables.DDVariable

class POMDPBayesianNetworkStep {
	POMDPBayesianNetworkStep parent
	POMDPBayesianNetworkStep child
	
	BayesianNetwork stationaryNetwork
	List<AlgebraicDD> stateDist
	List<AlgebraicDD> obsDist
	List<AlgebraicDD> actDist
	
	int time = 0
	
	POMDP p
	Random r = new Random()
	
	public POMDPBayesianNetworkStep(POMDP p) {
		this.p = p
		
		stationaryNetwork = new BayesianNetwork()
		
		p.transnFn.each { FactoredCondProbDD fcdd ->
			fcdd.functions.each{ CondProbDD cdd ->
				UncertaintyNode un = new UncertaintyNode(cdd.conditionalVariables, cdd.posteriorVariables, cdd.fn)
				stationaryNetwork.addNode(un)
			}
		}
		
		p.observationFunction.each { FactoredCondProbDD fcdd ->
			fcdd.functions.each{ CondProbDD cdd ->
				UncertaintyNode un = new UncertaintyNode(cdd.conditionalVariables, cdd.posteriorVariables, cdd.fn)
				stationaryNetwork.addNode(un)
			}
		}
		
		DecisionNode dn = new DecisionNode(p.actions, 0)
		stationaryNetwork.addNode(dn)
		
		stateDist = p.getInitialBelief().getFunctions().collect { CondProbDD dd ->
			dd.getFunction()
		}
		
		int actCombinations = 1
		p.getActions().each {
			actCombinations *= it.numValues
		}
		actDist = [new AlgebraicDD(p.getActions(),1.0d/actCombinations)]
		
		int obsCombinations = 1
		p.getObservations().each {
			obsCombinations *= it.numValues
		}
		obsDist = [new AlgebraicDD(p.getObservations(),1.0d/obsCombinations)]
		
		computeObsDist()
		computeActDist()
		
	}
	
	protected POMDPBayesianNetworkStep(POMDPBayesianNetworkStep parent, POMDP p, BayesianNetwork stationaryNetwork, List<AlgebraicDD> stateDist) {
		this.parent = parent
		this.p = p
		this.stationaryNetwork = stationaryNetwork
		this.time = parent.time + 1
		
		this.stateDist = stateDist
		
		int actCombinations = 1
		p.getActions().each {
			actCombinations *= it.numValues
		}
		actDist = [new AlgebraicDD(p.getActions(),1.0d/actCombinations)]
		
		int obsCombinations = 1
		p.getObservations().each {
			obsCombinations *= it.numValues
		}
		obsDist = [new AlgebraicDD(p.getObservations(),1.0d/obsCombinations)]
		
		computeObsDist()
		computeActDist()
		
	}
	
	private computeActDist() {
		/*BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
		stateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		obsDist.each {
			networkResult.probabilityIs(it)
		}
		
		networkResult.solve()
		
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		actDist = newNetwork.getJointProbability( p.getActions() )
		
		networkResult.solve()*/
	}
	
	private computeObsDist() {
		BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
		stateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		actDist.each {
			networkResult.decisionIs(it)
		}
		
		networkResult.solve()
		
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		obsDist = newNetwork.getJointProbability( p.getObservations() )
		
		networkResult.solve()
	}
	
	public revise(List<AlgebraicDD> postStateDist) {
		
		BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
		stateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		postStateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		networkResult.solve()
		
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		actDist = newNetwork.getJointProbability( p.getActions() )
		obsDist = newNetwork.getJointProbability( p.getObservations() )
		
		
		networkResult = new BayesianNetworkResult( this.stationaryNetwork )
		
		
		actDist.each {
			networkResult.decisionIs(it)
		}
		
		obsDist.each {
			networkResult.probabilityIs(it)
		}
		
		postStateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		
		networkResult.solve()
		
		newNetwork = networkResult.buildNetwork()
		
		List<AlgebraicDD> stateDistOld = stateDist
		List<AlgebraicDD> stateDistNew = newNetwork.getJointProbability( p.getStates() )
		
		stateDist = stateDistNew.collect { AlgebraicDD ddNew ->
			AlgebraicDD ddOld = stateDistOld.find { AlgebraicDD ddOld -> 
				ddOld.variables.containsAll(ddNew.variables) 
			}
			
			ddNew.multiply(ddOld.sumOut(ddOld.variables-ddNew.variables)).normalize()
			
		}
		
		
		if(parent) {
			def stateDistPrime = stateDist.collect {
				it.prime()
			}
			
			parent.revise(stateDistPrime)
		}
		
		def postStateDistUnprimed = postStateDist.collect {
			it.unprime()
		}
		
		if(!child) {
			child = new POMDPBayesianNetworkStep( this, p, stationaryNetwork, postStateDistUnprimed )
		}
		
		child
	}
	
	public sampleAction() {
		return sample(actDist)
	}
	
	public sampleObservation() {
		return sample(obsDist)
	}
	
	private sample(List<AlgebraicDD> fns) {
		def sample = [:]
		
		fns.each { AlgebraicDD dd ->
			dd.variables.each { DDVariable varCurr ->
				double thresh = r.nextDouble()
				double accumProb = 0.0d
				int varVal = 0
				
				AlgebraicDD singleVarDD = dd.sumOut(dd.variables - varCurr)
				
				while(accumProb <= thresh && varVal < varCurr.numValues) {
					sample[varCurr] = varVal
					accumProb += singleVarDD.getValue(sample)
					varVal ++
				}
				
			}
			
		}
		
		sample
	}
	
	public sampleBelief() {
		if(!parent) {
			return new POMDPBelief(p, p.getInitialBelief())
		}
		else {
			POMDPBelief b = parent.sampleBelief()
			def act = sampleAction()
			
			BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
			stateDist.each { AlgebraicDD dd ->
				networkResult.probabilityIs(dd)
			}
			
			networkResult.decisionIs(new AlgebraicDD(p.getActions(),0,act))
			
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
			obsDist = newNetwork.getJointProbability( p.getObservations() )
			
			def obs = sample(obsDist)
			println time + ":" + act + ":" + obs
			
			println()
			return b.getNextBelief(act, obs)
		}
	}
	
	public getStateDistChain() {
		if(parent)
			return [stateDist] + parent.getStateDistChain()
		else
			return [stateDist]
	}
	
	public getObsDistChain() {
		if(parent)
			return [obsDist] + parent.getObsDistChain()
		else
			return [obsDist]
	}
	
	public getActDistChain() {
		if(parent)
			return [actDist] + parent.getActDistChain()
		else
			return [actDist]
	}
	
}
