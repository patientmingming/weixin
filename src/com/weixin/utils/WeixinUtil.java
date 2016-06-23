package com.weixin.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale.Category;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import com.weixin.mesage.Article;
import com.weixin.mesage.resp.NewsMessage;
import com.weixin.mesage.resp.TextMessage;
import com.weixin.utils.http.HttpClientConnectionManager;

public class WeixinUtil {
	private static CloseableHttpClient httpclient = HttpClients.createDefault();

	/**
	 * 扫码支付生成预支付订单，返回map中code_url的值为待支付二维码链接
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param money
	 *            支付金额
	 * @param body
	 *            商品描述
	 * @param productId
	 *            商品id
	 * @param notifyUrl
	 *            回调地址
	 * @param attach
	 *            附加数据非
	 * @param tradeNo
	 *            订单号
	 * @return 返回结果xml封装成map
	 */
	public static Map<String, Object> weixinScanPay(HttpServletRequest request, HttpServletResponse response, float money, String body, String productId, String notifyUrl, String attach, String tradeNo) {
		// 金额转化为分为单位
		String finalmoney = String.format("%.2f", money);
		finalmoney = finalmoney.replace(".", "");

		// 商户相关资料
		String appid = WeixinConfig.APPID;
		String appsecret = WeixinConfig.APPSECRET;
		String partner = WeixinConfig.PARTNER;// 商户号
		String partnerkey = WeixinConfig.PARTNERKEY;

		String currTime = TenpayUtil.getCurrTime();
		// 8位日期
		String strTime = currTime.substring(8, currTime.length());
		// 四位随机数
		String strRandom = TenpayUtil.buildRandom(4) + "";
		// 10位序列号,可以自行调整。
		String strReq = strTime + strRandom;

		// 商户号
		String mch_id = partner;
		// 随机数
		String nonce_str = strReq;

		// 商户订单号
		String out_trade_no = tradeNo;
		int total_fee = Integer.parseInt(finalmoney);

		// 订单生成的机器 IP
		String spbill_create_ip = request.getRemoteAddr();

		String trade_type = "NATIVE";
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("attach", attach);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("product_id", productId);
		packageParams.put("total_fee", total_fee + "");
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notifyUrl);
		packageParams.put("trade_type", trade_type);

		RequestHandler reqHandler = new RequestHandler(request, response);
		reqHandler.init(appid, appsecret, partnerkey);

