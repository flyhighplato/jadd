package masg.dd.refactored;

import masg.dd.context.DDContext
import masg.dd.rules.operations.refactored.AdditionOperation
import masg.dd.rules.operations.refactored.ConstantMultiplicationOperation;
import masg.dd.rules.operations.refactored.MultiplicationOperation;
import masg.dd.rules.refactored.ImmutableDDElement
import masg.dd.rules.refactored.ImmutableDDNode;
import masg.dd.rules.refactored.MutableDDElement
import masg.dd.rules.refactored.MutableDDNode
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace
import masg.util.BitMap
import spock.lang.Specification;

public class HierarchicalDecisionRuleCollectionSpec extends Specification{
	MutableDDElement coll;
	DDVariable var1, var2, var3;
	DDVariableSpace space;
	def setup() {
		var1 = new DDVariable("test1",5);
		var2 = new DDVariable("test2",5);
		var3 = new DDVariable("test3",5);
		space = new DDVariableSpace();
		
		space.addVariable(var2);
		space.addVariable(var3);
		space.addVariable(var1);
		
		DDContext.canonicalVariableOrdering = [var1,var2,var3];
		coll = new MutableDDNode(DDContext.canonicalVariableOrdering, false);
	}
	
	def "rule can be added and retrieved"() {
		when:
			BitMap m = new BitMap(space.getBitCount());
		then:
			coll.setValue([var1,var3],m, 5.0f);
			assert coll.getValue([var1,var2,var3], m) == 5.0f;
	}
	
	def "collection can be built from closure"() {
		when:
			Closure c = {
				Map variables ->
				int var1Value = variables[var1.name];
				int var2Value = variables[var2.name];
				int var3Value = variables[var3.name];
				
				if(var1Value==var2Value || var1Value==var3Value || var2Value==var3Value) {
					return 999.0;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = MutableDDElementBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			ImmutableDDNode immColl = new ImmutableDDNode(coll);
			
		then:
			space.each{ HashMap<DDVariable,Integer> varSpacePoint ->
				double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				BitMap bm = MutableDDElementBuilder.varSpacePointToBitMap(vars,varSpacePoint);
	
				assert val == immColl.getValue(vars, bm)
			}
	}
	
	def "collection can be compressed"() {
		when:
			Closure c = {
				Map variables ->
				int var1Value = variables[var1.name];
				int var2Value = variables[var2.name];
				int var3Value = variables[var3.name];
				
				if(var3Value==var2Value) {
					return 999.0;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = MutableDDElementBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			
		then:
			println coll;
			coll.compress();
			println coll;
	}
	
	def "unary operations work"() {
		when:
			Closure c = {
				Map variables ->
				int var1Value = variables[var1.name];
				int var2Value = variables[var2.name];
				int var3Value = variables[var3.name];
				
				if(var3Value==var2Value) {
					return 10.0f;
				}
				
				if(var3Value == var1Value) {
					return 5.0f;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = MutableDDElementBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			coll.compress();
			
			ImmutableDDNode immColl = new ImmutableDDNode(coll);
			ConstantMultiplicationOperation multOp = new ConstantMultiplicationOperation(5.0f);
		then:
			ImmutableDDNode coll2 = immColl.apply(multOp);
			//println coll2;
	}
	
	def "binary operations work"() {
		when:
			Closure c = {
				Map variables ->
				int var1Value = variables[var1.name];
				int var2Value = variables[var2.name];
				int var3Value = variables[var3.name];
				
				if(var3Value==var2Value) {
					return 10.0f;
				}
				
				if(var3Value == var1Value) {
					return 5.0f;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = MutableDDElementBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			coll.compress();
			
			ImmutableDDNode immColl = new ImmutableDDNode(coll);
			MultiplicationOperation multOp = new MultiplicationOperation();
		then:
			//println coll;
			ImmutableDDNode coll2 = immColl.apply(multOp,immColl);
			//println coll2;
	}
	
	def "variable elimination works"() {
		when:
			Closure c = {
				Map variables ->
				int var1Value = variables[var1.name];
				int var2Value = variables[var2.name];
				int var3Value = variables[var3.name];
				
				if(var3Value==var2Value) {
					return 1.0f;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = MutableDDElementBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			coll.compress();
			
			ImmutableDDNode immColl = new ImmutableDDNode(coll);
			AdditionOperation addOp = new AdditionOperation();
			ArrayList<DDVariable> elimVars = [var2,var3]
		then:
			println immColl;
			ImmutableDDNode coll2 = immColl.eliminateVariables(elimVars,addOp);
			println coll2;
	}
	
	def "variable restriction works"() {
		when:
			Closure c = {
				Map variables ->
				int var1Value = variables[var1.name];
				int var2Value = variables[var2.name];
				int var3Value = variables[var3.name];
				
				if(var3Value==var2Value) {
					return 10.0f;
				}
				
				if(var3Value == var1Value) {
					return 5.0f;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = MutableDDElementBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			coll.compress();
			
			ImmutableDDNode immColl = new ImmutableDDNode(coll);
			
			HashMap<DDVariable,Integer> elimVars = new HashMap<DDVariable,Integer>();
			elimVars[var1]=1;
			elimVars[var2]=1;
		then:
			//println immColl;
			ImmutableDDNode coll2 = immColl.restrict(elimVars)
			//println coll2;
	}
}
