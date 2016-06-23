package com.weixin.mesage.req;

/**
 * 语音消息
 * 
 */
public class VoiceMessage extends BaseMessage {
	// 媒体ID
	private String mediaId;
	// 语音格式
	private String format;

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
