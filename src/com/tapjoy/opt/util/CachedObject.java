package com.tapjoy.opt.util;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

public class CachedObject<T> {
	private static Logger logger = Logger
			.getLogger(CachedObject.class);
	
	public enum PRIORITY {
		LOW, MEDIUM, HIGH, ;
	}

	public abstract static class PriorityRunnable implements Runnable,
			Comparable<PriorityRunnable> {
		final PRIORITY priority;

		public PriorityRunnable(PRIORITY priority) {
			this.priority = priority;
		}

		@Override
		public int compareTo(PriorityRunnable o) {
			return priority.compareTo(o.priority);
		}

	}

	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10,
			0, TimeUnit.NANOSECONDS, new PriorityBlockingQueue<Runnable>());
	private static ScheduledThreadPoolExecutor resetExecutor = new ScheduledThreadPoolExecutor(
			1); // Single instance executor to clear the flags

	public interface FetchTask<T> {
		public T fetch() throws TimeoutException;
	};

	protected T obj;
	long refreshedAt = System.currentTimeMillis();
	boolean refreshStarted = false;
	FetchTask<T> ft;
	int ttl;

	public CachedObject(T obj, int ttl, FetchTask<T> ft) {
		super();
		this.obj = obj;
		this.ft = ft;
		this.ttl = ttl;
		if (obj == null) {
			refreshedAt = -1; // make it stale
		}

	}

	public synchronized void refresh(T obj) {
		if (obj != null) {
			this.obj = obj;
			refreshedAt = System.currentTimeMillis();
		}
		refreshStarted = false;
		// Object will not be refreshed with a null value. It will retain the
		// old value if it is null
	}

	/**
	 * Refresh the object if required
	 * 
	 * @param task
	 *            Task that refreshed the cache. Note that the task should call
	 *            refresh()
	 * @param delay
	 *            when to refresh, in miliseconds. (when to execute the refresh
	 *            task)
	 * @param ttl
	 *            Time to live for the object, pass 0 or -1 to refresh it anyway
	 * @return
	 */
	public boolean scheduleRefresh(PRIORITY priority) {
		if (refreshStarted) {
			return false;
		}
		final CachedObject<T> parent = this;
		final long lastRefreshedAt = parent.refreshedAt;
		synchronized (this) {
			if (isStale(ttl)) { // double check the staleness before refresh
				refreshStarted = true;
				executor.execute(new PriorityRunnable(priority) {

					@Override
					public void run() {
						try {
							parent.refresh(ft.fetch());
						} catch (Throwable e) {
							logger.warn("Failed to refresh the cache", e);
							parent.refresh(null); // reset the refresh process, note that setting null may not really make the obj null
						} 
					}

				});

				resetExecutor.schedule(new Runnable() {
					@Override
					public void run() {
						if (parent.isRefreshStarted()
								&& parent.refreshedAt == lastRefreshedAt) {
							parent.refresh(null); // give up on the task and
													// refresh the flag
						}
					}
				}, 10, TimeUnit.SECONDS);
			}
		}

		return true;
	}

	/**
	 * Is this object stale given a ttl paramter, which is in seconds
	 * 
	 * @param ttl
	 * @return
	 */
	public boolean isStale(int ttl) {
		return ((System.currentTimeMillis() - refreshedAt) > (ttl * 1000));

	}

	/**
	 * @return the obj
	 */
	public T getObj() {
		if (isStale(ttl)) {
			scheduleRefresh(PRIORITY.LOW); // initiate refresh on low priority
		}
		return obj;
	}

	/**
	 * @return the obj
	 * @throws TimeoutException 
	 */
	public T getObj(boolean wait, long waitMillis) throws TimeoutException {
		if (isStale(ttl)) {
			scheduleRefresh(PRIORITY.LOW); // initiate refresh on low priority
		}
		long waited = 0;
		while (obj == null && wait && refreshStarted) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.debug("Ignoring the exception while waiting", e);
			}
			waited += 50;
			if(waited > waitMillis ) {
				throw new TimeoutException();
			}
		}
		return obj;
	}

	public static void executeNow(PriorityRunnable command) {
		executor.execute(command);
	}

	/**
	 * @return the refreshStarted
	 */
	public boolean isRefreshStarted() {
		return refreshStarted;
	}

}
