package masg.problem.tag

import java.util.List;
import java.util.Map;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.ProbDD;
import masg.dd.context.DDContext;
import masg.dd.pomdp.POMDP;
import masg.dd.variables.DDVariable;

class TagProblem {
	int gridWidth = 5, gridHeight = 5;
	
	DDVariable a1RowVar, a1ColVar
	DDVariable a2RowVar, a2ColVar
	DDVariable wRowVar, wColVar
	DDVariable a1RowPrimeVar, a1ColPrimeVar
	DDVariable a2RowPrimeVar, a2ColPrimeVar
	DDVariable wRowPrimeVar, wColPrimeVar
	DDVariable wPresenceObsVar, a1RowObsVar, a1ColObsVar
	DDVariable actVar
	
	Closure initBeliefClosure = {
		return 1.0f/Math.pow(gridWidth*gridHeight,3.0f)
	}
	
	Closure rewardFunctionClosure = { Map variables ->
			int w_row = variables["w_row"]
			int w_col = variables["w_col"]
			int a1_row = variables["a1_row"]
			int a1_col = variables["a1_col"]
			
			if(a1_col == w_col && a1_row == w_row)
				return 10.0f
			
			return -1.0f
	}
	
	List transnFunctionClosures = []
	List obervnFunctionClosures = []
	
	POMDP p;
	
