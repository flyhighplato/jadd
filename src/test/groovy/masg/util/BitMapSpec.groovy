package masg.util

import spock.lang.Specification

class BitMapSpec extends Specification {
	def "getting subset of bits works"() {
		when:
			BitMap bm = new BitMap(3);
			bm.set(1);
			bm.set(2);
		then:
			println bm
			println bm.get(0,2);
	}
}
