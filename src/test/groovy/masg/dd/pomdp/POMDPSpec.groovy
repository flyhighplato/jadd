package masg.dd.pomdp

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.List;

import masg.dd.AlgebraicDecisionDiagram;
import masg.dd.vars.DDVariable
import masg.problem.tag.TagProblem

import spock.lang.Specification

class POMDPSpec extends Specification {
	
	TagProblem problem = new TagProblem()

	def "POMDP can be initialized properly"() {
		when:
			
			HashMap<DDVariable,Integer> varValues = new HashMap<DDVariable,Integer>()
			varValues.put(problem.a1RowVar,0)
			varValues.put(problem.a1ColVar,0)
			varValues.put(problem.actVar,2)
			varValues.put(problem.a1RowPrimeVar,0)
			varValues.put(problem.a1ColPrimeVar,0)
			//varValues.put(problem.wColPrimeVar,0)
		then:
			problem.getPOMDP().getTransFns();
			/*p.getTransFns().getDDs().each{
				println it
				println "${it.rules.size()} rules"
				println()
			}*/
			
			problem.wColPrimeVar.numValues.times{
				//varValues.put(problem.wColPrimeVar, it)
				println problem.getPOMDP().getTransFns().getValue(varValues)
			}
			
	}
	
	/*def "POMDP transitions are correct"() {
		when:
			AlgebraicDecisionDiagram[] Ts = p.getTransFns()
		then:
			Ts.eachWithIndex{ AlgebraicDecisionDiagram T, int Tix ->
				T.getContext().getVariableInstances()
			}
			true;
	}*/
}
