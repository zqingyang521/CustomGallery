package com.example.customgallery.view;

import java.util.List;

import com.example.customgallery.R;
import com.example.customgallery.adapter.ImageDirPopAdapter;
import com.example.customgallery.bean.ImageFloder;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

/**
 * 文件夹选择popwindow Created by zhaoqingyang on 2016/4/26.
 */
public class ListImageDirPopupWindow extends PopupWindow {

	// 布局文件的最外层View
	protected View mContentView;

	protected Context context;

	// ListView的数据集
	protected List<ImageFloder> mDatas;

	// 显示文件夹ListView
	private ListView mListDir;

	// 文件夹选择的回调
	private OnImageDirSelected mImageDirSelected;

	private ImageDirPopAdapter imageDirPopAdapter;

	// 已选中的文件夹路径
	private String selectDir;

	public ListImageDirPopupWindow(int width, int height, List<ImageFloder> datas, String selectDir, View convertView) {
		super(convertView, width, height, true);
		this.selectDir = selectDir;
		this.mContentView = convertView;
		this.mDatas = datas;
		context = convertView.getContext();
		setBackgroundDrawable(new BitmapDrawable());
		setTouchable(true);
		setOutsideTouchable(true);
		setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					dismiss();
					return true;
				}
				return false;
			}
		});
		initViews();
		initEvents();
	}

	public void initViews() {
		mListDir = (ListView) mContentView.findViewById(R.id.id_list_dir);

		imageDirPopAdapter = new ImageDirPopAdapter(context, mDatas, selectDir);

		mListDir.setAdapter(imageDirPopAdapter);
	}

	public interface OnImageDirSelected {
		void selected(ImageFloder floder);
	}

	public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected) {
		this.mImageDirSelected = mImageDirSelected;
	}

	public void initEvents() {
		mListDir.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (mImageDirSelected != null) {
					mImageDirSelected.selected(mDatas.get(position));
				}
			}
		});
	}

	public void setSelectDir(String selectDir) {
		this.selectDir = selectDir;
		imageDirPopAdapter.setSelectDir(selectDir);
	}

}
