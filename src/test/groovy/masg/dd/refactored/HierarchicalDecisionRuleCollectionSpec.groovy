package masg.dd.refactored;

import masg.dd.context.DDContext
import masg.dd.rules.operations.refactored.AdditionOperation
import masg.dd.rules.operations.refactored.ConstantMultiplicationOperation;
import masg.dd.rules.operations.refactored.MultiplicationOperation;
import masg.dd.rules.refactored.HierarchicalDecisionRuleCollection;
import masg.dd.rules.refactored.ImmutableHierarchicalDecisionRuleCollection;
import masg.dd.vars.DDVariable
import masg.dd.vars.DDVariableSpace
import masg.util.BitMap
import spock.lang.Specification;

public class HierarchicalDecisionRuleCollectionSpec extends Specification{
	HierarchicalDecisionRuleCollection coll;
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
		coll = new HierarchicalDecisionRuleCollection(DDContext.canonicalVariableOrdering, false);
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
			coll = HierarchicalDecisionRuleCollectionBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			ImmutableHierarchicalDecisionRuleCollection immColl = new ImmutableHierarchicalDecisionRuleCollection(coll, false);
			
		then:
			space.each{ HashMap<DDVariable,Integer> varSpacePoint ->
				double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				BitMap bm = HierarchicalDecisionRuleCollectionBuilder.varSpacePointToBitMap(vars,varSpacePoint);
	
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
				
				if(var3Value==var2Value || var3Value == var1Value) {
					return 999.0;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = HierarchicalDecisionRuleCollectionBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			
		then:
			coll.compress();
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
			coll = HierarchicalDecisionRuleCollectionBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			coll.compress();
			
			ImmutableHierarchicalDecisionRuleCollection immColl = new ImmutableHierarchicalDecisionRuleCollection(coll, false);
			ConstantMultiplicationOperation multOp = new ConstantMultiplicationOperation(5.0f);
		then:
			HierarchicalDecisionRuleCollection coll2 = immColl.apply(multOp);
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
			coll = HierarchicalDecisionRuleCollectionBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			coll.compress();
			
			ImmutableHierarchicalDecisionRuleCollection immColl = new ImmutableHierarchicalDecisionRuleCollection(coll, false);
			MultiplicationOperation multOp = new MultiplicationOperation();
		then:
			HierarchicalDecisionRuleCollection coll2 = immColl.apply(multOp,immColl);
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
					return 10.0f;
				}
				
				if(var3Value == var1Value) {
					return 5.0f;
				}
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = HierarchicalDecisionRuleCollectionBuilder.build(vars,c,false)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			coll.compress();
			
			ImmutableHierarchicalDecisionRuleCollection immColl = new ImmutableHierarchicalDecisionRuleCollection(coll, false);
			AdditionOperation addOp = new AdditionOperation();
			ArrayList<DDVariable> elimVars = [var2,var3]
		then:
			println immColl;
			HierarchicalDecisionRuleCollection coll2 = immColl.eliminateVariables(elimVars,addOp);
			println coll2;
	}
}