package com.example.customgallery;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.example.customgallery.adapter.ImageSelectorAdapter;
import com.example.customgallery.bean.ImageFloder;
import com.example.customgallery.view.ListImageDirPopupWindow;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 图片选择页面 Created by zhaoqingyang on 2016/4/26.
 */
public class PictureSelectorActivity extends Activity implements ListImageDirPopupWindow.OnImageDirSelected {

	public static final String SELECT_TOTAL_COUNT = "selectTotalCount";

	// 更新选择数量
	public static final int UPDATE_COUNT = 0x110;

	// 更新数据
	public static final int UPDATE_DATA = 0x111;

	// 等待dialog
	private ProgressDialog mProgressDialog;

	// 文件夹选择pop
	private ListImageDirPopupWindow mListImageDirPopupWindow;

	// 总共要选择的数量
	private int totalSelectCount = 1;

	// 文件夹路径集合(辅助查询)
	private HashSet<String> mDirPaths = new HashSet<String>();

	// 图片总数量
	private int totalCount;

	// 图片最多的文件夹中的图片数量
	private int mPicsSize;

	// 图片最多的文件夹
	private File mImgDir;

	// 底部布局
	private RelativeLayout mBottomLy;

	// 返回按钮
	private TextView returnBtn;

	// 确定按钮
	private TextView sureBtn;

	// 图片展示gridview
	private GridView mGridView;

	// 显示选择文件夹
	private TextView mChooseDir;

	// 显示图片总数
	private TextView mImageCount;

	private List<ImageFloder> imageFloders = new ArrayList<ImageFloder>();

	// 所有的图片
	private List<String> mImgs;

	private ImageSelectorAdapter mAdapter;

	// 屏幕高度
	private int mScreenHeight;

	// 屏幕宽度
	private int mScreenWidth;

	// 已选择的图片
	private ArrayList<String> selectList = new ArrayList<String>();

