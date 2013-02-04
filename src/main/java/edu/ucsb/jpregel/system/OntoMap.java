package edu.ucsb.jpregel.system;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A map that creates objects on demand as they are requested. Uses Longs as
 * keys, and does not create value objects unless their keys do not already have
 * a corresponding value in the map.
 *
 * If multiple threads get(Long key) the same key, one thread will create a
 * value object and the others will block until the object is created, and then
 * they will get a reference to it. Because this class is final, this is nothing
 * to worry about.
 *
 * @author Charles Munger
 */
final public class OntoMap<V> {

	private static final ThreadLocal< Holder> uniqueNum = new ThreadLocal<Holder>() {

		@Override
		protected Holder initialValue() {
			return new Holder();
		}
	};
	private final Factory<V> factory;
	private final ConcurrentMap<Long, Holder<V>> map;

	public OntoMap(Factory<V> factory) {
		this(100, factory);
	}

	public OntoMap(int size, Factory<V> factory) {
		map = new ConcurrentHashMap<Long, Holder<V>>(100, 0.9f, 2);
		this.factory = factory;
	}

	public V get(final Long key) {
		final Holder<V> h = uniqueNum.get();
		final Holder<V> putIfAbsent = map.putIfAbsent(key, h);
		if (putIfAbsent != null) {
			return putIfAbsent.blockingGet();
		} else {
			final V make = factory.make();
			h.fill(make);
			uniqueNum.set(new Holder());
			return make;
		}
	}

	public V remove(Long key) {
		final Holder<V> remove = map.remove(key);
		return remove == null ? null : remove.get();
	}

	private static class Holder<T> {

		private volatile T contents = null;

		void fill(T contents) {
			this.contents = contents;
			synchronized (this) {
				notifyAll();
			}
		}

		T blockingGet() {
			if (contents == null) {
				synchronized (this) {
					try {
						while (contents == null) {
							wait();
						}
					} catch (InterruptedException ex) {
						System.out.println("Wait interrupted in get");
					}
				}
			}
			return contents;
		}

		T get() {
			return contents;
		}
	}
}
