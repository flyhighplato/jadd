package masg.dd

import spock.lang.Specification


class AlgebraicDecisionDiagramSpec extends Specification {
	DecisionDiagramContext ctx = new DecisionDiagramContext(['a','b','c']);
	
	def "ADD initializes correctly"() {
		when:
			AlgebraicDecisionDiagram add = new AlgebraicDecisionDiagram(ctx);
			
			add.addRule(new DecisionRule("111",50));
			add.addRule(new DecisionRule("110",6));
			add.addRule(new DecisionRule("101",6));
			add.addRule(new DecisionRule("100",6));
			add.addRule(new DecisionRule("011",7));
			add.addRule(new DecisionRule("010",7));
			add.addRule(new DecisionRule("001",7));
			add.addRule(new DecisionRule("000",7));
		then:
			add.rules.toString() == "[111:50.0, 110:6.0, 10*:6.0, 0**:7.0]";
	}
	
	def "ADDs add correctly"() {
		when:
		AlgebraicDecisionDiagram add1 = new AlgebraicDecisionDiagram(ctx);
		add1.addRule(new DecisionRule("11",1));
		add1.addRule(new DecisionRule("10",1));
		add1.addRule(new DecisionRule("01",2));
		add1.addRule(new DecisionRule("00",2));
		
		AlgebraicDecisionDiagram add2 = new AlgebraicDecisionDiagram(ctx);
		add2.addRule(new DecisionRule("11",3));
		add2.addRule(new DecisionRule("10",4));
		add2.addRule(new DecisionRule("01",5));
		add2.addRule(new DecisionRule("00",5));
		
		10000.times{
			add1 = (add1 + add2)
		}
		then:
			
			println add1;
	}
	
	def "ADDs max correctly"() {
		when:
		AlgebraicDecisionDiagram add1 = new AlgebraicDecisionDiagram(ctx);
		add1.addRule(new DecisionRule("11",1));
		add1.addRule(new DecisionRule("10",1));
		add1.addRule(new DecisionRule("01",7));
		add1.addRule(new DecisionRule("00",2));
		
		AlgebraicDecisionDiagram add2 = new AlgebraicDecisionDiagram(ctx);
		add2.addRule(new DecisionRule("11",1));
		add2.addRule(new DecisionRule("10",4));
		add2.addRule(new DecisionRule("01",5));
		add2.addRule(new DecisionRule("00",7));
		
		10000.times{
			add1 = add1.max(add2)
		}
		then:
			
			println add1;
	}
}
