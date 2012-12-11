package masg.dd.representations.tables

import masg.dd.context.DDContext
import masg.dd.operations.ConstantMultiplicationOperation
import masg.dd.operations.MultiplicationOperation
import masg.dd.representations.dag.ImmutableDDElement
import masg.dd.representations.dag.ImmutableDDLeaf
import masg.dd.representations.dag.ImmutableDDNode
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
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			
		then:
			println tableDD
			println immColl
	}
	
	def "unary operations work"() {
		when:
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			ConstantMultiplicationOperation multOp = new ConstantMultiplicationOperation(5.0f);
			
			tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],immColl,multOp) 
			immColl = tableDD.asDagDD()
		then:
			println tableDD
			println immColl
	}
	
	def "binary operations work"() {
		when:
			TableDD tableDD1 = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			ImmutableDDElement immColl1 = tableDD1.asDagDD()
			TableDD tableDD2 = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			ImmutableDDElement immColl2 = tableDD2.asDagDD()
			
			MultiplicationOperation multOp = new MultiplicationOperation();
			
			
			ImmutableDDElement immColl = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],[immColl1,immColl2],multOp);
			
		then:
			println "Done"
			//println immColl
	}
	
	def "binary operations perform quickly"() {
		when:
			TableDD tableDD1 = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			ImmutableDDElement immColl1 = tableDD1.asDagDD()
			TableDD tableDD2 = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			ImmutableDDElement immColl2 = tableDD2.asDagDD()
			
			
			
			MultiplicationOperation multOp = new MultiplicationOperation();
			
			
			ImmutableDDElement immColl;
			
			long timeStart = new Date().time
			int numOps = 0
			while(new Date().time - timeStart < 60000) {
				immColl = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],[immColl1,immColl2],multOp);
				++numOps;
			}
		then:
			println "number of binary operations: $numOps time: ${new Date().time - timeStart}"
			println immColl
	}
	
	def "variable restriction works"() {
		when:
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			
			HashMap<DDVariable,Integer> restrictVarValues = [:]
			restrictVarValues.put(a1RowVar, 1);
			tableDD = TableDD.restrict(restrictVarValues, immColl)
			immColl = tableDD.asDagDD()
			
		then:
			println tableDD
			println immColl
	}
	
	def "variable elimination works"() {
		when:
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar],{return 1d/25d})
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			
			immColl = TableDD.eliminate([a1RowVar,wRowVar], immColl)
			
		then:
			assert (immColl.getTotalWeight() - 1.0d)<0.0001d;
			//println tableDD
			println immColl
	}
	
	def "variable priming works"() {
		when:
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			
			tableDD = TableDD.prime(immColl)
			immColl = tableDD.asDagDD()
			
		then:
			println tableDD
			println immColl
	}
	
	def "variable unpriming works"() {
		when:
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			
			tableDD = TableDD.prime(immColl)
			immColl = tableDD.asDagDD()
			
			tableDD = TableDD.unprime(immColl)
			immColl = tableDD.asDagDD()
			
		then:
			println tableDD
			println immColl
	}
	
	def "approximating works"() {
		when:
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			
			tableDD = TableDD.approximate(immColl, 1.0f)
			immColl = tableDD.asDagDD()
			
		then:
			println tableDD
			println immColl
	}
	
	def "finding the max leaf works"() {
		when:
			TableDD tableDD = TableDD.build([a1RowVar,a1ColVar,wRowVar,wColVar],c)
			
			ImmutableDDElement immColl = tableDD.asDagDD()
			
			immColl = TableDD.findMaxLeaf(immColl)
			
		then:
			println tableDD
			println immColl
	}
}
