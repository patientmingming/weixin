package com.weixin.mesage.resp;

/**
 * 效应消息，文本消息
 * 
 * @author ming
 * @date 2015-12-16
 */
public class TextMessage extends BaseMessage {
	// 回复的消息内容
	private String Content;

	public String getContent() {
		return Content;
	}

	public void setContent(String Content) {
		this.Content = Content;
	}
}
