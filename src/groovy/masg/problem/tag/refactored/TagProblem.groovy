package masg.problem.tag.refactored

import java.util.List;
import java.util.Map;

import masg.dd.refactored.AlgebraicDD
import masg.dd.refactored.CondProbDD
import masg.dd.vars.DDVariable

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
		
		AlgebraicDD initBelief = AlgebraicDD.build([a1RowVar,a1ColVar,a2RowVar,a2ColVar,wRowVar,wColVar]) {
			return 1.0f/Math.pow(gridWidth*gridHeight,3.0f)
		};
	
		CondProbDD transFn = CondProbDD.build(
			[a1RowVar,a1ColVar,a2RowVar,a2ColVar,wRowVar,wColVar,actVar],[a1RowPrimeVar,a1ColPrimeVar,a2RowPrimeVar,a2ColPrimeVar,wRowPrimeVar,wColPrimeVar],
			{ Map variables ->
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
			},
			{ Map variables ->
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
			},
			{ Map variables ->
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
			},
			{ Map variables ->
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
		)
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
