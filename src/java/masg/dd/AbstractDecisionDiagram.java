package masg.dd;

import masg.dd.context.DDContext;


public abstract class AbstractDecisionDiagram implements DecisionDiagram {
	protected DDContext context;
	public AbstractDecisionDiagram(DDContext ctx) {
		context = ctx;
	}
	
	public final DDContext getContext() {
		return context;
	}
}
