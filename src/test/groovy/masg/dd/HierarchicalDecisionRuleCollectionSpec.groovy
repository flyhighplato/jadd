package masg.dd;

import masg.dd.context.DDContext
import masg.dd.rules.HierarchicalDecisionRuleCollection
import masg.dd.rules.ImmutableHierarchicalDecisionRuleCollection
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
		coll = new HierarchicalDecisionRuleCollection(DDContext.canonicalVariableOrdering);
	}
	
	def "rule can be added and retrieved"() {
		when:
			BitMap m = new BitMap(space.getBitCount());
		then:
			coll.setValue([var1,var3],m, 5.0f);
			assert coll.getValue([var1,var2,var3], m, false) == 5.0f;
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
			coll = HierarchicalDecisionRuleCollectionBuilder.build(vars,c)
			DDVariableSpace space = new DDVariableSpace(vars);
			ImmutableHierarchicalDecisionRuleCollection immColl = new ImmutableHierarchicalDecisionRuleCollection(coll);
			
		then:
			space.each{ HashMap<DDVariable,Integer> varSpacePoint ->
				double val = c(varSpacePoint.collectEntries{k,v -> [k.toString(),v]})
				BitMap bm = HierarchicalDecisionRuleCollectionBuilder.varSpacePointToBitMap(vars,varSpacePoint);
	
				assert val == immColl.getValue(vars, bm, false)
			}
	}
	
	def "collection can be compressed"() {
		when:
			Closure c = {
				Map variables ->
				int var1Value = variables[var1.name];
				int var2Value = variables[var2.name];
				int var3Value = variables[var3.name];
				
				/*if(var1Value==var3Value && var2Value==var3Value) {
					return 999.0;
				}*/
				
				return 0.0f;
			}
			ArrayList<DDVariable> vars = [var1,var2,var3]
			coll = HierarchicalDecisionRuleCollectionBuilder.build(vars,c)
			DDVariableSpace space = new DDVariableSpace(vars);
			
			
		then:
			//println coll
			coll.compress();
			println coll;
	}
}
