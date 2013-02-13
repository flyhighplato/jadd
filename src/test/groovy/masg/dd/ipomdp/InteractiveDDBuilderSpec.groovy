package masg.dd.ipomdp

import masg.dd.pomdp.POMDP
import masg.dd.representation.DDCollection
import masg.dd.representation.DDElement
import masg.dd.representation.DDInfo
import masg.dd.representation.builder.buildfunctions.DDBuilderInteractiveClosureFunction
import masg.problem.tag.TagProblemPOMDP
import spock.lang.Specification
import masg.dd.representation.builder.DDBuilder;

class InteractiveDDBuilderSpec extends Specification {
	def "interactive dd can be built"() {
		when:
		
			TagProblemPOMDP problem = new TagProblemPOMDP()
			POMDP p = problem.getPOMDP();
			
			DDInfo info = new DDInfo(p.getStates(),false)
		    DDCollection ddColl = new DDCollection(info);
			
			DDElement otherBelief = ddColl.merge(p.getInitialBelief().toProbabilityDD().getFunction().getFunction());
			Closure c = {
				
				HashMap<DDElement,Double> dist = [:]
				dist[otherBelief] = 1.0d;
				
				return [0.5d, dist]	
			}
			
			DDBuilderInteractiveClosureFunction fn = new DDBuilderInteractiveClosureFunction(c);
			
			DDElement result = DDBuilder.build(info, fn).getRootNode()
		then:
			println result
	}
}