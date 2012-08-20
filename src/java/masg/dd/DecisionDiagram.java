package masg.dd;


public class DecisionDiagram {
	protected final DecisionDiagramContext context;
	public DecisionDiagram(DecisionDiagramContext ctx) {
		context = ctx;
	}
	
	public final DecisionDiagramContext getContext() {
		return context;
	}
}
