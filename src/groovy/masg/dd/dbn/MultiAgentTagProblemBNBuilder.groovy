package masg.dd.dbn

import java.util.List;
import java.util.Map;

import masg.dd.AlgebraicDD
import masg.dd.CondProbDD
import masg.dd.context.DDContext;
import masg.dd.variables.DDVariable


class MultiAgentTagProblemBNBuilder {
	int gridHeight = 5, gridWidth = 5
	
	public obsFns = [:], transFns = [:], rewFns = [:], actions = [:], observations = [:], states = []
	
	public beliefNetworks = []
	public trackingNetworks = [:]
	
	public BayesianNetwork fullNetwork = new BayesianNetwork()
	
	public MultiAgentTagProblemBNBuilder(int numAgents) {
		
		def agtVars = [:]
		
		def vars = []
		
		int totalAgents = numAgents + 1
		int wumpusAgentIx = numAgents
		
		totalAgents.times { int thisAgentIx ->
			
			String myPrefix = "agt${thisAgentIx}"
			
			def obsVars = []
			def actVars = []
			def stateVars = []
			
			if(thisAgentIx != wumpusAgentIx) {
				//Observations
				obsVars << new DDVariable(0,"${myPrefix}_wpres",2)
				obsVars << new DDVariable(0,"${myPrefix}_rowloc",gridHeight)
				obsVars << new DDVariable(0,"${myPrefix}_colloc",gridWidth)
				
				//Actions
				actVars << new DDVariable(0,"${myPrefix}_act",4)
				
			}
			else {
				//Actions
				actVars << new DDVariable(0,"${myPrefix}_act",5)
			}
			
			//States
			stateVars << new DDVariable(0,"${myPrefix}_row",gridHeight)
			stateVars << new DDVariable(0,"${myPrefix}_col",gridWidth)
			
			//States prime
			def stateVarsPrime = stateVars.collect { it.getPrimed() }
			
			vars += obsVars + actVars + stateVars + stateVarsPrime
			
			states += stateVars
			
			actions[thisAgentIx] = actVars
			
			
			observations[thisAgentIx] = obsVars
			
			fullNetwork.addNode(new DecisionNode(actVars,0))
			
			
		}
		
		
		DDContext.setCanonicalVariableOrdering(vars);
		
		numAgents.times { int thisAgentIx ->
			
			String myPrefix = "agt${thisAgentIx}"
			String wumpusPrefix = "agt${wumpusAgentIx}"
			
			def fnVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v -> 
				["${wumpusPrefix}_row","${wumpusPrefix}_col","${myPrefix}_row","${myPrefix}_col"].find { it == v.name}
			}
			
			Closure rewClosure = { Map variables ->
				int w_row = variables["${wumpusPrefix}_row"]
				int w_col = variables["${wumpusPrefix}_col"]
				int a1_row = variables["${myPrefix}_row"]
				int a1_col = variables["${myPrefix}_col"]
				
				if(a1_col == w_col && a1_row == w_row)
					return 10.0d
				
				return -1.0d
			}
			
			List<AlgebraicDD> rewFns = [new AlgebraicDD(fnVars,0,rewClosure)]
			
			this.rewFns[thisAgentIx] = rewFns
		}
		
		List<CondProbDD> transFns = []
		
		totalAgents.times { int thisAgentIx ->
			
			String myPrefix = "agt${thisAgentIx}"
			Closure colTransnClosure
			Closure rowTransnClosure
			
			
			def condVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_act","${myPrefix}_col"].find { it == v.name}
			}
			
