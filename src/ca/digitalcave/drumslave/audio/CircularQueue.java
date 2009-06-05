package ca.digitalcave.drumslave.audio;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CircularQueue<E> extends ConcurrentLinkedQueue<E> {

	private static final long serialVersionUID = 1l;

	@Override
	public E poll() {
		E e = super.poll();
		this.offer(e);
		return e;
	}
}
