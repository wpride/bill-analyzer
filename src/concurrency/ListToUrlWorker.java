package concurrency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import utils.BillUtil;
import utils.ProxyUtil.Pair;

public class ListToUrlWorker implements Runnable {

	private final BlockingQueue<Pair> proxyQueue;
	private final BlockingQueue<Pair> rangeQueue;
	private final BlockingQueue<String> urlQueue;
	private Pair mProxy;
	private int congressCode;
	private String billCode;
	private String key;

	public ListToUrlWorker(BlockingQueue<Pair> proxyQ, BlockingQueue<Pair> rangeQ, BlockingQueue<String> urlQ,
			int congressCode, String billCode, String key) { 
		proxyQueue = proxyQ;
		rangeQueue = rangeQ;
		urlQueue = urlQ;
		mProxy = proxyQueue.remove();
		this.congressCode = congressCode;
		this.billCode = billCode;
		this.key = key;
	}

	public void run() {
		try {
			while (true) { 
				consume(rangeQueue.take()); 
			}
		} catch (InterruptedException ex) {
			System.out.println("BillUrlWriter produced exception: " + ex);
			ex.printStackTrace();
		}
	}

	void consume(Pair range) { 
		try {
			
			String url = BillUtil.getBillPage(111, "hr", range.getLeft(), range.getRight());
			
			ArrayList<String> returnedURLs = BillUtil.getKeyRef(url, BillUtil.getProxyInputStream(url, mProxy), key);
			
			urlQueue.addAll(returnedURLs);
			
			if(true){
				Thread.sleep(BillUtil.RATE_LIMIT);
			}
		} 
		catch(IOException ioe){
			System.out.println("BillUrlWriter consumption produced IOException: " + ioe);
			rangeQueue.add(range);
			mProxy = proxyQueue.remove();
		} 
		catch (Exception e) {
			System.out.println("BillUrlWriter produced consumption produced exception: " + e);
			e.printStackTrace();
			rangeQueue.add(range);
		}
		
	}
}