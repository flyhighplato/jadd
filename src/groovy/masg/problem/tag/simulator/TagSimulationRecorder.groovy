package masg.problem.tag.simulator

import masg.dd.pomdp.agent.policy.Policy
import masg.dd.variables.DDVariable
import masg.problem.tag.TagProblemModel

public interface TagSimulationRecorder {
	void initialize(TagProblemModel problem, Policy policy, String baseFilePath);
	
	void startSimulation()
	void endSimulation()
	void startTrial()
	void endTrial()
	void step(TagGrid grid, HashMap<DDVariable,Integer> action, HashMap<DDVariable,Integer> observation)
}
