package com.bxw.springbootinit.utils;


import cn.hutool.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * ClassName:TrustAllCertManager
 * Package:com.bxw.springbootinit.utils
 * Description:
 * 忽略证书
 *
 * @Author 卜翔威
 * @Create 2024/8/10 19:06
 * @Version 1.0
 */
public class TrustAllCertManagerUtils implements X509TrustManager {
	@Override
	public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}


	public static void rawDataHomePage(String url) {
		// 创建自定义 TrustManager
		TrustManager[] trustManagers = new TrustManager[]{new TrustAllCertManagerUtils()};

		// 获取默认的 SSLContext
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustManagers, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
		// 应用自定义的 SSLContext
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	}
}