			def postVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_col'"].find { it == v.name}
			}
			
			colTransnClosure = { Map variables ->
				int col = variables["${myPrefix}_col"]
				int act = variables["${myPrefix}_act"]
				int colPrime = variables["${myPrefix}_col'"]
				//assert act<4
				
				
				//E
				if(act==2){
					if(col<gridWidth-1 && colPrime==col+1)
						return 1.0d;
					else if(col==gridWidth-1 && col==colPrime)
						return 1.0d;
				}
				//W
				else if(act==3){
					if(col>0 && colPrime==col-1)
						return 1.0d;
					else if(col==0 && col==colPrime)
						return 1.0d;
				}
				else if(act!=2 && act!=3 && col==colPrime)
					return 1.0d;
				
				return 0.0d;
			}
			
			transFns << new CondProbDD(condVars,postVars,0,colTransnClosure)
			
			condVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_act","${myPrefix}_row"].find { it == v.name}
			}
			postVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_row'"].find { it == v.name}
			}
			
			rowTransnClosure = {Map variables ->
				int row = variables["${myPrefix}_row"]
				int act = variables["${myPrefix}_act"]
				int rowPrime = variables["${myPrefix}_row'"]
				
				//N
				if(act==0){
					if(row<gridHeight-1 && rowPrime==row+1)
						return 1.0d;
					else if(row==gridHeight-1 && rowPrime==row)
						return 1.0d;
				}
				//S
				else if(act==1){
					if(row>0 && rowPrime==row-1)
						return 1.0d;
					else if(row==0 && rowPrime==row)
						return 1.0d;
				}
				else if(act!=0 && act!=1 && row==rowPrime)
					return 1.0d;
				
				return 0.0d;
			}
			
			transFns << new CondProbDD(condVars,postVars,0,rowTransnClosure)
			
			
		}
		transFns.each { condFn ->
			fullNetwork.addNode(new UncertaintyNode(condFn.conditionalVariables,condFn.posteriorVariables, condFn.getFunction()))
		}
		
		numAgents.times { int thisAgentIx ->
			this.transFns[thisAgentIx] = transFns
		}
		
		numAgents.times { int thisAgentIx ->
			
			List<AlgebraicDD> obsFns = []
			
			String myPrefix = "agt${thisAgentIx}"
			String wumpPrefix = "agt${wumpusAgentIx}"
			
			def condVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_col'","${wumpPrefix}_col'","${myPrefix}_row'","${wumpPrefix}_row'"].find { it == v.name}
			}
			
			def postVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_wpres"].find { it == v.name}
			}
			
			Closure wumpObsClosure = { Map variables ->
				int w_row = variables["${wumpPrefix}_row'"]
				int w_col = variables["${wumpPrefix}_col'"]
				int a1_row = variables["${myPrefix}_row'"]
				int a1_col = variables["${myPrefix}_col'"]
				int w_pres = variables["${myPrefix}_wpres"]
				
				int distance = Math.abs(w_row - a1_row) + Math.abs(w_col - a1_col)
				
				if((distance<2 && w_pres==1) || (distance>=2 && w_pres==0)) {
					return 1.0d
				}
				
				return 0.0d
			}
			
			obsFns << new CondProbDD(condVars,postVars,0,wumpObsClosure)
			
			condVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_col'"].find { it == v.name}
			}
			
			postVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_colloc"].find { it == v.name}
			}
			
			Closure colObsClosure = { Map variables ->
				int a1_col = variables["${myPrefix}_col'"]
				int a1_col_loc = variables["${myPrefix}_colloc"]
				
				if(a1_col == a1_col_loc)
					return 1.0d;
				
				return 0.0d;
			}
			
			obsFns << new CondProbDD(condVars,postVars,0,colObsClosure)
			
			condVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_row'"].find { it == v.name}
			}
			
			postVars = DDContext.canonicalVariableOrdering.findAll { DDVariable v ->
				["${myPrefix}_rowloc"].find { it == v.name}
			}
			
			Closure rowObsClosure = { Map variables ->
				int row = variables["${myPrefix}_row'"]
				int row_loc = variables["${myPrefix}_rowloc"]
				
				if(row == row_loc)
					return 1.0d;
				
				return 0.0d;
			}
			
			obsFns << new CondProbDD(condVars,postVars,0,rowObsClosure)
			
			this.obsFns[thisAgentIx] = obsFns
		}
		
		numAgents.times { int thisAgentIx ->
			BayesianNetwork beliefNetwork = new BayesianNetwork()
			this.actions.each { agentIx, acts ->
				beliefNetwork.addNode(new DecisionNode(acts,0))
			}
			
			this.transFns[thisAgentIx].each { CondProbDD condFn ->
				beliefNetwork.addNode(new UncertaintyNode(condFn.conditionalVariables,condFn.posteriorVariables, condFn.getFunction()))
			}
			
			this.obsFns[thisAgentIx].each { CondProbDD condFn ->
				beliefNetwork.addNode(new UncertaintyNode(condFn.conditionalVariables,condFn.posteriorVariables, condFn.getFunction()))
				
				fullNetwork.addNode(new UncertaintyNode(condFn.conditionalVariables,condFn.posteriorVariables, condFn.getFunction()))
			}
			
			this.beliefNetworks << beliefNetwork
			
			def agtTrackingNetworks = []
			
			BayesianNetwork obsTrackNetwork = new BayesianNetwork()
			
			this.transFns[thisAgentIx].each { CondProbDD condFn ->
				obsTrackNetwork.addNode(new UncertaintyNode(condFn.conditionalVariables,condFn.posteriorVariables, condFn.getFunction()))
			}
			
			this.actions.each { agentIx, acts ->
				obsTrackNetwork.addNode(new DecisionNode(acts,0))
			}
			
			numAgents.times { int otherAgentIx ->
				
				//if(otherAgentIx != thisAgentIx) {
					
					this.obsFns[otherAgentIx].each { CondProbDD condFn ->
						obsTrackNetwork.addNode(new UncertaintyNode(condFn.conditionalVariables,condFn.posteriorVariables, condFn.getFunction()))
					}
					
					
				//}
			}
			agtTrackingNetworks << obsTrackNetwork
			this.trackingNetworks[thisAgentIx] = agtTrackingNetworks
			
		}
		
	}
	
	
	Map getPossibleMoves(int col, int row, List acts) {
		
		Map possMoves = [:]
		acts.each { a ->
			switch(a) {
				case "N":
					if(row < gridHeight-1)
						possMoves[a] = [col, row + 1]
					else
						possMoves[a] = [col, row]
					break;
				case "S":
					if(row > 0)
						possMoves[a] = [col, row - 1]
					else
						possMoves[a] = [col, row]
					break;
				case "E":
					if(col < gridWidth-1)
						possMoves[a] = [col+1, row]
					else
						possMoves[a] = [col, row]
					break;
				case "W":
					if(col > 0)
						possMoves[a] = [col-1, row]
					else
						possMoves[a] = [col, row]
					break;
				case "STAY":
					possMoves[a] = [col, row]
					break;
			}
		}
		
		return possMoves;
	}
}
