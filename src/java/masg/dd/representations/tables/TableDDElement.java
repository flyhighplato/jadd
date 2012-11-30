package masg.dd.representations.tables;

import java.util.concurrent.atomic.AtomicLong;

abstract public class TableDDElement {
	private static AtomicLong idCurr = new AtomicLong();
	protected long id;
	public TableDDElement() {
		id = idCurr.getAndIncrement();
	}
	
	public long getId() {
		return id;
	}
}
