package masg.dd.alphavector.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import masg.dd.AlgebraicDD;
import masg.dd.CondProbDD;
import masg.dd.ProbDD;
import masg.dd.alphavector.BeliefAlphaVector;
import masg.dd.representation.DDElement;
import masg.dd.representation.serialization.DDElementReader;
import masg.dd.serialization.CondProbDDReader;
import masg.dd.variables.DDVariable;

public class BeliefAlphaVectorReader {
	BufferedReader reader;
	public BeliefAlphaVectorReader(BufferedReader reader) {
		this.reader = reader;
	}
	
	public BeliefAlphaVector read() throws IOException {
		HashMap<DDVariable,Integer> action = new HashMap<DDVariable,Integer>();
		while(true) {
			String str = reader.readLine();

			if(str.isEmpty()) {
				break;
			}
			
			String[] params = str.split(":");
			String varName = params[0];
			int valCount = Integer.parseInt(params[1]);
			
			DDVariable v = new DDVariable(varName,valCount);
			int valAssigned = Integer.parseInt(params[2]);
			
			action.put(v, valAssigned);
		}
		
		CondProbDDReader cpddReader = new CondProbDDReader(reader);
		CondProbDD witnessPt = cpddReader.read();
		
		if(witnessPt==null)
			return null;
		
		DDElementReader elReader = new DDElementReader(reader);
		DDElement el = elReader.read();
		
		if(el==null)
			return null;
		
		AlgebraicDD valFn = new AlgebraicDD(el);
		
		return new BeliefAlphaVector(action,valFn,new ProbDD(witnessPt.getFunction()));
	}
}
