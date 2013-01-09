package masg.dd.serialization;

import java.io.BufferedWriter;
import java.io.IOException;

import masg.dd.CondProbDD;
import masg.dd.representation.serialization.DDElementWriter;
import masg.dd.variables.DDVariable;

public class CondProbDDWriter {
	CondProbDD dd;
	public CondProbDDWriter(CondProbDD dd) {
		this.dd = dd;
	}
	
	public void write(BufferedWriter w) throws IOException {
		DDElementWriter elWriter = new DDElementWriter(dd.getFunction().getFunction());
		elWriter.write(w);
		
		for(DDVariable v:dd.getConditionalVariables()) {
			w.write(v.getName() + ":" + v.getValueCount());
			w.newLine();
		}
		w.newLine();
		
		for(DDVariable v:dd.getPosteriorVariables()) {
			w.write(v.getName() + ":" + v.getValueCount());
			w.newLine();
		}
		w.newLine();
	}
}
