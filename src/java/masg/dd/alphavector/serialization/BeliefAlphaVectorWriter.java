package masg.dd.alphavector.serialization;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map.Entry;

import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.representation.serialization.DDElementWriter;
import masg.dd.serialization.CondProbDDWriter;
import masg.dd.variables.DDVariable;

public class BeliefAlphaVectorWriter {
	BeliefAlphaVector alphaVector;
	
	public BeliefAlphaVectorWriter(BeliefAlphaVector alphaVector) {
		this.alphaVector = alphaVector;
	}
	
	public void write(BufferedWriter w) throws IOException {
		for(Entry<DDVariable, Integer> e:alphaVector.getAction().entrySet()) {
			w.write(e.getKey().getName() + ":" + e.getKey().getValueCount() + ":" + e.getValue());
			w.newLine();
		}
		w.newLine();
		
		CondProbDDWriter cpddWriter = new CondProbDDWriter(alphaVector.getWitnessPoint());
		cpddWriter.write(w);
		
		DDElementWriter elWriter = new DDElementWriter(alphaVector.getValueFunction().getFunction());
		elWriter.write(w);
		
	}
}
