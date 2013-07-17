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
	
	List<AlgebraicDD> initialBelief = []
	
	def actions = []
	def observations = []
	def states = []
	
	BayesianNetwork beliefOtherNetwork
	def actOther = [], obsOther = []
	
	/*def states = []
	def actionsMe = []
	def obsMe = []*/
	
	int time = 0
	
	//POMDP p
	static Random r = new Random()
	
	/*public POMDPBayesianNetworkStep(POMDP p) {
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
		
		p.getInitialBelief().each { FactoredCondProbDD fcdd ->
			fcdd.functions.each{ CondProbDD cdd ->
				initialBelief << cdd.getFunction()
			}
		}
		
		DecisionNode dn = new DecisionNode(p.actions, 0)
		stationaryNetwork.addNode(dn)
		
		initializeActDist()
		
	}*/
	
	public POMDPBayesianNetworkStep(BayesianNetwork stationaryNetwork, BayesianNetwork beliefOtherNetwork, initialBelief, actions, actOther, states, observations, obsOther) {
		this.stationaryNetwork = stationaryNetwork
		this.beliefOtherNetwork = beliefOtherNetwork
		this.initialBelief = initialBelief
		this.actions = actions
		this.actOther = actOther
		this.states = states
		this.observations = observations
		this.obsOther = obsOther
		initializeActDist()
	}
	
	protected POMDPBayesianNetworkStep(POMDPBayesianNetworkStep parent, BayesianNetwork stationaryNetwork, BayesianNetwork beliefOtherNetwork, initialBelief, actions, actOther, states, observations, obsOther) {
		this.parent = parent
		this.stationaryNetwork = stationaryNetwork
		this.beliefOtherNetwork = beliefOtherNetwork
		this.initialBelief = initialBelief
		this.actions = actions
		this.actOther = actOther
		this.states = states
		this.observations = observations
		this.obsOther = obsOther
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
			
			actDist = newNetwork.getJointProbability( actions )
		}
		else {
			int actCombinations = 1
			actions.each {
				actCombinations *= it.numValues
			}
			actDist = [new AlgebraicDD(actions,1.0d/actCombinations)]
		}
	}
	
	
	private multiply(List<AlgebraicDD> fn1, List<AlgebraicDD> fn2) {
		def fnsNew = []
		def fns = fn1 + fn2
		
		while(fns) {
			int oldSize = 0
			
			HashSet group = new HashSet()
			group.add(fns[0])
			HashSet vars = new HashSet()
			vars.addAll(fns[0].variables)
			
			while(group.size() != oldSize) {
				oldSize = group.size()
				
				group.addAll(fns.findAll {
					it.variables.intersect(vars)
				})
				
				group.each {
					vars.addAll(it.variables)
				}
			}
			
			fns = fns - group
			
			AlgebraicDD ddNew = new AlgebraicDD(new ArrayList(vars),1.0d)
			
			group.each {
				ddNew = ddNew.multiply(it)
			}
			
			fnsNew << ddNew.normalize()
		}
		
		fnsNew
	}
	
	private updatePostState(List<AlgebraicDD> postStateDist) {
		if(this.postStateDist) {
			this.postStateDist = multiply(this.postStateDist,postStateDist)
		}
		else {
			this.postStateDist = postStateDist
		}
		
		
		assert this.postStateDist
		
		def temp = this.postStateDist.find{
			it.totalWeight < 0.8d
		}
		
		if(temp) {
			println "wrong!"
		}
		this.postStateDist.find{
			assert it.totalWeight > 0.8d
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
			
			List<AlgebraicDD> stateDistNew = newNetwork.getJointProbability( states ).collect { it.prime() }
			
			parent.updatePostState(stateDistNew)
		}
	}
	
	private updateActObs() {
		if(parent) {
			parent.updateActObs()
		}
		
		def oldActDist = actDist
		
		BayesianNetworkResult networkResult = new BayesianNetworkResult( this.stationaryNetwork )
		
		if(parent && parent.postStateDist) {
			parent.postStateDist.each { AlgebraicDD dd ->
				networkResult.probabilityIs(dd.unprime())
			}
		}
		else {
			initialBelief.each {
				networkResult.probabilityIs(it)
			}
		}
		
		postStateDist.each { AlgebraicDD dd ->
			networkResult.probabilityIs(dd)
		}
		
		networkResult.solve()
		
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		
		actDist = newNetwork.getJointProbability( actions )
		
		def temp = actDist.find{
			it.totalWeight < 0.8d
		}
		
		if(temp) {
			println "wrong!"
		}
		
		assert actDist
	}
	
	
	public revise(List<AlgebraicDD> postStateDist) {
		updatePostState(postStateDist)
		updateActObs()
		
		def postStateDistUnprimed = postStateDist.collect {
			it.unprime()
		}
		
		if(!child) {
			child = new POMDPBayesianNetworkStep( this, stationaryNetwork, beliefOtherNetwork, postStateDistUnprimed, actions, actOther, states, observations, obsOther )
		}
		
		child
	}
	
	public sampleAction() {
		return sample(actDist)
	}
	
	public static sample(List<AlgebraicDD> fns, def vars = []) {
		def sample = [:]
		
		fns.each { AlgebraicDD dd ->
			def varsTemp = dd.variables
			
			if(vars) {
				varsTemp = vars.intersect(dd.variables)
			}
			
			varsTemp.each { DDVariable varCurr ->
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
		List<AlgebraicDD> b
		def bFiltered = []
		
		while(!bFiltered || bFiltered.find { it.totalWeight - 1.0d < -0.01d}) {
			if(parent) {
				//println "Sampling at time $time"
				b = parent.sampleBelief()
				bFiltered = multiply(b,parent.postStateDist.collect {it.unprime()})
			}
			else {
				b =  initialBelief
				bFiltered = b
			}
			
			if(bFiltered.find { it.totalWeight - 1.0d < -0.01d}) {
				println "Dropping sample and resampling"
			}
		}
		
		def act = sample(actDist,actOther)
		def actSample = new AlgebraicDD(actOther,0,act)
		
		def actDistTemp = actDist.collect {
			if(it.variables.containsAll(actOther)) {
				return actSample
			}
			else {
				return it
			}
		}
		
		
		
		//Compute possible states prime
		BayesianNetworkResult networkResult = new BayesianNetworkResult( this.beliefOtherNetwork )
		
		bFiltered.each { AlgebraicDD dd ->
			assert Math.abs(dd.totalWeight - 1.0d) < 0.0000001d
			networkResult.probabilityIs(dd)
		}
		
		actDistTemp.each { 
			networkResult.decisionIs(it)
		}
		networkResult.solve()
		BayesianNetwork newNetwork = networkResult.buildNetwork()
		List<AlgebraicDD> possNextStates = newNetwork.getJointProbability( states.collect {it.getPrimed()} )
		
		if(postStateDist) {
			possNextStates = multiply(possNextStates,postStateDist)
		}
		
		def actDistOther = actDist.collect {
			if(it.variables.containsAll(actOther)) {
				return actSample
			}
			else {
				int actCombinations = 1
				it.variables.each {
					actCombinations *= it.numValues
				}
				return new AlgebraicDD(it.variables,1.0d/actCombinations)
			}
		}
		
		def actDistOtherFiltered = multiply(actDist, actDistOther)
		
		//Compute possible observations
		networkResult = new BayesianNetworkResult( this.beliefOtherNetwork )
		bFiltered.each {
			//assert Math.abs(it.totalWeight - 1.0d) < 0.000001d
			networkResult.probabilityIs(it)
		}
		possNextStates.each {
			//assert Math.abs(it.totalWeight - 1.0d) < 0.000001d
			networkResult.probabilityIs(it)
		}
		actDistOtherFiltered.each {
			//assert Math.abs(it.totalWeight - 1.0d) < 0.0000001d
			networkResult.decisionIs(it)
		}
		networkResult.solve()
		newNetwork = networkResult.buildNetwork()
		
		List<AlgebraicDD> obsPossible = newNetwork.getJointProbability( observations )
		
		/*obsPossible.each {
			assert Math.abs(it.totalWeight - 1.0d) < 0.0000001d
		}*/
		
		def obs = sample(obsPossible, obsOther)
		def obsSample = new AlgebraicDD(obsOther,0,obs)
		
		println time + ":" + act + ":" + obs
		println()

		//Compute resulting belief
		
		networkResult = new BayesianNetworkResult( this.beliefOtherNetwork )
		b.each {
			networkResult.probabilityIs(it)
		}
		
		actDistOther.each {
			networkResult.decisionIs(it)
		}
		networkResult.probabilityIs(obsSample)
		networkResult.solve()
		
		newNetwork = networkResult.buildNetwork()
		
		def result =  newNetwork.getJointProbability(states.collect{it.getPrimed()}).collect {
			it.unprime()
		}
		
		result
		
	}
	
	public getActDistChain() {
		if(parent)
			return [actDist] + parent.getActDistChain()
		else
			return [actDist]
	}
	
}
