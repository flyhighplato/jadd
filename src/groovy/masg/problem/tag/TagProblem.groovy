package masg.problem.tag

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import masg.dd.pomdp.POMDP;
import masg.dd.vars.DDVariable;

class TagProblem {
	
	List<DDVariable> states = []
	List<DDVariable> acts = []
	List<DDVariable> obs = []
	
	POMDP p
	
	List<List<DDVariable>> transFnVars = [], obsFnVars = []
	List<Closure<Double>> transFns = [], obsFns = []
	Closure<Double> initBelief
	Closure<Double> rewFn
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
		acts = [actVar]
		
		wPresenceObsVar = new DDVariable("w_pres",2)
		a1RowObsVar = new DDVariable("a1_row_loc",5)
		a1ColObsVar = new DDVariable("a1_col_loc",5)
		obs = [wPresenceObsVar, a1RowObsVar, a1ColObsVar]
		
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
		
		states << a1RowVar
		states << a1ColVar
		states << a2RowVar
		states << a2ColVar
		states << wRowVar
		states << wColVar
		
		
		
		initBelief = {
			return 1.0f/Math.pow(gridWidth*gridHeight,3.0f)
		}
		
		transFnVars << [[a1RowVar, actVar], [a1RowPrimeVar]]
		transFns << { Map variables ->
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
		
		transFnVars << [[a1ColVar, actVar], [a1ColPrimeVar]]
		transFns << { Map variables ->
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
		
		transFnVars << [[a2RowVar,a2ColVar,actVar], [a2RowPrimeVar,a2ColPrimeVar]]
		transFns << { Map variables ->
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
		
		transFnVars << [[wRowVar,wColVar,actVar], [wRowPrimeVar,wColPrimeVar]]
		transFns << { Map variables ->
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
		
		obsFnVars << [[wRowPrimeVar,wColPrimeVar,a1ColPrimeVar,a1RowPrimeVar,actVar],[wPresenceObsVar]]
		
		obsFns << { Map variables ->
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
		
		obsFnVars << [[a1ColPrimeVar,actVar],[a1ColObsVar]]
		obsFns << { Map variables ->
				
			int a1_col = variables["a1_col'"]
			int a1_col_loc = variables["a1_col_loc"]
			
			if(a1_col == a1_col_loc)
				return 1.0f;
			
			return 0.0f;
		}
		
		obsFnVars << [[a1RowPrimeVar,actVar],[a1RowObsVar]]
		obsFns << { Map variables ->
				
			int a1_row = variables["a1_row'"]
			int a1_row_loc = variables["a1_row_loc"]
			
			if(a1_row == a1_row_loc)
				return 1.0f;
			
			return 0.0f;
		}
		
		
		rewFn = { Map variables ->
			int w_row = variables["w_row"]
			int w_col = variables["w_col"]
			int a1_row = variables["a1_row"]
			int a1_col = variables["a1_col"]
			
			if(a1_col == w_col && a1_row == w_row)
				return 10.0f
			
			return 0.0f
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
	
	public POMDP getPOMDP() {
		if(!p)
			p = new POMDP(obs, acts, states, initBelief, transFnVars, transFns, obsFnVars, obsFns, rewFn)
		return p
	}
}
