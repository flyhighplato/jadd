package masg.problem.tag.simulator

import java.text.SimpleDateFormat
import java.util.HashMap;

import masg.dd.pomdp.agent.policy.AlphaVectorPolicy
import masg.dd.pomdp.agent.policy.Policy;
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyWriter
import masg.dd.variables.DDVariable;
import masg.problem.tag.TagProblemModel;

class AlessandroTagSimRecorder implements TagSimulationRecorder {
	Map obsNum = [:]
	Map actNum = [:]
	
	def actions = []
	def observations = []
	
	File traceFile
	Writer traceFileWriter
	
	@Override
	public void initialize(TagProblemModel problem, Policy policy, String baseFilePath) {
		problem.getPOMDP().observationSpace.eachWithIndex{obs, ix -> obsNum[obs] = ix}
		problem.getPOMDP().actionSpace.eachWithIndex{act, ix -> actNum[act] = ix}
		
		new File(baseFilePath + "/alessandro").mkdir()
		baseFilePath = baseFilePath + "/alessandro"
		
		if(policy instanceof AlphaVectorPolicy) {
			BufferedWriter writer = new File(baseFilePath + "/runPolicy.policy").newWriter(false)
			AlphaVectorPolicyWriter policyWriter = new AlphaVectorPolicyWriter(policy);
			policyWriter.write(writer);
			writer.flush();
			writer.close();
		}
		
		new File(baseFilePath + "/observations.txt").withWriter { writer ->
			obsNum.each{ obs, num ->
				writer.write("${num} ${obs}\n")
			}
		}
		
		new File(baseFilePath + "/actions.txt").withWriter { writer ->
			actNum.each{ act, num ->
				writer.write("${num} ${act}\n")
			}
		}
		
		traceFile = new File(baseFilePath + "/trace.txt")
	}

	@Override
	public void startSimulation() {	
		traceFileWriter = traceFile.newWriter(true)
	}

	@Override
	public void endSimulation() {	
		traceFileWriter.close()
	}

	@Override
	public void startTrial() {
		actions = []
		observations = []
	}

	@Override
	public void endTrial() {
		
		traceFileWriter.write(actions.join(",") + "\n")
		traceFileWriter.write(observations.join(",") + "\n")
		traceFileWriter.write("\n")
		traceFileWriter.flush()
		
	}

	@Override
	public void step(TagGrid grid, HashMap<DDVariable, Integer> action, HashMap<DDVariable, Integer> observation) {
		actions << actNum[action]
		observations << obsNum[observation]
		
	}


}
