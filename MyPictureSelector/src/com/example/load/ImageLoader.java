package com.example.load;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ImageLoader {
	//轮询的线程
	private Thread mPoolThread;
	private Handler mPoolThreadHander;
	//引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
	private volatile Semaphore mSemaphore = new Semaphore(1);

	/**
	 * 引入一个值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
	 */
	private volatile Semaphore mPoolSemaphore;
	//线程池
	private ExecutorService mThreadPool;
	//队列的调度方式
	private Type mType = Type.LIFO;
	//任务队列
	private LinkedList<Runnable> mTask;
	/**
	 * 图片缓存的核心类
	 */
	private LruCache<String, Bitmap> mLruCache;

	/**
	 * 运行在UI线程的handler，用于给ImageView设置图片
	 */
	private Handler mHandler;
	
	
	
	
	/**
	 * @author lzh
	 * */
	public enum Type
	{
		FIFO, LIFO
	}
	
	private class ImageBeenHolder{
		Bitmap bitmap;
		ImageView imageView;
		String path;
	}
	
	private class ImageSize
	{
		int width;
		int height;
	}
	
	private static ImageLoader mInstance;

	public ImageLoader(int threadCount, Type type) {
		init(threadCount,type);
	}

	private void init(int threadCount, Type type) {
		mPoolThread = new Thread(){
			@Override
			public void run() {
				super.run();
				try{
					//请求一个信号量
					mSemaphore.acquire();
				}catch(InterruptedException e){
					
				}
				Looper.prepare();
				
				mPoolThreadHander = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						mThreadPool.execute(getTask());
						try{
							mPoolSemaphore.acquire();
						}catch(InterruptedException e){
							
						}
					}
				};
				
				//释放一个信号量
				mSemaphore.release();
				Looper.loop();
			}
		};
		
		mPoolThread.start();
		
		//获取应用程序最大可以内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheSize){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
		
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mPoolSemaphore = new Semaphore(threadCount);
		mTask = new LinkedList<Runnable>();
		mType = type ==null ? Type.LIFO:type;
		
	}

	public static ImageLoader getInstance() {
		if(mInstance == null){
			synchronized (ImageLoader.class) {
				if(mInstance == null){
					mInstance = new ImageLoader(1,Type.LIFO);
				}
			}
		}
		return null;
	}
	
	private synchronized Runnable getTask(){
		if(mType == Type.FIFO){
			return mTask.removeFirst();
		}else{
			return mTask.removeLast();
		}
	}
	
	private synchronized void addTask(Runnable runnable){
		try{
			//请求信号量，防止mPoolThreadHander为null
			if(mPoolThreadHander == null){
				mSemaphore.acquire();
			}
		}catch(InterruptedException e){
			
		}
		mTask.add(runnable);
		mPoolThreadHander.sendEmptyMessage(0x110);
	}

	public void loadImage(final String path,final ImageView imageview){
		//set tag
		imageview.setTag(path);
		//UI线程
		if(mHandler == null ){
			mHandler = new Handler(){
				public void handleMessage(Message msg) {
					ImageBeenHolder holder = (ImageBeenHolder) msg.obj;
					ImageView imageView = holder.imageView;
					Bitmap bm = holder.bitmap;
					String path = holder.path;
					if(imageview.getTag().toString().equals(path)){
						imageview.setImageBitmap(bm);
					}
				}
			};
		}
		
		Bitmap bm = getBitmapFromLruCache(path);
		if(bm != null){
			ImageBeenHolder holder = new ImageBeenHolder();
			holder.bitmap = bm;
			holder.imageView = imageview;
			holder.path = path;
			Message message = Message.obtain();
			message.obj = holder;
			mHandler.sendMessage(message);
		}else{
			addTask(new Runnable() {
				
				@Override
				public void run() {
					ImageSize imageSize = 
				}
			});
		}
	}
	
	
	/**
	 * 从LruCache中获取一张图片，如果不存在就返回null。
	 */
	private Bitmap getBitmapFromLruCache(String key)
	{
		return mLruCache.get(key);
	}
	/**
	 * 根据ImageView获得适当的压缩的高和宽
	 * 
	 * */
	private ImageSize getImageViewWidth(ImageView imageView){
		ImageSize imageSize = 
	}
}
