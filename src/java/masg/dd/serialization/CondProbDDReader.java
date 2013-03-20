package masg.dd.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.representation.DDElement;
import masg.dd.representation.serialization.DDElementReader;
import masg.dd.variables.DDVariable;

public class CondProbDDReader {
	BufferedReader reader;
	public CondProbDDReader(BufferedReader reader) {
		this.reader = reader;
	}
	
	public CondProbDD read() throws IOException {
		
		DDElementReader elReader = new DDElementReader(reader);
		DDElement el = elReader.read();
		
		if(el==null)
			return null;
		
		AlgebraicDD dd = new AlgebraicDD(el);
		
		
		
		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			String[] params = str.split(":");
			String varName = params[0];
			int valCount = Integer.parseInt(params[1]);
			
			condVars.add(new DDVariable(0,varName,valCount));
		}
		
		ArrayList<DDVariable> postVars = new ArrayList<DDVariable>();
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			String[] params = str.split(":");
			String varName = params[0];
			int valCount = Integer.parseInt(params[1]);
			
			postVars.add(new DDVariable(0,varName,valCount));
		}
		
		return new CondProbDD(condVars,postVars,dd);
	}
}
