package com.weixin.utils;

/**
 * 微信配置文件
 * 
 */
public class WeixinConfig {
	// 与接口配置信息中的Token要一致
	public static final String TOKEN = "eccrevalcom";

	// 商户相关资料
	public static final String APPID = "wx8e7ab6d104dd4eb5";

	public static final String APPSECRET = "44a01656fa4a8a01e6c216e275b9a9b3";
	// 商户号
	public static final String PARTNER = "1275845901";
	public static final String PARTNERKEY = "C795D770F824D9E8F7DA978B3CD29AE4";

	// 通讯成功回调地址
	public static final String NOTIFY_URL = "http://m.xiangxianhui.com/weixin/notify.jspx";

	// 创建永久二维码请求
	public static final String QRCODE_CREATE = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=TOKEN";

	// 获取access_token的接口地址（GET） 限200（次/天）
	public final static String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APPID + "&secret=" + APPSECRET;
}
