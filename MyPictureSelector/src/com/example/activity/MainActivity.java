package com.example.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.example.mypictureselector.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private GridView gridView;
	private ProgressDialog progressDialog;
	//临时的辅助类，用于防止同一个文件夹多长扫描
	private HashSet<String> mDirPath = new HashSet<String>();
	//存储文件夹中的图片数量
	private int mPicSize;
	//图片数量最多的文件夹
	private File mImgDir;
	//所以图片
	private List<String> mImgs;
	private ListAdapter adapter;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			progressDialog.dismiss();
			mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					if(filename.endsWith(".jpg")){
						return true;
					}
					return false;
				}
			}));
			
			adapter = new
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		gridView = (GridView) findViewById(R.id.id_gridView);
		getImage();
	}

	private void getImage() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		
		progressDialog = ProgressDialog.show(this, null, "正在加载");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Uri mImage = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver contentResolver = MainActivity.this.getContentResolver();
				
				Cursor cursor = contentResolver.query(mImage, null, MediaStore.Images.Media.MIME_TYPE + "=? or" + MediaStore.Images.Media.MIME_TYPE + "=?", new String[]{"image/jpeg","image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
				while(cursor.moveToNext()){
					String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
					File parentFile = new File(path).getParentFile();
					String dirPath = parentFile.getAbsolutePath();
					
					if(mDirPath.contains(dirPath)){
						continue;
					}else{
						mDirPath.add(dirPath);
					}
					
					int picSize = parentFile.list(new FilenameFilter() {
						
						@Override
						public boolean accept(File dir, String filename) {
							if(filename.endsWith(".jpg")){
								return true;
							}
							
							return false;
						}
					}).length;
					
					if(picSize > mPicSize){
						mPicSize = picSize;
						mImgDir  = parentFile;
					}
				}
				cursor.close();
				mDirPath = null;
				
			}
		});
	}
}
