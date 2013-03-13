package masg.problem.tag

import masg.dd.pomdp.AbstractPOMDP
import masg.dd.pomdp.IPOMDP
import masg.problem.builder.IPOMDPProblemBuilder
import masg.problem.builder.POMDPProblemBuilder

class TagProblemIPOMDP implements TagProblemModel {
	int gridWidth = 5, gridHeight = 5;
	
	IPOMDP p;

	ArrayList<AbstractPOMDP> otherAgents = new ArrayList<AbstractPOMDP>();
	
	public TagProblemIPOMDP() {
		IPOMDPProblemBuilder builder = new IPOMDPProblemBuilder();
		
		builder.addAction("act", 4);
		
		String otherAgentName = "agent2"
		POMDPProblemBuilder builderOtherAgent = new TagProblemPOMDP(1).getBuilder();
		builderOtherAgent.setScope(1);
		
		otherAgents.add(builderOtherAgent.buildPOMDP())
		builder.addAgentType(otherAgentName, builderOtherAgent, null);

		builder.addObservation("w_pres", 2);
		
		
		builder.addObservation("a1_row_loc",gridHeight);
		builder.addObservation("a1_col_loc",gridWidth);
		
		builder.addState("a1_row", gridHeight);
		builder.addState("a1_col", gridWidth);
		
		builder.addState("a2_row", gridHeight);
		builder.addState("a2_col", gridWidth);
		
		builder.addState("w_row", gridHeight);
		builder.addState("w_col", gridWidth);
		
		builder.setInitialBelief {
			return 1.0d/Math.pow(gridWidth*gridHeight,3.0d)
		}
		
		builder.setRewardFunction(["w_row","w_col","a1_row","a1_col","a2_row","a2_col"], ["act"], [:]) { Map variables ->
			int w_row = variables["w_row"]
			int w_col = variables["w_col"]
			int a1_row = variables["a1_row"]
			int a1_col = variables["a1_col"]
			int a2_row = variables["a2_row"]
			int a2_col = variables["a2_col"]
			
			if((a1_col == w_col && a1_row == w_row) || (a2_col == w_col && a2_row == w_row))
				return 10.0d
			
			return -1.0d
		}
		
		builder.addTransitionFunction(["a1_col"], ["act"], [:], ["a1_col'"]) {Map variables ->
			int col = variables["a1_col"]
			int act = variables["act"]
			int colPrime = variables["a1_col'"]
			assert act<4
			
			
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
		
		builder.addTransitionFunction(["a1_row"], ["act"], [:], ["a1_row'"]) {Map variables ->
			int row = variables["a1_row"]
			int act = variables["act"]
			int rowPrime = variables["a1_row'"]
			assert act<4
			
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
		
		builder.addTransitionFunction(["a2_col"], [],  [agent2 : ["act"] ], ["a2_col'"]) {Map variables ->
			int col = variables["a2_col"]
			int act = variables[1]["act"]
			int colPrime = variables["a2_col'"]
			assert act<4
			
			
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
		
		builder.addTransitionFunction(["a2_row"], [],  [agent2 : ["act"] ], ["a2_row'"]) {Map variables ->
			int row = variables["a2_row"]
			int act = variables[1]["act"]
			int rowPrime = variables["a2_row'"]
			assert act<4
			
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
		
		builder.addTransitionFunction(["w_row","w_col"], [], [:], ["w_row'","w_col'"]) {
			Map variables ->
			int row = variables["w_row"]
			int col = variables["w_col"]
			int rowPrime = variables["w_row'"]
			int colPrime = variables["w_col'"]
			
			int moveDistance = Math.abs(rowPrime - row) + Math.abs(colPrime - col)
			
			if(moveDistance<2) {
				Map locProb = [:]
				Map possMoves = getPossibleMoves(col,row,["N","S","E","W","STAY"])
				possMoves.each{a,loc ->
					if(locProb[loc])
						locProb[loc] += 1.0d/possMoves.size()
					else
						locProb[loc] = 1.0d/possMoves.size()
				}
			
			return locProb.get([colPrime,rowPrime])?locProb.get([colPrime,rowPrime]):0.0d
			}
			
			return 0.0d;
		}
		
		builder.addObservationFunction(["w_row'","w_col'","a1_row'","a1_col'"], [], [:], ["w_pres"]) { Map variables ->
			int w_row = variables["w_row'"]
			int w_col = variables["w_col'"]
			int a1_row = variables["a1_row'"]
			int a1_col = variables["a1_col'"]
			int w_pres = variables["w_pres"]
			
			int distance = Math.abs(w_row - a1_row) + Math.abs(w_col - a1_col)
			
			if((distance<2 && w_pres==1) || (distance>=2 && w_pres==0)) {
				return 1.0d
			}
			
			return 0.0d
		}
		
		builder.addObservationFunction(["a1_col'"], [], [:], ["a1_col_loc"]) { Map variables ->
			int a1_col = variables["a1_col'"]
			int a1_col_loc = variables["a1_col_loc"]
			
			if(a1_col == a1_col_loc)
				return 1.0d;
			
			return 0.0d;
		}
		
		builder.addObservationFunction(["a1_row'"], [], [:], ["a1_row_loc"]) { Map variables ->	
			int a1_row = variables["a1_row'"]
			int a1_row_loc = variables["a1_row_loc"]
			
			if(a1_row == a1_row_loc)
				return 1.0d;
			
			return 0.0d;
		}
		
		p = builder.buildIPOMDP();
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

	@Override
	public IPOMDP getIPOMDP() {
		return p;
	}
}
