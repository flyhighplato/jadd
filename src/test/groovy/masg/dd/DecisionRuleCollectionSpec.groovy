package masg.dd

import spock.lang.Specification

class DecisionRuleCollectionSpec extends Specification {
	def "rules sorted correctly"() {
		when:
			DecisionRuleCollection coll = new DecisionRuleCollection(3)
			coll << new DecisionRule("000:1.0f");
			coll << new DecisionRule("001:1.0f");
			coll << new DecisionRule("010:1.0f");
			coll << new DecisionRule("011:1.0f");
			
			coll << new DecisionRule("100:1.0f");
			coll << new DecisionRule("101:1.0f");
			coll << new DecisionRule("110:1.0f");
			coll << new DecisionRule("111:1.0f");
			coll << new DecisionRule("*1*:1.0f");
			
		then:
			coll.each{
				println it
			}
			println()
			coll.compress()
			coll.each{
				println it
			}
			println()
			
	}
	
	def "rules compressed correctly with NaN"() {
		when:
		DecisionRuleCollection coll = new DecisionRuleCollection("00000000000011000*".length())
		coll << new DecisionRule("00000000000011000*",0.0f);
		coll << new DecisionRule("************110***",Double.NaN);
		
	then:
		coll.each{
			println it
		}
		println()
		coll.compress()
		coll.each{
			println it
		}
	}
}
