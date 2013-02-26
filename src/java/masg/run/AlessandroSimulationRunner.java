package masg.run;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import masg.dd.pomdp.agent.policy.AlphaVectorPolicy;
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyReader;
import masg.problem.tag.TagProblemPOMDP;
import masg.problem.tag.TagProblemSimulator;
import masg.problem.tag.simulator.AlessandroTagSimRecorder;

public class AlessandroSimulationRunner {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
			int numTrials = 10000;
			int numSteps = 100;
			
			String fileName = "policy.policy";
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			AlphaVectorPolicyReader policyReader = new AlphaVectorPolicyReader(reader);
			AlphaVectorPolicy pol1 = policyReader.read();
			reader.close();
			
			reader = new BufferedReader(new FileReader(fileName));
			policyReader = new AlphaVectorPolicyReader(reader);
			AlphaVectorPolicy pol2 = policyReader.read();
			reader.close();
			
			TagProblemPOMDP problem = new TagProblemPOMDP();
			TagProblemSimulator simulator = new TagProblemSimulator();
			
			ArrayList l = new ArrayList();
			l.add(new AlessandroTagSimRecorder());
			
			simulator.simulate(problem, pol1, pol2, numTrials, numSteps, l);
	}

}