		String sign = reqHandler.createSign(packageParams);
		String xml = "<xml>";
		xml += "<appid>" + appid + "</appid>";
		xml += "<mch_id>" + mch_id + "</mch_id>";
		xml += "<nonce_str>" + nonce_str + "</nonce_str>";
		xml += "<sign>" + sign + "</sign>";
		xml += "<body><![CDATA[" + body + "]]></body>";
		xml += "<attach>" + attach + "</attach>";
		xml += "<out_trade_no>" + out_trade_no + "</out_trade_no>";
		xml += "<total_fee>" + total_fee + "</total_fee>";
		xml += "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>";
		xml += "<product_id>" + productId + "</product_id>";
		xml += "<notify_url>" + notifyUrl + "</notify_url>";
		xml += "<trade_type>" + trade_type + "</trade_type>";
		xml += "</xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		Map<String, Object> map = getPostResult(createOrderURL, xml);
		return map;
	}

	public static Map<String, Object> getPostResult(String url, String xmlParam) {
		System.out.println("xml是:" + xmlParam);
		HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
		try {
			httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
			HttpResponse response = httpclient.execute(httpost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println("返回结果\n " + jsonStr);
			Map<String, Object> map = doXMLParse(jsonStr);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
	 * 
	 * @param strxml
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Map<String, Object> doXMLParse(String strxml) throws Exception {
		if (null == strxml || "".equals(strxml)) {
			return null;
		}

		Map<String, Object> m = new HashMap<String, Object>();
		InputStream in = String2Inputstream(strxml);
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		Element root = doc.getRootElement();
		List list = root.getChildren();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Element e = (Element) it.next();
			String k = e.getName();
			String v = "";
			List children = e.getChildren();
			if (children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = getChildrenText(children);
			}

			m.put(k, v);
		}

		// 关闭流
		in.close();

		return m;
	}

	/**
	 * 获取子结点的xml
	 * 
	 * @param children
	 * @return String
	 */
	public static String getChildrenText(List children) {
		StringBuffer sb = new StringBuffer();
		if (!children.isEmpty()) {
			Iterator it = children.iterator();
			while (it.hasNext()) {
				Element e = (Element) it.next();
				String name = e.getName();
				String value = e.getTextNormalize();
				List list = e.getChildren();
				sb.append("<" + name + ">");
				if (!list.isEmpty()) {
					sb.append(getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}

		return sb.toString();
	}

	/**
	 * 创建永久二维码，调用创建二维码接口得到对应的二维码链接
	 * 
	 * @param scene_str
	 *            自定义的二维码参数值，该参数值在扫码成功后传递给服务端
	 * @return 二维码的链接地址
	 */
	public static String createQrCode(String scene_str) {
		// 生成token
		Map<String, Object> map = getAccessToken();
		String token = (String) map.get("token");
		if (token != null) {
			String url = WeixinConfig.QRCODE_CREATE.replace("TOKEN", token);
			HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
			String postData = "{\"action_name\": \"QR_LIMIT_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"" + scene_str + "\"}}}";
			httpost.setEntity(new StringEntity(postData, "UTF-8"));
			HttpResponse response = null;
			try {
				response = httpclient.execute(httpost);
				String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
				JSONObject jsonObject = new JSONObject(jsonStr);
				return jsonObject.optString("url");
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 获取access_token
	 * 
	 * @param appid
	 *            凭证
	 * @param appsecret
	 *            密钥
	 * @return
	 */
	public static Map<String, Object> getAccessToken() {
		Map<String, Object> map = new HashMap<String, Object>();
		String requestUrl = WeixinConfig.ACCESS_TOKEN_URL;
		HttpGet httGet = HttpClientConnectionManager.getGetMethod(requestUrl);
		HttpResponse response = null;
		try {
			response = httpclient.execute(httGet);
			String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
			JSONObject jsonObject = new JSONObject(jsonStr);
			// 如果请求成功
			if (null != jsonObject) {
				map.put("token", jsonObject.optString("access_token"));
				map.put("expiresIn", jsonObject.optInt("expires_in"));
			}
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return map;
	}

	/**
	 * 处理微信发来的请求
	 * 
	 * @param request
	 * @return
	 */
	public static String processRequest(HttpServletRequest request) {
		String respMessage = null;
		try {
			// 默认返回的文本消息内容
			String respContent = "";

			String xml = IOUtils.toString(request.getInputStream(), "UTF-8");
			// xml请求解析
			Map<String, Object> requestMap = XmlUtil.doXMLParse(xml);

			// 发送方帐号（open_id）
			String fromUserName = (String) requestMap.get("FromUserName");
			// 公众帐号
			String toUserName = (String) requestMap.get("ToUserName");
			// 消息类型
			String msgType = (String) requestMap.get("MsgType");
			// 按钮key
			String eventKey = (String) requestMap.get("EventKey");
			// 内容
			String content = (String) requestMap.get("Content");

			// 回复文本消息
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			textMessage.setFuncFlag(0);

			// 文本消息
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				// respContent = "您发送的是文本消息！";
			} else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {// 图片消息
				// respContent = "您发送的是图片消息！";
			} else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {// 地理位置消息
				// respContent = "您发送的是地理位置消息！";
			} else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {// 链接消息
				// respContent = "您发送的是链接消息！";
			} else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {// 音频消息
				// respContent = "您发送的是音频消息！";
			} else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {// 事件推送
				// 事件类型
				String eventType = (String) requestMap.get("Event");
				// 订阅
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					// 第一次关注
					respContent = "";
					// 获得动态二维码参数值 如：qrscene_123
					if (eventKey.indexOf("qrscene_") == 0) {
						String params = eventKey.replace("qrscene_", "");
						// 得到商品id和活动id 生成图文消息推送给用户

						/** 参考代码如下 
						List<Article> articleList = new ArrayList<Article>();
						// 创建图文消息
						NewsMessage newsMessage = new NewsMessage();
						newsMessage.setToUserName(fromUserName);
						newsMessage.setFromUserName(toUserName);
						newsMessage.setCreateTime(new Date().getTime());
						newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
						newsMessage.setFuncFlag(0);

						// 获取文章内容
						Article art = new Article();
						art.setTitle("测试");
						art.setDescription("测试文章内容描述");
						art.setPicUrl("https://img.yzcdn.cn/upload_files/2015/05/14/Fvwmfd5yCOCSL60EHxmO0WUqguM3.png");// 图文消息图片地址
						art.setUrl("https://wap.koudaitong.com/v2/showcase/goods?alias=3nrp5kjeohsgw&scan=1&activity=none&from=kdt");
						articleList.add(art);

						newsMessage.setArticleCount(articleList.size());
						newsMessage.setArticles(articleList);
						*/
					}
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {// 扫码
					String params = eventKey;
					// 得到商品id和活动id 生成图文消息推送给用户

					/** 参考代码如下 
					List<Article> articleList = new ArrayList<Article>();
					// 创建图文消息
					NewsMessage newsMessage = new NewsMessage();
					newsMessage.setToUserName(fromUserName);
					newsMessage.setFromUserName(toUserName);
					newsMessage.setCreateTime(new Date().getTime());
					newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
					newsMessage.setFuncFlag(0);

					// 获取文章内容
					Article art = new Article();
					art.setTitle("测试");
					art.setDescription("测试文章内容描述");
					art.setPicUrl("https://img.yzcdn.cn/upload_files/2015/05/14/Fvwmfd5yCOCSL60EHxmO0WUqguM3.png");// 图文消息图片地址
					art.setUrl("https://wap.koudaitong.com/v2/showcase/goods?alias=3nrp5kjeohsgw&scan=1&activity=none&from=kdt");
					articleList.add(art);

					newsMessage.setArticleCount(articleList.size());
					newsMessage.setArticles(articleList);
					*/
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) { // 取消订阅
				} else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) { // 自定义菜单点击事件
				}
			}

			textMessage.setContent(respContent);

			respMessage = MessageUtil.textMessageToXml(textMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return respMessage;
	}

	/**
	 * 验证签名
	 * 
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @return
	 */
	public static boolean checkSignature(String signature, String timestamp, String nonce) {

		String[] arr = new String[] { WeixinConfig.TOKEN, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典序排序
		Arrays.sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = null;
		String tmpStr = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密
			byte[] digest = md.digest(content.toString().getBytes());
			tmpStr = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		content = null;

		// 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
		return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;

	}

	/**
	 * 将字节数组转换为十六进制字符串
	 * 
	 * @param byteArray
	 * @return
	 */
	private static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/**
	 * 将字节转换为十六进制字符串
	 * 
	 * @param mByte
	 * @return
	 */
	private static String byteToHexStr(byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];

		String s = new String(tempArr);
		return s;
	}

	private static InputStream String2Inputstream(String str) {
		return new ByteArrayInputStream(str.getBytes());
	}

}