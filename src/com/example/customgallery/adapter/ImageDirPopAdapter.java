package com.example.customgallery.adapter;

import java.util.List;

import com.example.customgallery.PictureSelectorActivity;
import com.example.customgallery.R;
import com.example.customgallery.bean.ImageFloder;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * pop选择文件夹适配器 Created by zhaoqingyang on 2016/4/26.
 */
public class ImageDirPopAdapter extends BaseAdapter {

	private Context context;

	// 文件夹集合
	private List<ImageFloder> mDatas;

	// 以选择的文件夹
	private String selectDir;

	public ImageDirPopAdapter(Context context, List<ImageFloder> mDatas, String selectDir) {
		this.context = context;
		this.mDatas = mDatas;
		this.selectDir = selectDir;
	}

	@Override
	public int getCount() {
		if (mDatas == null) {
			return 0;
		}
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.item_list_dir, parent, false);
			holder.mImageView = (ImageView) convertView.findViewById(R.id.id_dir_item_image);
			holder.mChoose = (ImageView) convertView.findViewById(R.id.id_dir_item_choose);
			holder.mName = (TextView) convertView.findViewById(R.id.id_dir_item_name);
			holder.mCount = (TextView) convertView.findViewById(R.id.id_dir_item_count);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final ImageFloder item = mDatas.get(position);
		ImageLoader.getInstance().displayImage("file://" + item.getFirstImagePath(), holder.mImageView,
				PictureSelectorActivity.options);
		holder.mName.setText(item.getName());
		holder.mCount.setText(item.getCount() + "张");

		// 设置选中文件夹标志
		if (selectDir.equals(item.getDir())) {
			holder.mChoose.setVisibility(View.VISIBLE);
		} else {
			holder.mChoose.setVisibility(View.GONE);
		}

		return convertView;
	}

	public class ViewHolder {
		private ImageView mImageView;
		private ImageView mChoose;
		private TextView mName;
		private TextView mCount;
	}

	public void setmDatas(List<ImageFloder> mDatas) {
		this.mDatas = mDatas;
	}

	public void setSelectDir(String selectDir) {
		this.selectDir = selectDir;
	}
}
