package masg.dd;


public abstract class AbstractDecisionDiagram implements DecisionDiagram {
	protected final DecisionDiagramContext context;
	public AbstractDecisionDiagram(DecisionDiagramContext ctx) {
		context = ctx;
	}
	
	public final DecisionDiagramContext getContext() {
		return context;
	}
}
