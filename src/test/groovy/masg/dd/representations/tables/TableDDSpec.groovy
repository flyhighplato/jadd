package masg.dd.representations.tables

import masg.dd.context.DDContext
import masg.dd.operations.ConstantMultiplicationOperation
import masg.dd.operations.MultiplicationOperation
import masg.dd.representation.BaseDDElement;
import masg.dd.representation.DDElement
import masg.dd.representation.DDInfo
import masg.dd.representation.DDLeaf;
import masg.dd.representation.DDNode;
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable;
import masg.dd.variables.DDVariableSpace;
import spock.lang.Specification
import spock.lang.Shared

class TableDDSpec extends Specification {
	DDVariable a1RowVar, a1ColVar
	DDVariable a1RowPrimeVar, a1ColPrimeVar
	
	DDVariable wRowVar, wColVar
	DDVariable wRowPrimeVar, wColPrimeVar
	
	int gridHeight = 5;
	int gridWidth = 5;
	
	
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
	}
	
	def "collection can be built from closure"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
		then:
			println tableDD
			
	}
	
	def "unary operations work"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
			ConstantMultiplicationOperation multOp = new ConstantMultiplicationOperation(5.0f);
			
			tableDD = DDBuilder.build([a1RowVar,a1ColVar,wRowVar,wColVar],tableDD.getRootNode(),multOp) 

		then:
			println tableDD
	}
	
	def "binary operations work"() {
		when:
			DDBuilder tableDD1 = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			DDBuilder tableDD2 = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
			MultiplicationOperation multOp = new MultiplicationOperation();
			
			
			DDNode node = DDBuilder.build([a1RowVar,a1ColVar,wRowVar,wColVar],[tableDD1.getRootNode(),tableDD2.getRootNode()],multOp);
			
		then:
			println node
	}
	
	def "binary operations perform quickly"() {
		when:
			DDBuilder tableDD1 = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			DDBuilder tableDD2 = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
			MultiplicationOperation multOp = new MultiplicationOperation();
			
			long timeStart = new Date().time
			int numOps = 0
			
			BaseDDElement res;
			while(new Date().time - timeStart < 60000) {
				res = DDBuilder.build([a1RowVar,a1ColVar,wRowVar,wColVar],[tableDD1.getRootNode(),tableDD2.getRootNode()],multOp);
				++numOps;
			}
		then:
			println "number of binary operations: $numOps time: ${new Date().time - timeStart}"
			println res
	}
	
	def "variable restriction works"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
			HashMap<DDVariable,Integer> restrictVarValues = [:]
			restrictVarValues.put(a1RowVar, 1);
			tableDD = DDBuilder.restrict(restrictVarValues, tableDD.getRootNode())
			
			
		then:
			println tableDD
			
	}
	
	def "variable elimination works"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),{return 1d/25d})
			
			DDElement ret = DDBuilder.eliminate([a1RowVar,wRowVar], tableDD.getRootNode())
			
		then:
			assert (ret.getTotalWeight() - 1.0d)<0.0001d;
			println ret
	}
	
	def "variable priming works"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
			tableDD = DDBuilder.prime(tableDD.getRootNode())	
		then:
			println tableDD

	}
	
	def "variable unpriming works"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
			tableDD = DDBuilder.prime(tableDD.getRootNode())
			
			tableDD = DDBuilder.unprime(tableDD.getRootNode())
			
		then:
			println tableDD
	}
	
	def "approximating works"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			
			tableDD = DDBuilder.approximate(tableDD.getRootNode(), 1.0f)
			
			
		then:
			println tableDD
	}
	
	def "finding the max leaf works"() {
		when:
			DDBuilder tableDD = DDBuilder.build(new DDInfo([a1RowVar,a1ColVar,wRowVar,wColVar],false),c)
			DDLeaf leaf = DDBuilder.findMaxLeaf(tableDD.getRootNode())
			
		then:
			println tableDD
			println leaf
	}
}
