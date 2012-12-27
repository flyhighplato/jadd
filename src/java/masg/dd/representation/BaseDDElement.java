package masg.dd.representation;

import java.util.concurrent.atomic.AtomicLong;


abstract public class BaseDDElement implements DDElement {
	private static AtomicLong idCurr = new AtomicLong();
	protected long id;
	public BaseDDElement() {
		id = idCurr.getAndIncrement();
	}
	
	public long getId() {
		return id;
	}
	
}
