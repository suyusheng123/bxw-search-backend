package com.bxw.springbootinit.utils;

import javax.net.ssl.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
/**
 * ClassName:InstallCertUtils
 * Package:com.bxw.springbootinit.utils
 * Description:
 * ssl证书工具类
 * @Author 卜翔威
 * @Create 2024/8/10 18:39
 * @Version 1.0
 */
public class InstallCertUtils {
	public static final String path = System.getProperty("java.home") + File.separator + "lib" + File.separator + "security";//Java秘钥库存放位置

	public static void main(String[] args) throws Exception {
		String hostname = "crescointl.com";//网站地址
		String passphrase = "changeit";//信任证书默认密钥changeit
		createCert(hostname, passphrase);//创建证书
	}

	public static void createCert(String hostname, String passphrase) throws Exception {
		String host;
		int port;
		char[] passphrases;
		if (hostname != null && !hostname.isEmpty()) {
			String[] c = hostname.split(":");
			host = c[0];
			port = (c.length == 1) ? 443 : Integer.parseInt(c[1]);
			passphrases = passphrase.toCharArray();
		} else {
			System.out.println("请正确输入服务器地址: <host>[:port]");
			return;
		}

		System.out.println("加载秘钥存储库数据");

		File certs = new File(path, "cacerts");
		InputStream certsInputStream = null;
		if (certs.exists() && certs.isFile()) {
			certsInputStream = Files.newInputStream(certs.toPath());
		}

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(certsInputStream, passphrases);
		if (certsInputStream != null) {
			certsInputStream.close();
		}

		File file = new File(path, "jssecacerts");
		InputStream inputStream = null;
		if (file.exists()) {
			if (!file.isFile()) {
				System.out.println("存在文件夹: " + file.getPath());
				return;
			} else {
				if (file.length() > 0) {
					inputStream = Files.newInputStream(file.toPath());
				}
			}
		}
		KeyStore customKs = KeyStore.getInstance(KeyStore.getDefaultType());
		customKs.load(inputStream, passphrases);
		if (inputStream != null) {
			inputStream.close();
		}

		//将jssecacerts中证书全部加载到keyStore库中
		Enumeration<String> aliases = customKs.aliases();
		if (aliases != null) {
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				Certificate certificate = customKs.getCertificate(alias);
				keyStore.setCertificateEntry(alias, certificate);
			}
		}

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);
		X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[]{tm}, null);
		SSLSocketFactory factory = context.getSocketFactory();

		System.out.println("打开链接 " + host + ":" + port);
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);
		try {
			System.out.println("开始SSL握手......");
			socket.startHandshake();
			socket.close();
			System.out.println("没有错误，证书已受信任");
			return;
		} catch (SSLException e) {
			System.out.println();
			System.out.println("SSL握手失败: " + e.getMessage());
		}

		X509Certificate[] chain = tm.chain;
		if (chain == null) {
			System.out.println("无法获取服务器证书链");
			return;
		}

		System.out.println();
		System.out.println("服务器发送了 " + chain.length + " 个证书: ");
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < chain.length; i++) {
			X509Certificate cert = chain[i];
			System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
			System.out.println("   Issuer  " + cert.getIssuerDN());
			sha1.update(cert.getEncoded());
			System.out.println("   sha1    " + toHexString(sha1.digest()));
			md5.update(cert.getEncoded());
			System.out.println("   md5     " + toHexString(md5.digest()));
			System.out.println();
		}

		X509Certificate cert = chain[1];
		String alias = host;
		customKs.setCertificateEntry(alias, cert);

		//如果文件不存在则创建文件
		if (!file.exists()) {
			if (file.createNewFile()) {
				System.out.println("创建 jssecacerts 秘钥库成功");
			}
		}
		OutputStream os = Files.newOutputStream(file.toPath());
		customKs.store(os, passphrases);
		os.close();

		System.out.println();
		System.out.println("已使用别名 " + alias + " 将证书添加到秘钥存储库 jssecacerts 中");
		System.out.println("jssecacerts 路径: " + path);
		System.out.println("可使用: keytool -list -keystore jssecacerts -storepass changeit 命令查看秘钥库信息");
	}

	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	private static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (int b : bytes) {
			b &= 0xff;
			sb.append(HEX_DIGITS[b >> 4]);
			sb.append(HEX_DIGITS[b & 15]);
			sb.append(' ');
		}
		return sb.toString();
	}

	private static class SavingTrustManager implements X509TrustManager {

		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		public X509Certificate[] getAcceptedIssuers() {
			return chain;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}
}
