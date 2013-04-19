package masg.dd.pomdp.agent.policy.serialization;

import java.io.BufferedWriter;
import java.io.IOException;

import masg.dd.alphavector.AlphaVector;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.alphavector.serialization.BeliefAlphaVectorWriter;
import masg.dd.pomdp.agent.policy.BeliefAlphaVectorPolicy;

public class AlphaVectorPolicyWriter {
	BeliefAlphaVectorPolicy pol;
	
	public AlphaVectorPolicyWriter(BeliefAlphaVectorPolicy pol) {
		this.pol = pol;
	}
	
	public void write(BufferedWriter w) throws IOException {
		for(AlphaVector alpha:pol.getAlphaVectors()){
			w.write("+ALPHA");
			w.newLine();
			
			BeliefAlphaVectorWriter alphaWriter = new BeliefAlphaVectorWriter((BeliefAlphaVector) alpha);
			alphaWriter.write(w);
		}
		w.newLine();
	}
}
