package concurrency;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.BlockingQueue;

import utils.BillUtil;
import utils.ProxyUtil.Pair;

public class UrlToFileWorker implements Runnable {

	private final BlockingQueue<String> urlQueue;
	private final BlockingQueue<Pair> proxyQueue;
	private Pair mPair;
	private String writePath;

	public UrlToFileWorker(BlockingQueue<String> urlQ, BlockingQueue<Pair> proxyQ, String writePath) { 
		urlQueue = urlQ;
		proxyQueue = proxyQ;
		mPair = proxyQ.remove();
		this.writePath = writePath;
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
			boolean written = BillUtil.writeFile(url, BillUtil.getProxyInputStream(url, mPair), writePath);
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