	public TagProblem() {
		actVar = new DDVariable("act",4)
		
		wPresenceObsVar = new DDVariable("w_pres",2)
		a1RowObsVar = new DDVariable("a1_row_loc",5)
		a1ColObsVar = new DDVariable("a1_col_loc",5)
		
		a1RowVar = new DDVariable("a1_row",gridHeight)
		a1ColVar = new DDVariable("a1_col",gridWidth)
		
		a2RowVar = new DDVariable("a2_row",gridHeight)
		a2ColVar = new DDVariable("a2_col",gridWidth)
		
		wRowVar = new DDVariable("w_row",gridHeight)
		wColVar = new DDVariable("w_col",gridWidth)
		
		a1RowPrimeVar = new DDVariable((a1RowVar.name + "'").toString(), a1RowVar.getValueCount())
		a1ColPrimeVar = new DDVariable((a1ColVar.name + "'").toString(), a1ColVar.getValueCount())
		a2RowPrimeVar = new DDVariable((a2RowVar.name + "'").toString(), a2RowVar.getValueCount())
		a2ColPrimeVar = new DDVariable((a2ColVar.name + "'").toString(), a2ColVar.getValueCount())
		wRowPrimeVar = new DDVariable((wRowVar.name + "'").toString(), wRowVar.getValueCount())
		wColPrimeVar = new DDVariable((wColVar.name + "'").toString(), wColVar.getValueCount())
		
		DDContext.canonicalVariableOrdering = [actVar,wPresenceObsVar,a1RowObsVar,a1ColObsVar,a1RowVar,a1ColVar,a2RowVar,a2ColVar,wRowVar,wColVar,a1RowPrimeVar,a1ColPrimeVar,a2RowPrimeVar,a2ColPrimeVar,wRowPrimeVar,wColPrimeVar]
		
		println "Building initial belief..."
		ProbDD initBelief = ProbDD.buildProbability([a1RowVar,a1ColVar,a2RowVar,a2ColVar,wRowVar,wColVar], initBeliefClosure) 
	
		def vars = []
		def closures = []
		
		vars << [[a2RowVar,a2ColVar,actVar],[a2RowPrimeVar,a2ColPrimeVar]]
		closures << { Map variables ->
				int row = variables["a2_row"]
				int col = variables["a2_col"]
				int act = variables["act"]
				int rowPrime = variables["a2_row'"]
				int colPrime = variables["a2_col'"]
				
				int moveDistance = Math.abs(rowPrime - row) + Math.abs(colPrime - col)
				
				if(moveDistance<2) {
					Map locProb = [:]
					Map possMoves = getPossibleMoves(col,row,["N","S","E","W"])
					possMoves.each{a,loc ->
						if(locProb[loc])
							locProb[loc] += 1.0f/possMoves.size()
						else
							locProb[loc] = 1.0f/possMoves.size()
					}
					
					return locProb.get([colPrime,rowPrime])?locProb.get([colPrime,rowPrime]):0.0f
				}
				
				return 0.0f;
			}
		
		vars << [[a1ColVar,actVar],[a1ColPrimeVar]]
		closures << { Map variables ->
				int col = variables["a1_col"]
				int act = variables["act"]
				int colPrime = variables["a1_col'"]
				assert act<4
				
				
				//E
				if(act==2){
					if(col<gridWidth-1 && colPrime==col+1)
						return 1.0f;
					else if(col==gridWidth-1 && col==colPrime)
						return 1.0f;
				}
				//W
				else if(act==3){
					if(col>0 && colPrime==col-1)
						return 1.0f;
					else if(col==0 && col==colPrime)
						return 1.0f;
				}
				else if(act!=2 && act!=3 && col==colPrime)
					return 1.0f;
				
				return 0.0f;
			}
		
		vars << [[a1RowVar,actVar],[a1RowPrimeVar]]
		closures << { Map variables ->
				int row = variables["a1_row"]
				int act = variables["act"]
				int rowPrime = variables["a1_row'"]
				assert act<4
				
				//N
				if(act==0){
					if(row<gridHeight-1 && rowPrime==row+1)
						return 1.0f;
					else if(row==gridHeight-1 && rowPrime==row)
						return 1.0f;
				}
				//S
				else if(act==1){
					if(row>0 && rowPrime==row-1)
						return 1.0f;
					else if(row==0 && rowPrime==row)
						return 1.0f;
				}
				else if(act!=0 && act!=1 && row==rowPrime)
					return 1.0f;
				
				return 0.0f;
			}
		
		vars << [[wRowVar,wColVar,actVar],[wRowPrimeVar,wColPrimeVar]]
		closures << { Map variables ->
				int row = variables["w_row"]
				int col = variables["w_col"]
				int act = variables["act"]
				int rowPrime = variables["w_row'"]
				int colPrime = variables["w_col'"]
				
				int moveDistance = Math.abs(rowPrime - row) + Math.abs(colPrime - col)
				
				if(moveDistance<2) {
					Map locProb = [:]
					Map possMoves = getPossibleMoves(col,row,["N","S","E","W","STAY"])
					possMoves.each{a,loc ->
						if(locProb[loc])
							locProb[loc] += 1.0f/possMoves.size()
						else
							locProb[loc] = 1.0f/possMoves.size()
					}
				
				return locProb.get([colPrime,rowPrime])?locProb.get([colPrime,rowPrime]):0.0f
				}
				
				return 0.0f;
			}
		println "Building transition function..."
		CondProbDD transFn = CondProbDD.build(vars,closures)
		
		transnFunctionClosures.addAll(closures);
		
		vars = []
		closures = []
		
		vars << [[wRowPrimeVar,wColPrimeVar,a1ColPrimeVar,a1RowPrimeVar],[wPresenceObsVar]]
		closures << { Map variables ->
				int w_row = variables["w_row'"]
				int w_col = variables["w_col'"]
				int a1_row = variables["a1_row'"]
				int a1_col = variables["a1_col'"]
				int w_pres = variables["w_pres"]
				
				int distance = Math.abs(w_row - a1_row) + Math.abs(w_col - a1_col)
				
				if((distance<2 && w_pres==1) || (distance>=2 && w_pres==0)) {
					return 1.0f
				}
				
				return 0.0f
			}
		
		vars << [[a1ColPrimeVar],[a1ColObsVar]]
		closures <<  { Map variables ->
				
			int a1_col = variables["a1_col'"]
			int a1_col_loc = variables["a1_col_loc"]
			
			if(a1_col == a1_col_loc)
				return 1.0f;
			
			return 0.0f;
		}
		
		
		vars << [[a1RowPrimeVar],[a1RowObsVar]]
		closures << { Map variables ->
				
				int a1_row = variables["a1_row'"]
				int a1_row_loc = variables["a1_row_loc"]
				
				if(a1_row == a1_row_loc)
					return 1.0f;
				
				return 0.0f;
			}
		
		println "Building observation function..."
		CondProbDD obsFn = CondProbDD.build(vars, closures)
		obervnFunctionClosures.addAll(closures);
		
		AlgebraicDD rewFn = AlgebraicDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],rewardFunctionClosure) 
		
		p = new POMDP(initBelief,rewFn,transFn,obsFn,[a1RowVar,a1ColVar,a2RowVar,a2ColVar,wRowVar,wColVar],[wPresenceObsVar,a1RowObsVar,a1ColObsVar],[actVar]);
	}
	
	public POMDP getPOMDP() {
		return p;
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
