package com.weixin.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.JDOMException;
import org.json.JSONException;
import com.weixin.utils.WeixinUtil;
import com.weixin.utils.Qrcode;
import com.weixin.utils.ResponseUtils;
import com.weixin.utils.XmlUtil;

/**
 * 微信处理
 * 
 */
public class WeixinAct {

	/**
	 * 
	 * @param signature
	 *            微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
	 * @param timestamp
	 *            时间戳
	 * @param nonce
	 *            随机数
	 * @param echostr
	 *            随机数
	 * @return
	 */
	// @RequestMapping(value = "", method = RequestMethod.GET)
	public String get(String signature, String timestamp, String nonce, String echostr, HttpServletRequest request, HttpServletResponse response) {

		// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
		if (WeixinUtil.checkSignature(signature, timestamp, nonce)) {
			return echostr;
		}

		return "";
	}

	// @RequestMapping(value = "", method = RequestMethod.POST)
	public void post(String signature, String timestamp, String nonce, String echostr, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 调用核心业务类接收消息、处理消息
		String respMessage = WeixinUtil.processRequest(request);

		// 响应消息
		ResponseUtils.renderJson(response, respMessage);
	}

	/**
	 * 通知
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws JDOMException
	 */
	// @RequestMapping(value = { "/weixin/notify.jspx" })
	public void notify(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JDOMException {

		InputStream inStream = request.getInputStream();
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		String result = new String(outSteam.toByteArray(), "utf-8");// 获取微信调用我们notify_url的返回信息

		Map<String, Object> map = XmlUtil.doXMLParse(result);
		if (map.get("result_code").toString().equalsIgnoreCase("SUCCESS")) {

		}

	}

	/**
	 * 微信扫码支付
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @throws ServletException
	 * @throws IOException
	 * @throws JSONException
	 */
	// @RequestMapping(value = { "/weixin/weixinScanPay.jspx" })
	public void weixinScanPay(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
		// 支付金额
		float money = new Float(request.getParameter("money"));
		// 支付内容
		String body = request.getParameter("body");
		// 产品ID
		String productId = request.getParameter("productId");
		// 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
		String notifyUrl = "http://m.xiangxianhui.com/weixin/notify.jspx";
		// 其他内容非必输
		String attach = request.getParameter("attach");
		// 订单交易码
		String tradeNo = request.getParameter("tradeNo");
		Map<String, Object> map = WeixinUtil.weixinScanPay(request, response, money, body, productId, notifyUrl, attach, tradeNo);

		ResponseUtils.renderText(response, (String) map.get("code_url"));
	}

	/**
	 * 生成二维码
	 * 
	 * @param content
	 * @param request
	 * @param response
	 */
	// @RequestMapping({ "/order/qrCode.jspx" })
	public void createQRCode(String content, HttpServletRequest request, HttpServletResponse response) {
		Qrcode.createQRCoder(content, response);
	}
}
