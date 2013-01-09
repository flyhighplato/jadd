package masg.dd.pomdp.agent.policy.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.alphavector.serialization.BeliefAlphaVectorReader;
import masg.dd.pomdp.agent.policy.AlphaVectorPolicy;

public class AlphaVectorPolicyReader {
	BufferedReader reader;
	public AlphaVectorPolicyReader(BufferedReader reader) {
		this.reader = reader;
	}
	
	public AlphaVectorPolicy read() throws IOException {
		
		ArrayList<BeliefAlphaVector> alphas = new ArrayList<BeliefAlphaVector>();
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			BeliefAlphaVectorReader alphaReader = new BeliefAlphaVectorReader(reader);
			BeliefAlphaVector alpha = alphaReader.read();
			
			if(alpha==null)
				return null;
			
			alphas.add(alpha);
		}
		
		return new AlphaVectorPolicy(alphas);
	}
}
