package masg.dd.representations.serialization

import groovy.lang.Closure;
import masg.dd.context.DDContext;
import masg.dd.representation.DDElement
import masg.dd.representation.DDInfo
import masg.dd.representation.builder.DDBuilder
import masg.dd.representation.serialization.DDElementReader
import masg.dd.representation.serialization.DDElementWriter
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;
import spock.lang.Shared;
import spock.lang.Specification

class DDElementSerializationSpec extends Specification {
	DDVariable a1RowVar, a1ColVar
	DDVariable a1RowPrimeVar, a1ColPrimeVar
	
	DDVariable wRowVar, wColVar
	DDVariable wRowPrimeVar, wColPrimeVar
	
	int gridHeight = 5;
	int gridWidth = 5;
	
	DDElement dd
	
	@Shared
	Closure<Double> c = { Map variables ->
			int w_row = variables["w_row"]
			int w_col = variables["w_col"]
			int a1_row = variables["a1_row"]
			int a1_col = variables["a1_col"]
			
			if(a1_col == w_col && a1_row == w_row)
				return 10.0d
			
			return -1.0d
	}
	
	DDVariableSpace space;
	def setup() {
		a1RowVar = new DDVariable("a1_row",gridHeight)
		a1ColVar = new DDVariable("a1_col",gridWidth)
		
		wRowVar = new DDVariable("w_row",gridHeight)
		wColVar = new DDVariable("w_col",gridWidth)
		
		a1RowPrimeVar = new DDVariable((a1RowVar.name + "'").toString(), a1RowVar.getValueCount())
		a1ColPrimeVar = new DDVariable((a1ColVar.name + "'").toString(), a1ColVar.getValueCount())
		wRowPrimeVar = new DDVariable((wRowVar.name + "'").toString(), wRowVar.getValueCount())
		wColPrimeVar = new DDVariable((wColVar.name + "'").toString(), wColVar.getValueCount())
		
		DDContext.canonicalVariableOrdering = [a1RowVar,a1ColVar,wRowVar,wColVar,a1RowPrimeVar,a1ColPrimeVar,wRowPrimeVar,wColPrimeVar];
		
		dd = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c).getRootNode()
	}
	
	def "DD can be written"() {
		when:
			DDElementWriter writer = new DDElementWriter(dd);
			BufferedWriter w = new BufferedWriter(new PrintWriter(System.out))
			
		then:
			writer.write(w);
			w.flush();
	}
	
	def "DD can be read"() {
		when:
			DDElementWriter writer = new DDElementWriter(dd);
			StringWriter sw = new StringWriter()
			BufferedWriter w = new BufferedWriter(sw)
			writer.write(w);
			w.flush();
			BufferedReader r = new BufferedReader(new StringReader(sw.toString()))
			DDElementReader reader = new DDElementReader(r);
		then:
			DDVariableSpace currVarSpace = new DDVariableSpace(new ArrayList<DDVariable>(dd.getVariables()));
			DDElement ddNew = reader.read()
			currVarSpace.each { pt ->
				assert ddNew.getValue(pt) != null
				assert dd.getValue(pt) == ddNew.getValue(pt)
			}
			
	}
}
