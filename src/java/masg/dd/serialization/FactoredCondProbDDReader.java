package masg.dd.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import masg.dd.CondProbDD;
import masg.dd.FactoredCondProbDD;

public class FactoredCondProbDDReader {
	BufferedReader reader;
	public FactoredCondProbDDReader(BufferedReader reader) {
		this.reader = reader;
	}
	
	public FactoredCondProbDD read(int scope) throws IOException {
		
		ArrayList<CondProbDD> fns = new ArrayList<CondProbDD>();
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			CondProbDDReader cpddReader = new CondProbDDReader(reader);
			CondProbDD cpdd = cpddReader.read(scope);
			
			if(cpdd==null)
				return null;
			
			fns.add(cpdd);
		}
		
		return new FactoredCondProbDD(fns);
	}
}
