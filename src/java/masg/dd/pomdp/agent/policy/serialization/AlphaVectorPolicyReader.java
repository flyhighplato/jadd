package masg.dd.pomdp.agent.policy.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.alphavector.serialization.BeliefAlphaVectorReader;
import masg.dd.pomdp.agent.policy.BeliefAlphaVectorPolicy;

public class AlphaVectorPolicyReader {
	BufferedReader reader;
	public AlphaVectorPolicyReader(BufferedReader reader) {
		this.reader = reader;
	}
	
	public BeliefAlphaVectorPolicy read() throws IOException {
		return read(0);
	}
	
	public BeliefAlphaVectorPolicy read(int scope) throws IOException {
		
		ArrayList<BeliefAlphaVector> alphas = new ArrayList<BeliefAlphaVector>();
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			BeliefAlphaVectorReader alphaReader = new BeliefAlphaVectorReader(reader);
			BeliefAlphaVector alpha = alphaReader.read(scope);
			
			if(alpha==null)
				return null;
			
			alphas.add(alpha);
		}
		
		return new BeliefAlphaVectorPolicy(alphas);
	}
}
