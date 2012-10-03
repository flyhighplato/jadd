package masg.dd;

import masg.dd.context.DecisionDiagramContext;


public abstract class AbstractDecisionDiagram implements DecisionDiagram {
	protected DecisionDiagramContext context;
	public AbstractDecisionDiagram(DecisionDiagramContext ctx) {
		context = ctx;
	}
	
	public final DecisionDiagramContext getContext() {
		return context;
	}
}
