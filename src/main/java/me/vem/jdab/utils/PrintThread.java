package me.vem.jdab.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class PrintThread extends Thread{
	
	private static PrintStream oldout, olderr;
	
	private static final PrintStream out = new PrintStream(new OutputStream() {
		@Override public void write(int i) throws IOException {
			instance.outQ.write(i);
		}
	});
	
	public static final PrintStream err = new PrintStream(new OutputStream() {
		@Override public void write(int i) throws IOException {
			instance.errQ.write(i);
		}
	});
	
	private static PrintThread instance;
	
	public static boolean hasInstance() {
	    return instance != null;
	}
	
	public static PrintThread getInstance() {
		if(instance == null) {
			instance = new PrintThread();
			
			instance.stdouts.add(oldout = System.out);
			System.setOut(out);
			
			instance.stderrs.add(olderr = System.err);
			System.setErr(err);
			
			instance.start();
		}
		
		return instance;
	}
	
    private boolean alive = true;
    
	private final List<PrintStream> stdouts, stderrs;
	private final ByteQueue outQ, errQ;
	
	private PrintThread() {
		super("Print Thread");
		stdouts = new LinkedList<>();
		stderrs = new LinkedList<>();
		
		outQ = new ByteQueue(1 << 16);
		errQ = new ByteQueue(1 << 16);
		
		//this.setDaemon(true);
	}
	
	@Override public void run() {
		while(alive){
		    synchronized(this) {
		        while(alive && outQ.isEmpty() && errQ.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        if(alive)
                            e.printStackTrace();
                    }
                }			        
		    }
			
		    synchronized(stdouts) {
	            while(!outQ.isEmpty()) {
	                int i = outQ.readInt();
	                for(PrintStream stream : stdouts)
	                    stream.write(i);
	            }
	        }
		    
		    synchronized(stderrs) {
	            while(!errQ.isEmpty()) {
	                int i = errQ.readInt();
	                for(PrintStream stream : stderrs)
	                    stream.write(i);   
	            }
	        }
		}

		System.setOut(oldout);
		System.setErr(olderr);
		instance = null;
	}
    
    public void kill() {
        alive = false;
        instance.interrupt();
    }
    
    public void addOut(PrintStream stream) {
        synchronized(stdouts) {
            stdouts.add(stream);   
        }
    }
    
    public void addErr(PrintStream stream) {
        synchronized(stderrs) {
            stderrs.add(stream);   
        }
    }
    
    public boolean removeOut(PrintStream stream) {
        synchronized(stdouts) {
            return stdouts.remove(stream); 
        }
    }
    
    public boolean removeErr(PrintStream stream) {
        synchronized(stderrs) {
            return stderrs.remove(stream);    
        }
    }
	
	class ByteQueue{
		private byte[] queue;
		private int read, write, size;
		
		public ByteQueue(int size) {
			queue = new byte[size];
			read = write = size = 0;
		}
		
		public void write(char c) { write((byte)c); }
		public void write(int i) { write((byte)i); }
		public synchronized void write(byte b) {
			if(size >= capacity()) 
				throw new RuntimeException("ByteQueue is full: cannot write");
			
			queue[write++] = b;
			size++;
			if(write == capacity())
				write = 0;
			
			if(PrintThread.instance != null) {
			    synchronized(PrintThread.instance) {
			        PrintThread.instance.notify();			        
			    }
			}
		}

		public char readChar() { return (char)readInt(); }
		public int readInt() { return read() & 0xFF; }
		public synchronized byte read() {
			if(isEmpty())
				throw new RuntimeException("ByteQueue is empty: cannot read");
			byte ret = queue[read++];
			size--;
			if(read == capacity())
				read = 0;
			return ret;
		}
		
		public int capacity() { return queue.length; }
		public boolean isEmpty() { return size == 0; }
		
		public void flushTo(PrintStream stream) {
			while(!isEmpty())
				stream.print(readChar());
		}
	}
}