	public static DisplayImageOptions options;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_picture_selector);

		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		mScreenHeight = outMetrics.heightPixels;
		mScreenWidth = outMetrics.widthPixels;

		// 默认值为1
		totalSelectCount = getIntent().getExtras().getInt(SELECT_TOTAL_COUNT, 1);

		initImageLoader();

		initView();

		initDatas();

		initEvent();

	}

	/**
	 * 初始化imageLoader 为了简化功能需求 options 为静态变量
	 */
	private void initImageLoader() {
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
		config.threadPoolSize(3);// 线程池内加载的数量
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();// 不缓存图片的多种尺寸在内存中
		config.discCacheFileNameGenerator(new Md5FileNameGenerator());// 将保存的时候的URI名称用MD5
		config.discCacheSize(50 * 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		config.writeDebugLogs();// Remove for release app
		// 初始化ImageLoader
		ImageLoader.getInstance().init(config.build());

		if (options == null) {
			options = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_launcher)// 设置图片在下载期间显示的图片
					.showImageForEmptyUri(R.drawable.ic_launcher)// 设置图片Uri为空或是错误的时候显示的图片
					.showImageOnFail(R.drawable.ic_launcher)// 设置图片加载/解码过程中错误时候显示的图片
					.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
					.cacheOnDisc(true)// 设置下载的资源是否缓存在SD卡中
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)// 设置图片以何种编码方式显示
					.bitmapConfig(Bitmap.Config.RGB_565) // 设置图片的解码类型
					.displayer(new RoundedBitmapDisplayer(20))// 是否设置为圆角，弧度为多少
					.displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
					.build();
		}

	}

	/**
	 * 初始化数据
	 */
	private void initDatas() {

		// 判断外部存储是否可读写
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}

		mProgressDialog = ProgressDialog.show(this, null, "正在加载，请稍候...");

		// 开启线程进行查询
		new Thread(new Runnable() {
			@Override
			public void run() {

				// 获取外部存储图片库地址
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

				ContentResolver mContentResolver = PictureSelectorActivity.this.getContentResolver();

				// 只查询jpeg和png的图片
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);

				// 遍历查询结果
				while (mCursor.moveToNext()) {
					// 获取图片路径
					String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));

					// 获取该图片的父文件夹
					File parentFile = new File(path).getParentFile();

					if (parentFile == null) {
						continue;
					}

					// 获取父文件夹的绝对路径
					String dirPath = parentFile.getAbsolutePath();

					ImageFloder imageFloder = null;

					// 如果该文件夹路径已保存
					if (mDirPaths.contains(dirPath)) {
						continue;
					} else {
						// 添加文件夹路径
						mDirPaths.add(dirPath);

						// 初始化imageFloder
						imageFloder = new ImageFloder();
						imageFloder.setDir(dirPath);
						imageFloder.setFirstImagePath(path);
					}

					if (parentFile.list() == null) {
						continue;
					}

					// 图片数量
					int picSize = parentFile.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg")) {
								return true;
							}
							return false;
						}
					}).length;

					// 总数量累加
					totalCount += picSize;

					imageFloder.setCount(picSize);

					imageFloders.add(imageFloder);

					// 比较数量最大的文件夹
					if (picSize > mPicsSize) {
						mPicsSize = picSize;
						mImgDir = parentFile;
					}

				}

				// 关闭查询
				mCursor.close();

				// 清除辅组查询数组
				mDirPaths.clear();
				mDirPaths = null;

				mHandler.sendEmptyMessage(UPDATE_DATA);

			}
		}).start();

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE_DATA:
				mProgressDialog.dismiss();
				// 为View绑定数据
				data2View();
				// 初始化展示文件夹的popupWindw
				initListDirPopupWindw();
				break;

			case UPDATE_COUNT:

				selectList = (ArrayList<String>) (msg.obj);

				if (selectList.size() == 0) {

					sureBtn.setText("确定");
					sureBtn.setEnabled(false);
				} else {
					sureBtn.setText("确定(" + selectList.size() + "/" + totalSelectCount + ")");
					sureBtn.setEnabled(true);
				}
				break;

			default:
				break;
			}

		}
	};

	/**
	 * 初始化视图
	 */

	private void initView() {

		returnBtn = (TextView) findViewById(R.id.returnBtn);

		sureBtn = (TextView) findViewById(R.id.sureBtn);

		sureBtn.setEnabled(false);

		mGridView = (GridView) findViewById(R.id.gridView);

		mBottomLy = (RelativeLayout) findViewById(R.id.bottom_ly);

		mChooseDir = (TextView) findViewById(R.id.choose_dir);

		mImageCount = (TextView) findViewById(R.id.total_count);
	}

	private void initEvent() {
		/**
		 * 为底部的布局设置点击事件，弹出popupWindow
		 */
		mBottomLy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListImageDirPopupWindow.setAnimationStyle(R.style.anim_popup_dir);
				mListImageDirPopupWindow.setSelectDir(mImgDir.getAbsolutePath());
				mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);

				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 0.3f;
				getWindow().setAttributes(lp);
			}
		});

		sureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setOnResult();

			}
		});

		returnBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	/**
	 * 为View绑定数据
	 */
	private void data2View() {
		if (mImgDir == null) {
			Toast.makeText(getApplicationContext(), "没有扫描到图片", Toast.LENGTH_SHORT).show();
			return;
		}

		initFileList();
		mImageCount.setText(mImgs.size() + "张");
		mChooseDir.setText(mImgDir.getName());
	}

	/**
	 * 初始化文件目录
	 */
	private void initFileList() {
		mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg"))
					return true;
				return false;
			}
		}));

		Collections.reverse(mImgs);

		mAdapter = new ImageSelectorAdapter(this, mImgs, selectList, mImgDir.getAbsolutePath(), mHandler,
				totalSelectCount, mScreenWidth);
		mGridView.setAdapter(mAdapter);
	}

	/**
	 * 初始化展示文件夹的popupWindw
	 */
	private void initListDirPopupWindw() {
		mListImageDirPopupWindow = new ListImageDirPopupWindow(GridLayout.LayoutParams.MATCH_PARENT,
				(int) (mScreenHeight * 0.7), imageFloders, mImgDir.getAbsolutePath(),
				LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_list_dir, null));

		mListImageDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
			}
		});
		// 设置选择文件夹的回调
		mListImageDirPopupWindow.setOnImageDirSelected(this);
	}

	@Override
	public void selected(ImageFloder floder) {

		mImgDir = new File(floder.getDir());
		initFileList();
		mImageCount.setText(floder.getCount() + "张");
		mChooseDir.setText(floder.getName());
		mListImageDirPopupWindow.dismiss();
	}

	/**
	 * 返回值
	 */
	private void setOnResult() {
		Intent in = new Intent();

		Bundle bundle = new Bundle();

		bundle.putInt("selectCount", totalSelectCount);

		bundle.putStringArrayList("selectList", selectList);

		in.putExtras(bundle);

		setResult(RESULT_OK, in);

		finish();
	}

}
