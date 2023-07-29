package com.example.server;

// https://www.baeldung.com/java-threadlocal
// The TheadLocal construct allows us to store data that will be accessible only by a specific thread.
public class ThreadLocalDemo {
	public static void main(String[] args) {
		// ThreadLocal<String> threadLocal = new ThreadLocal<String>();
		ThreadLocal<Context> threadLocal = new ThreadLocal<Context>();

		Runnable runnable1 = () -> {
			// Set in threadLocal
			threadLocal.set(new Context("t1_k1", "t1_k2"));
			// threadLocal.set("t1");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Get from threadLocal
			System.out.println(threadLocal.get().key1);
			System.out.println(threadLocal.get().key2);
		};
		Thread thread1 = new Thread(runnable1);

		Runnable runnable2 = () -> {
			// Set in threadLocal
			threadLocal.set(new Context("t2_k1", "t2_k2"));
			// threadLocal.set("t2");
			// Get from threadLocal
			System.out.println(threadLocal.get().key1);
			System.out.println(threadLocal.get().key2);
			// Remove from threadLocal
			threadLocal.remove();
		};
		Thread thread2 = new Thread(runnable2);

		thread1.start();
		thread2.start();
	}
}

class Context {
	String key1;
	String key2;

	public Context(String key1, String key2) {
		this.key1 = key1;
		this.key2 = key2;
	}
}
