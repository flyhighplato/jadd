package masg.dd.representations.tables

import masg.dd.context.DDContext
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;
import spock.lang.Specification

class TableDDSpec extends Specification {
	DDVariable a1RowVar, a1ColVar
	//DDVariable a2RowVar, a2ColVar
	DDVariable wRowVar, wColVar
	int gridHeight = 5;
	int gridWidth = 5;
	
	DDVariableSpace space;
	def setup() {
		a1RowVar = new DDVariable("a1_row",gridHeight)
		a1ColVar = new DDVariable("a1_col",gridWidth)
		
		//a2RowVar = new DDVariable("a2_row",gridHeight)
		//a2ColVar = new DDVariable("a2_col",gridWidth)
		
		wRowVar = new DDVariable("w_row",gridHeight)
		wColVar = new DDVariable("w_col",gridWidth)
		
		DDContext.canonicalVariableOrdering = [a1RowVar,a1ColVar,wRowVar,wColVar];
	}
	
	def "can be populated"() {
		when:
			Closure<Double> c = { Map variables ->
					int w_row = variables["w_row"]
					int w_col = variables["w_col"]
					int a1_row = variables["a1_row"]
					int a1_col = variables["a1_col"]
					
					if(a1_col == w_col && a1_row == w_row)
						return new Double(10.0f)
					
					return new Double(-1.0f)
			}
			
			TableDD dd = new TableDD(new ArrayList<DDVariable>([a1RowVar,a1ColVar,wRowVar,wColVar]),c);
		then:
			println dd
	}
}
