package com.weixin.mesage;

/**
 * 微信发送文章实体
 * 
 * @author ming
 * @date 2015-12-17
 */
public class Article {
	//标题
	private String Title = null;
	//描述
	private String Description = null;
	//图片链接
	private String PicUrl = null;
	//图文消息调整路径
	private String Url = null;
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public String getPicUrl() {
		return PicUrl;
	}
	public void setPicUrl(String picUrl) {
		PicUrl = picUrl;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
}
