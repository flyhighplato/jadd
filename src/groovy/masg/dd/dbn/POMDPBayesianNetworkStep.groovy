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
	List<AlgebraicDD> postStateDist
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
		
		initializeActDist()
		
	}
	
	protected POMDPBayesianNetworkStep(POMDPBayesianNetworkStep parent, POMDP p, BayesianNetwork stationaryNetwork, List<AlgebraicDD> stateDist) {
		this.parent = parent
		this.p = p
		this.stationaryNetwork = stationaryNetwork
		this.time = parent.time + 1
		
		initializeActDist()
		
		
	}
	
	private initializeActDist() {
		
		if(parent && parent.postStateDist) {
			BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
			
			parent.postStateDist.each { AlgebraicDD dd ->
				networkResult.probabilityIs(dd.unprime())
			}
			
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
			actDist = newNetwork.getJointProbability( p.getActions() )
		}
		else {
			int actCombinations = 1
			p.getActions().each {
				actCombinations *= it.numValues
			}
			actDist = [new AlgebraicDD(p.getActions(),1.0d/actCombinations)]
		}
	}
	
	private updatePostState(List<AlgebraicDD> postStateDist) {
		if(this.postStateDist) {
			this.postStateDist = postStateDist.collect { AlgebraicDD ddNew ->
				AlgebraicDD ddOld = this.postStateDist.find { AlgebraicDD ddOld ->
					ddOld.variables.containsAll(ddNew.variables)
				}
				
				ddNew.multiply(ddOld.sumOut(ddOld.variables-ddNew.variables)).normalize().prime()
			}
		}
		else {
			this.postStateDist = postStateDist
		}
		
		if(parent) {
			BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
			
			
			actDist.each {
				networkResult.decisionIs(it)
			}
			
			this.postStateDist.each { AlgebraicDD dd ->
				networkResult.probabilityIs(dd)
			}
			
			
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
			List<AlgebraicDD> stateDistNew = newNetwork.getJointProbability( p.getStates() ).collect { it.prime() }
			
			parent.updatePostState(stateDistNew)
		}
	}
	
	private updateActObs() {
		if(parent) {
			parent.updateActObs()
		}
		
		BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
		
		if(parent && parent.postStateDist) {
			parent.postStateDist.each { AlgebraicDD dd ->
				networkResult.probabilityIs(dd.unprime())
			}
		}
		else {
			p.getInitialBelief().each { FactoredCondProbDD fcdd ->
				fcdd.functions.each{ CondProbDD cdd ->
					networkResult.probabilityIs(cdd.getFunction())
				}
			}
		}
		
		postStateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		networkResult.solve()
		
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		actDist = newNetwork.getJointProbability( p.getActions() )
	}
	
	
	public revise(List<AlgebraicDD> postStateDist) {
		updatePostState(postStateDist)
		updateActObs()
		
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
		POMDPBelief b
		if(parent) {
			b = parent.sampleBelief()
		}
		else {
			b = new POMDPBelief(p, p.getInitialBelief())
		}
		def act = sampleAction()
		
		BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
		postStateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		if(parent) {
			parent.postStateDist.each { AlgebraicDD dd ->
				networkResult.probabilityIs(dd.unprime())
			}
		}
		
		networkResult.decisionIs(new AlgebraicDD(p.getActions(),0,act))
		networkResult.solve()
		
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		List<AlgebraicDD> obsDistTemp = newNetwork.getJointProbability( p.getObservations() )
		
		def obs = sample(obsDistTemp)
		println time + ":" + act + ":" + obs
		
		println()
		
		try {
			return b.getNextBelief(act, obs)
		} catch (e) {
			println "impossible belief"
		}
		
	}
	
	public getActDistChain() {
		if(parent)
			return [actDist] + parent.getActDistChain()
		else
			return [actDist]
	}
	
}
