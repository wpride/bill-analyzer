package concurrency;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.BlockingQueue;

import utils.BillUtil;
import utils.ProxyUtil.Pair;

public class BillUrlWriter implements Runnable {

	private final BlockingQueue<String> urlQueue;
	private final BlockingQueue<Pair> proxyQueue;
	private Pair mPair;

	public BillUrlWriter(BlockingQueue<String> urlQ, BlockingQueue<Pair> proxyQ) { 
		urlQueue = urlQ;
		proxyQueue = proxyQ;
		mPair = proxyQ.remove();
	}

	public void run() {
		try {
			while (true) { 
				consume(urlQueue.take()); 			
			}
		} catch (InterruptedException ex) {
			System.out.println("BillUrlWriter produced exception: " + ex);
			ex.printStackTrace();
		}
	}

	void consume(String url) { 
		try {
			System.out.println("BillUrlWriter trying to write url: " + url + " with proxy addr: " + mPair.getLeft() + " port: " + mPair.getRight());
			boolean written = BillUtil.writeBillFile(url, BillUtil.getProxyInputStream(url, mPair));
			if(written){
				Thread.sleep(BillUtil.RATE_LIMIT);
			}
		} 
		catch(IOException ioe){
			System.out.println("BillUrlWriter consumption produced IOException: " + ioe);
			urlQueue.add(url);
			mPair = proxyQueue.remove();
		} 
		catch (Exception e) {
			System.out.println("BillUrlWriter produced consumption produced exception: " + e);
			e.printStackTrace();
			urlQueue.add(url);
		}
		
	}
}
