package masg.dd.representations.tables

import masg.dd.context.DDContext
import masg.dd.representations.dag.ImmutableDDElement
import masg.dd.representations.dag.ImmutableDDLeaf
import masg.dd.representations.dag.ImmutableDDNode
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
			
			TableDD tableDD = new TableDD(new ArrayList<DDVariable>([a1RowVar,a1ColVar,wRowVar,wColVar]),c);
			ImmutableDDElement immColl = null;
			if(tableDD.getRootNode() instanceof TableDDNode) {
				immColl = new ImmutableDDNode((TableDDNode)tableDD.getRootNode(), null);
			}
			else if(tableDD.getRootNode() instanceof TableDDLeaf) {
				immColl = new ImmutableDDLeaf((TableDDLeaf)tableDD.getRootNode(), null);
			}
		then:
			println tableDD
			println immColl
	}
}
