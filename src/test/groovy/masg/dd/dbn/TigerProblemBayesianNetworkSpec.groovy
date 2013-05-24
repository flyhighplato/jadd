package masg.dd.dbn

import masg.dd.AlgebraicDD
import masg.dd.variables.DDVariable
import spock.lang.Specification
import masg.dd.context.DDContext

class TigerProblemBayesianNetworkSpec extends Specification {
	DDVariable tigerLocation = new DDVariable(0,"L",2)
	DDVariable growlLocation = new DDVariable(0,"G",2)
	DDVariable action = new DDVariable(0,"ACT",3)
	
	def setup() {
		DDContext.setCanonicalVariableOrdering([action,tigerLocation,growlLocation,tigerLocation.getPrimed()])
	}
	
	private BayesianNetwork buildNetwork() {
		BayesianNetwork network = new BayesianNetwork()
		
		UncertaintyNode un = new UncertaintyNode([tigerLocation,action],[tigerLocation.getPrimed()],0,{ argVars ->
			int loc = argVars["L"]
			int locNew = argVars["L'"]
			int act = argVars["ACT"]
			
			if(act == 0 ) {
				if(loc == locNew) {
					return 1.0d
				}
				else {
					return 0.0d
				}
			}
			else {
				return 0.5d
			}
		})
		
		network.addNode(un)
		
		un = new UncertaintyNode([tigerLocation.getPrimed(),action],[growlLocation],0,{ argVars ->
			int locNew = argVars["L'"]
			int act = argVars["ACT"]
			int growl = argVars["G"]
			
			if(act == 0 ) {
				if(growl == locNew) {
					return 0.65d
				}
				else {
					return 0.35d
				}
			}
			else {
				return 0.5d
			}
		})
		
		network.addNode(un)
		
		DecisionNode dn = new DecisionNode([action], 0, { argVars ->
			0.5d
		})
		
		network.addNode(dn)
		
		return network
		
	}
	
	def "bayesian network can be constructed"() {
		when:
			BayesianNetwork network = buildNetwork()
			
		then:
			println network
			
	}
	
	def "bayesian network can infer"() {
		when:
			BayesianNetwork network = buildNetwork()
			
			BayesianNetworkResult networkResult = new BayesianNetworkResult(network)
			
			AlgebraicDD beliefFn = new AlgebraicDD([tigerLocation],0, { argVars ->
				int loc = argVars["L"]
				return 0.5d
				
			})
			
			networkResult.probabilityIs(beliefFn)
			
			AlgebraicDD obsFn = new AlgebraicDD([growlLocation],0, { argVars ->
				int growl = argVars["G"]
				
				if(growl == 0) {
					return 1.0d
				}
				else {
					return 0.0d
				}
				
			})
			
			networkResult.probabilityIs(obsFn)
			
			AlgebraicDD actFn = new AlgebraicDD([action],0, { argVars ->
				int act = argVars["ACT"]
				
				if(act == 0) {
					return 1.0d
				}
				else {
					return 0.0d
				}
				
			})
			
			networkResult.decisionIs(actFn)
			
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
		then:
			println newNetwork
			
	}
	
	def "bayesian network can progress"() {
		when:
			BayesianNetwork network = buildNetwork()
			
			BayesianNetworkResult networkResult = new BayesianNetworkResult(network)
			
			AlgebraicDD beliefFn = new AlgebraicDD([tigerLocation],0, { argVars ->
				int loc = argVars["L"]
				return 0.5d
				
			})
			
			networkResult.probabilityIs(beliefFn)
			
			AlgebraicDD obsFn = new AlgebraicDD([growlLocation],0, { argVars ->
				int growl = argVars["G"]
				
				if(growl == 0) {
					return 0.0d
				}
				else {
					return 1.0d
				}
				
			})
			
			networkResult.probabilityIs(obsFn)
			
			AlgebraicDD actFn = new AlgebraicDD([action],0, { argVars ->
				int act = argVars["ACT"]
				
				if(act == 0) {
					return 1.0d
				}
				else {
					return 0.0d
				}
				
			})
			
			networkResult.decisionIs(actFn)
			
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
			def fns = newNetwork.getJointProbability([tigerLocation.getPrimed()])
			fns = fns.collect { AlgebraicDD dd ->
				dd.unprime()
			}
			
			networkResult = new BayesianNetworkResult(network)
			fns.each {
				networkResult.probabilityIs(it)
			}
			networkResult.probabilityIs(obsFn)
			
			networkResult.decisionIs(actFn)
			networkResult.solve()
			newNetwork = networkResult.buildNetwork()
		then:
			println fns
			println newNetwork
			
	}
}
