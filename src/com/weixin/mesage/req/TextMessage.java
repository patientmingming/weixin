package com.weixin.mesage.req;
/**
 * 文本消息
 * @author ming
 * @date 2015-12-16
 */
public class TextMessage extends BaseMessage {
	// 消息内容
	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}