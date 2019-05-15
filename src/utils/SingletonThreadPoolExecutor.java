package utils;

import java.util.Random;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingletonThreadPoolExecutor {
	private ScheduledThreadPoolExecutor thread;

	private final static SingletonThreadPoolExecutor instance = null;

	private SingletonThreadPoolExecutor() {
		thread = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
				new MyRejectedExecutionHandler());
	}

	public static SingletonThreadPoolExecutor getInstance() {
		if (instance != null)
			return instance;

		return new SingletonThreadPoolExecutor();
	}

	/**
	 * @return the thread
	 */
	public ScheduledThreadPoolExecutor get() {
		return thread;
	}

	private class MyRejectedExecutionHandler implements RejectedExecutionHandler {

		private static final short INTERVAL = 5000;

		@Override
		public void rejectedExecution(Runnable subprotocol, ThreadPoolExecutor thread) {
			((ScheduledThreadPoolExecutor) thread).schedule(subprotocol, INTERVAL + new Random().nextInt(INTERVAL),
					TimeUnit.MILLISECONDS);
		}

	}
}