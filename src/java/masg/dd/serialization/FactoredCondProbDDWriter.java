package masg.dd.serialization;

import java.io.BufferedWriter;
import java.io.IOException;

import masg.dd.CondProbDD;
import masg.dd.FactoredCondProbDD;

public class FactoredCondProbDDWriter {
	FactoredCondProbDD fdd;
	
	public FactoredCondProbDDWriter(FactoredCondProbDD dd) {
		fdd = dd;
	}
	
	public void write(BufferedWriter w) throws IOException {
		for(CondProbDD dd:fdd.getFunctions()){
			w.write("+DD");
			w.newLine();
			
			CondProbDDWriter cpddWriter = new CondProbDDWriter(dd);
			cpddWriter.write(w);
		}
		w.newLine();
	}
}
