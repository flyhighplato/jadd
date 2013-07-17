package masg.run;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import masg.dd.pomdp.agent.policy.BeliefAlphaVectorPolicy;
import masg.dd.pomdp.agent.policy.serialization.AlphaVectorPolicyReader;
import masg.problem.tag.TagProblemPOMDP;
import masg.problem.tag.simulator.AlessandroTagSimRecorder;
import masg.problem.tag.simulator.TagProblemSimulator;

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
			BeliefAlphaVectorPolicy pol1 = policyReader.read();
			reader.close();
			
			reader = new BufferedReader(new FileReader(fileName));
			policyReader = new AlphaVectorPolicyReader(reader);
			BeliefAlphaVectorPolicy pol2 = policyReader.read();
			reader.close();
			
			TagProblemPOMDP problem = new TagProblemPOMDP();
			TagProblemSimulator simulator = new TagProblemSimulator();
			
			ArrayList l = new ArrayList();
			l.add(new AlessandroTagSimRecorder());
			
			simulator.simulate(problem, problem, pol1, pol2, numTrials, numSteps, l);
	}

}
