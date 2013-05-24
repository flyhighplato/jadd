package masg.dd.dbn

import masg.dd.AlgebraicDD
import masg.dd.variables.DDVariable
import spock.lang.Specification
import masg.dd.context.DDContext

class BayesianNetworkSpec extends Specification {
	
	DDVariable varCoin = new DDVariable(0,"COIN",2)
	DDVariable varSide = new DDVariable(0,"SIDE",2)
	DDVariable varRule = new DDVariable(0,"RULE",2)
	DDVariable varWin = new DDVariable(0,"WIN",2)
	DDVariable varAct = new DDVariable(0,"ACT",2)
	
	
	def setup() {
		DDContext.setCanonicalVariableOrdering([varCoin,varSide,varRule, varWin,varAct])
	}
	
	private BayesianNetwork buildNetwork() {
		BayesianNetwork network = new BayesianNetwork()
		
		UncertaintyNode un = new UncertaintyNode([varCoin],[varSide,varRule],0,{ argVars ->
			int coin = argVars["COIN"]
			int side = argVars["SIDE"]
			int rule = argVars["RULE"]
			
			if(coin == 0) {
				if(side == 0) {
					if(rule == 0) {
						return 0.8d * 0.7d
					}
					else {
						return 0.8d * 0.3d
					}
				}
				else {
					if(rule == 0) {
						return 0.2d * 0.3d
					}
					else {
						return 0.2d * 0.7d
					}
				}
			}
			else if(coin == 1) {
				if(side == 0) {
					return 0.2d
				}
				else {
					return 0.8d
				}
			}
		})
		
		network.addNode(un)
		
		un = new UncertaintyNode([varSide,varRule,varAct],[varWin],0,{ argVars ->
			int side = argVars["SIDE"]
			int win = argVars["WIN"]
			
			int rule = argVars["RULE"]
			int act = argVars["ACT"]
			
			if(act == 0) {
				if(rule == 0) {
					if(side == 0 ) {
						if(win == 0) {
							return 0.1d
						}
						else {
							return 0.9d
						}
					}
					else {
						if(win == 0) {
							return 0.9d
						}
						else {
							return 0.1d
						}
					}
				}
				else {
					if(side == 1 ) {
						if(win == 0) {
							return 0.1d
						}
						else {
							return 0.9d
						}
					}
					else {
						if(win == 0) {
							return 0.9d
						}
						else {
							return 0.1d
						}
					}
				}
			} else {
				return 0.5d
			}
		})
		
		network.addNode(un)
		
		DecisionNode dn = new DecisionNode([varAct], 0, { argVars ->
			0.5d
		})
		
		network.addNode(dn)
		
		ValueNode vn = new ValueNode([varWin],0, { argVars ->
			int win = argVars["WIN"]
			
			if(win == 0)
				return 10.0d
			else
				return -1.0d
		})
		
		network.addNode(vn)
		
		return network
	}
	
	def "bayesian network can be constructed"() {
		when:
			BayesianNetwork network = buildNetwork()
			
		then:
			println network
			//network.nodes.size() == 4
			
	}
	
	def "bayesian network can infer by probability"() {
		when:
			BayesianNetwork network = buildNetwork()
			
			BayesianNetworkResult networkResult = new BayesianNetworkResult(network)
			
			AlgebraicDD testFn = new AlgebraicDD([varCoin],0, { argVars ->
				int coin = argVars["COIN"]
				
				if(coin == 0)
					return 0.95d
				else
					return 0.05d
			})
			
			networkResult.probabilityIs(testFn)
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
		then:
			println newNetwork
			//newNetwork.nodes.size() == 3
			
	}
	
	def "bayesian network can deduce by probability"() {
		when:
			BayesianNetwork network = buildNetwork()
			
			BayesianNetworkResult networkResult = new BayesianNetworkResult(network)
			
			AlgebraicDD testFn = new AlgebraicDD([varWin],0, { argVars ->
				int win = argVars["WIN"]
				
				if(win == 0)
					return 0.95d
				else 
					return 0.05d
			})
			
			networkResult.probabilityIs(testFn)
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
		then:
			println newNetwork
			//newNetwork.nodes.size() == 3
			
	}
	
	def "bayesian network can deduce by decision"() {
		when:
			BayesianNetwork network = buildNetwork()
			
			BayesianNetworkResult networkResult = new BayesianNetworkResult(network)
			
			AlgebraicDD testFn = new AlgebraicDD([varAct],0, { argVars ->
				int act = argVars["ACT"]
				
				if(act == 1)
					return 1.0d
				
				return 0.0d
			}).normalize()
			
			networkResult.decisionIs(testFn)
			networkResult.solve()
			
			BayesianNetwork newNetwork = networkResult.buildNetwork()
			
		then:
			println newNetwork
			//newNetwork.nodes.size() == 4
			
	}
}
