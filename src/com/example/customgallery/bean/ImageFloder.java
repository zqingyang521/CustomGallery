package com.example.customgallery.bean;

/**
 * 文件夹信息(图片扫描) Created by zhaoqingyang on 2016/4/26.
 */
public class ImageFloder {

	// 图片文件夹路径
	private String dir;

	// 第一张图片的路径
	private String firstImagePath;

	// 文件夹名称
	private String name;

	// 当前文件夹的图片的数量
	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
		int lastIndexOf = this.dir.lastIndexOf("/") + 1;
		this.name = this.dir.substring(lastIndexOf);
	}

	public String getFirstImagePath() {
		return firstImagePath;
	}

	public void setFirstImagePath(String firstImagePath) {
		this.firstImagePath = firstImagePath;
	}

	public String getName() {
		return name;
	}

}
