package concurrency;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import utils.BillUtil;
import utils.ProxyUtil.Pair;

public class UrlToFileWorker implements Runnable {

	private final BlockingQueue<String> urlQueue;
	private final BlockingQueue<Pair> proxyQueue;
	private Pair mPair;
	private String writePath;

	public UrlToFileWorker(BlockingQueue<Pair> proxyQ, BlockingQueue<String> urlQ, String writePath) { 
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
			System.out.println("UrlToFileWorker produced exception: " + ex);
			ex.printStackTrace();
		}
	}

	void consume(String url) { 
		try {
			System.out.println("UrlToFileWorker trying to write url: " + url + " with proxy addr: " + mPair.getLeft() + " port: " + mPair.getRight());
			boolean written = BillUtil.writeFile(url, BillUtil.getProxyInputStream(url, mPair), writePath);
			if(written){
				System.out.println("UrlToFileWorker added " + url + " to path " + writePath);
				Thread.sleep(BillUtil.RATE_LIMIT);
			}
		} 
		catch(IOException ioe){
			System.out.println("UrlToFileWorker consumption produced IOException: " + ioe);
			urlQueue.add(url);
			try {
				mPair = proxyQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		catch (Exception e) {
			System.out.println("UrlToFileWorker produced consumption produced exception: " + e);
			e.printStackTrace();
			urlQueue.add(url);
		}
		
	}
}
