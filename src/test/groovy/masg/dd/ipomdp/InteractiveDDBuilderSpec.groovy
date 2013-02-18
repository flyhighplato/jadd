package masg.dd.ipomdp

import java.util.Map.Entry
import masg.dd.pomdp.POMDP
import masg.dd.pomdp.agent.belief.Belief
import masg.dd.pomdp.agent.belief.POMDPBelief
import masg.dd.representation.DDCollection
import masg.dd.representation.DDElement
import masg.dd.representation.DDInfo
import masg.dd.representation.builder.buildfunctions.DDBuilderInteractiveClosureFunction
import masg.problem.tag.TagProblemPOMDP
import spock.lang.Specification
import masg.dd.representation.builder.DDBuilder;
import masg.dd.variables.DDVariable

class InteractiveDDBuilderSpec extends Specification {
	def "interactive dd can be built"() {
		when:
		
			TagProblemPOMDP problem = new TagProblemPOMDP()
			POMDP p = problem.getPOMDP();
			
			DDInfo info = new DDInfo(p.getStates(),false)
		    DDCollection ddColl = new DDCollection(info);
			
			POMDPBelief pBelief = new POMDPBelief(p, p.getInitialBelief());
			
			DDElement otherBelief = ddColl.merge(p.getInitialBelief().toProbabilityDD().getFunction().getFunction());
			Closure c = { HashMap args ->
				
				HashMap<DDElement,Double> dist = [:]
				dist[otherBelief] = 1.0d;
				
				double val = 1.0d/Math.pow(25,3.0d)
				
				return [val, dist]	
			}
			
			DDBuilderInteractiveClosureFunction fn = new DDBuilderInteractiveClosureFunction(c);
			
			DDElement result = DDBuilder.build(info, fn).getRootNode()
		then:
			println result
	}
}
