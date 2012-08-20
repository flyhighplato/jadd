package masg.dd


import spock.lang.Specification

class DecisionRuleSpec extends Specification {
	def "rule to string works"() {
		when:
			DecisionRule rule1 = new DecisionRule("10**",1);
		then:
			"$rule1" == "10**:1.0"
	}
	
	def "matching works"() {
		when:
			DecisionRule rule1 = new DecisionRule("10**",1);
			DecisionRule rule2 = new DecisionRule("000000**",1);
		then:
			
			rule1.getMatchingRule(new DecisionRule("1001",2)).toString() == "1001:NaN";
			rule1.getMatchingRule(new DecisionRule("10*1",2)).toString() == "10*1:NaN";
			rule1.getMatchingRule(new DecisionRule("***1",2)).toString() == "10*1:NaN";
			rule1.getMatchingRule(new DecisionRule("1101",2)) == null;
			rule1.getMatchingRule(new DecisionRule("0101",2)) == null;
			rule2.getMatchingRule(new DecisionRule("00000000",Double.NaN)).toString() == "00000000:NaN"
	}
	
	def "rule merging works"() {
		when:
			DecisionRule rule3 = new DecisionRule("000:1.0");
			DecisionRule rule4 = new DecisionRule("010:1.0");
		then:
			DecisionRule ruleMerged = DecisionRule.mergeRules(rule3, rule4, 0.1f);
			ruleMerged == new DecisionRule("0*0:1.0")
	}
}
