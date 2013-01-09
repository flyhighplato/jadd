package masg.dd.pomdp.agent.policy.serialization;

import java.io.BufferedWriter;
import java.io.IOException;

import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.alphavector.serialization.BeliefAlphaVectorWriter;
import masg.dd.pomdp.agent.policy.AlphaVectorPolicy;

public class AlphaVectorPolicyWriter {
	AlphaVectorPolicy pol;
	
	public AlphaVectorPolicyWriter(AlphaVectorPolicy pol) {
		this.pol = pol;
	}
	
	public void write(BufferedWriter w) throws IOException {
		for(BeliefAlphaVector alpha:pol.getAlphaVectors()){
			w.write("+ALPHA");
			w.newLine();
			
			BeliefAlphaVectorWriter alphaWriter = new BeliefAlphaVectorWriter(alpha);
			alphaWriter.write(w);
		}
		w.newLine();
	}
}
