package com.tapjoy.opt.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * @author lding
 *
 * Another candidate for the volatile cache based on soft reference
 * (From online resource. Google to get the original version with verbose comments) 
 */
public class SoftHashMap {

	@SuppressWarnings("rawtypes")
	private final Map hash = new HashMap();

	private final int HARD_SIZE;

	@SuppressWarnings("rawtypes")
	private final LinkedList hardCache = new LinkedList();

	@SuppressWarnings("rawtypes")
	private final ReferenceQueue queue = new ReferenceQueue();
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SoftHashMap.class);

	public SoftHashMap() { 
		this(100); 
	}

	public SoftHashMap(int hardSize) { 
		HARD_SIZE = hardSize; 
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object get(Object key) {
		Object result = null;

		// We get the SoftReference represented by that key
		SoftReference soft_ref = (SoftReference)hash.get(key);
		if (soft_ref != null) {
			result = soft_ref.get();
			if (result == null) {
				hash.remove(key);
			} else {
				hardCache.addFirst(result);
				if (hardCache.size() > HARD_SIZE) {
					hardCache.removeLast();
				}
			}
		}
		return result;
	}


	@SuppressWarnings("rawtypes")
	private static class SoftValue extends SoftReference {
		private final Object key; 
		@SuppressWarnings("unchecked")
		private SoftValue(Object k, Object key, ReferenceQueue q) {
			super(k, q);
			this.key = key;
		}
	}

	/** Here we go through the ReferenceQueue and remove garbage
	   collected SoftValue objects from the HashMap by looking them
	   up using the SoftValue.key data member. */
	private void processQueue() {
		SoftValue sv;
		while ((sv = (SoftValue)queue.poll()) != null) {
			hash.remove(sv.key); 
		}
	}

	@SuppressWarnings("unchecked")
	public Object put(Object key, Object value) {
		processQueue(); 
		return hash.put(key, new SoftValue(value, key, queue));
	}

	public Object remove(Object key) {
		processQueue(); 
		return hash.remove(key);
	}

	public void clear() {
		hardCache.clear();
		processQueue(); 
		hash.clear();
	}

	public int size() {
		processQueue(); 
		return hash.size();
	}

	@SuppressWarnings("rawtypes")
	public Set entrySet() {
		// no, no, you may NOT do that!!! GRRR
		throw new UnsupportedOperationException();
	}
}



