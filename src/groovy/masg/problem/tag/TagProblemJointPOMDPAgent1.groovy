package masg.problem.tag

import java.util.List;
import java.util.Map;

import masg.dd.AlgebraicDD;
import masg.dd.FactoredCondProbDD;
import masg.dd.ProbDD;
import masg.dd.context.DDContext;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;
import masg.problem.builder.JointPOMDPProblemBuilder
import masg.problem.builder.POMDPProblemBuilder;

class TagProblemJointPOMDPAgent1 implements TagProblemModel{
	int gridWidth = 5, gridHeight = 5;
	
	POMDP p = null;
	protected JointPOMDPProblemBuilder builder;
	
	boolean directionalObservation = false;
	
	public TagProblemJointPOMDPAgent1(Integer scope = null) {
		
		builder = new JointPOMDPProblemBuilder();
		
		if(scope!=null)
			builder.scope = scope
		
		builder.addAction("act1", 4);
		builder.addActionOther("act2", 4);
		
		builder.addObservation("a1_w_pres",2);
		
		
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
		
		builder.setRewardFunction(["w_row","w_col","a1_row","a1_col","a2_row","a2_col"], ["act1"], ["act2"]) { Map variables ->
			int w_row = variables["w_row"]
			int w_col = variables["w_col"]
			int a1_row = variables["a1_row"]
			int a1_col = variables["a1_col"]	
			int a2_row = variables["a2_row"]
			int a2_col = variables["a2_col"]
			
			if(a1_col == w_col && a1_row == w_row)
				return 10.0d
			else if(a2_col == w_col && a2_row == w_row)
				return 10.0d
			
			return -1.0d
		}
		
		builder.addTransition(["a1_col"], ["act1"], ["act2"], ["a1_col'"]) {Map variables ->
				int col = variables["a1_col"]
				int act = variables["act1"]
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
		
		builder.addTransition(["a2_col"], ["act1"], ["act2"], ["a2_col'"]) {Map variables ->
			int col = variables["a2_col"]
			int act = variables["act2"]
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
		
		builder.addTransition(["a1_row"], ["act1"], ["act2"], ["a1_row'"]) {Map variables ->
			int row = variables["a1_row"]
			int act = variables["act1"]
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
		
		builder.addTransition(["a2_row"], ["act1"], ["act2"], ["a2_row'"]) {Map variables ->
			int row = variables["a2_row"]
			int act = variables["act2"]
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
		
		builder.addTransition(["w_row","w_col"], ["act1"], ["act2"], ["w_row'","w_col'"]) {
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
		
		builder.addObservation(["w_row'","w_col'","a1_row'","a1_col'"], ["act1"], ["act2"],["a1_w_pres"]) { Map variables ->
			int w_row = variables["w_row'"]
			int w_col = variables["w_col'"]
			int a1_row = variables["a1_row'"]
			int a1_col = variables["a1_col'"]
			int w_pres = variables["a1_w_pres"]
			
			int distance = Math.abs(w_row - a1_row) + Math.abs(w_col - a1_col)
			
			if((distance<2 && w_pres==1) || (distance>=2 && w_pres==0)) {
				return 1.0d
			}
			
			return 0.0d
		}
		
		builder.addObservation(["a1_col'"], ["act1"], ["act2"], ["a1_col_loc"]) { Map variables ->
			int a1_col = variables["a1_col'"]
			int a1_col_loc = variables["a1_col_loc"]
			
			if(a1_col == a1_col_loc)
				return 1.0d;
			
			return 0.0d;
		}
		
		builder.addObservation(["a1_row'"], ["act1"], ["act2"], ["a1_row_loc"]) { Map variables ->
				
			int a1_row = variables["a1_row'"]
			int a1_row_loc = variables["a1_row_loc"]
			
			if(a1_row == a1_row_loc)
				return 1.0d;
			
			return 0.0d;
		}
		
		
	}
	
	public POMDP getPOMDP() {
		if(p==null) {
			p = builder.buildPOMDP();
		}
		return p;
	}
	
	public POMDPProblemBuilder getBuilder() {
		return builder;
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
