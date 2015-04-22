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
	private boolean relative;

	public ListToUrlWorker(BlockingQueue<Pair> proxyQ, BlockingQueue<Pair> rangeQ, BlockingQueue<String> urlQ,
			int congressCode, String billCode, String key, boolean relative) {
		proxyQueue = proxyQ;
		rangeQueue = rangeQ;
		urlQueue = urlQ;
		mProxy = proxyQueue.remove();
		this.congressCode = congressCode;
		this.billCode = billCode;
		this.key = key;
		this.relative = relative;
	}

	public void run() {
		try {
			while (true) { 
				consume(rangeQueue.take()); 
			}
		} catch (InterruptedException ex) {
			System.out.println("ListToUrlWorker produced exception: " + ex);
			ex.printStackTrace();
		}
	}

	void consume(Pair range) {
		
		try {
			
			String url = BillUtil.getBillPage(111, BillUtil.BILL_CODE, range.getLeft(), range.getRight());
			
			ArrayList<String> returnedURLs = BillUtil.getKeyRef(url, BillUtil.getProxyInputStream(url, mProxy), key, relative);
			
			for(String cur: returnedURLs){
				
				System.out.println("cur: " + cur);
				
				if(cur.contains("ih&")){
					urlQueue.add(cur);
				}
			}
			
			if(true){
				Thread.sleep(BillUtil.RATE_LIMIT);
			}
		} 
		catch(IOException ioe){
			System.out.println("ListToUrlWorker consumption produced IOException: " + ioe);
			rangeQueue.add(range);
			try {
				mProxy = proxyQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		catch (Exception e) {
			System.out.println("ListToUrlWorker produced consumption produced exception: " + e);
			try {
				Thread.sleep(1000000);
			} catch (InterruptedException e1) {
			}
			rangeQueue.add(range);
		}
		
	}
}