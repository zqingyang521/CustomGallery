package com.example.customgallery;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView oneBtn;

	private TextView moreBtn;

	private TextView detail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		oneBtn = (TextView) findViewById(R.id.oneBtn);
		moreBtn = (TextView) findViewById(R.id.moreBtn);
		detail = (TextView) findViewById(R.id.detail);

		oneBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt(PictureSelectorActivity.SELECT_TOTAL_COUNT, 1);
				Intent intent = new Intent(MainActivity.this, PictureSelectorActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);

			}
		});

		moreBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt(PictureSelectorActivity.SELECT_TOTAL_COUNT, 5);
				Intent intent = new Intent(MainActivity.this, PictureSelectorActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 读取图库图片
		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {

				int selectCount = data.getExtras().getInt("selectCount", 1);

				// 如果最大选择数为1
				if (selectCount == 1) {

					String path = data.getExtras().getStringArrayList("selectList").get(0);

					detail.setText(path);
				} else {
					ArrayList<String> paths = data.getExtras().getStringArrayList("selectList");

					detail.setText(paths.toString());
				}

			}

		}
	}

}
