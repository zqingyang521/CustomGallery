package com.example.customgallery.adapter;

import java.util.List;

import com.example.customgallery.PictureSelectorActivity;
import com.example.customgallery.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 选择图片你适配器 Created by zhaoqingyang on 2016/4/26.
 */
public class ImageSelectorAdapter extends BaseAdapter {

	private Context context;

	// 文件名集合
	private List<String> mDatas;

	/**
	 * 文件夹路径
	 */
	private String mDirPath;

	private Handler mHandler;

	private int totalSelectCount;

	private List<String> selectList;

	private int size;

	private ImageView sImageView;

	private ImageView sSelect;

	public ImageSelectorAdapter(Context context, List<String> mDatas, List<String> selectList, String dirPath,
			Handler handler, int number, int screenWidth) {
		this.context = context;
		this.mDatas = mDatas;
		this.selectList = selectList;
		this.mDirPath = dirPath;
		this.mHandler = handler;
		this.totalSelectCount = number;

		size = (screenWidth - dip2px(context, 5f) * 4) / 3;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.item_image_selector, parent, false);
			holder.mImageView = (ImageView) convertView.findViewById(R.id.id_item_image);
			holder.mItemLayout = (RelativeLayout) convertView.findViewById(R.id.id_item_layout);
			holder.mSelect = (ImageView) convertView.findViewById(R.id.id_item_select);
			RelativeLayout.LayoutParams logoParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			logoParams.height = size;
			logoParams.width = size;
			holder.mItemLayout.setLayoutParams(logoParams);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final String item = mDatas.get(position);

		ImageLoader.getInstance().displayImage("file://" + mDirPath + "/" + item, holder.mImageView,
				PictureSelectorActivity.options);

		// 设置ImageView的点击事件
		holder.mImageView.setOnClickListener(new OnClickListener() {
			// 选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v) {

				// 已经选择过该图片
				if (selectList.contains(mDirPath + "/" + item)) {
					selectList.remove(mDirPath + "/" + item);
					holder.mSelect.setImageResource(R.drawable.selector_picture_unselected);
					holder.mImageView.setColorFilter(null);

				} else
				// 未选择该图片
				{
					// 当最大选择值为1的时候
					if (totalSelectCount == 1) {
						selectList.clear();
						selectList.add(mDirPath + "/" + item);

						if (sSelect != null) {
							sSelect.setImageResource(R.drawable.selector_picture_unselected);
						}
						if (sImageView != null) {
							sImageView.setColorFilter(null);
						}

						sImageView = holder.mImageView;
						sSelect = holder.mSelect;
						sSelect.setImageResource(R.drawable.selector_pictures_selected);
						sImageView.setColorFilter(Color.parseColor("#77000000"));
					} else {
						if (totalSelectCount > selectList.size()) {
							selectList.add(mDirPath + "/" + item);
							holder.mSelect.setImageResource(R.drawable.selector_pictures_selected);
							holder.mImageView.setColorFilter(Color.parseColor("#77000000"));
						}
					}

				}

				Message msg = new Message();
				msg.what = PictureSelectorActivity.UPDATE_COUNT;
				msg.obj = selectList;
				mHandler.sendMessage(msg);

			}
		});

		/**
		 * 已经选择过的图片，显示出选择过的效果
		 */
		if (selectList.contains(mDirPath + "/" + item)) {
			holder.mSelect.setImageResource(R.drawable.selector_pictures_selected);
			holder.mImageView.setColorFilter(Color.parseColor("#77000000"));
		} else {
			holder.mSelect.setImageResource(R.drawable.selector_picture_unselected);
			holder.mImageView.setColorFilter(null);
		}
		return convertView;
	}

	public class ViewHolder {
		protected RelativeLayout mItemLayout;
		private ImageView mImageView;
		private ImageView mSelect;
	}

	public void setmDatas(List<String> mDatas) {
		this.mDatas = mDatas;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	private int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
