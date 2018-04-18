package org.apache.struts.apps.mailreader.actions;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import brave.http.HttpTracing;
import org2.apache.brave.HttpClientFactory;

@Controller
public class SpringController {
	@Autowired
	HttpClient client;

	@RequestMapping("/aa")
	public void index() throws ClientProtocolException, IOException {
		String address = "http://10.4.120.77:8081/brave-hc/";
		HttpPost httppost = new HttpPost(address);

	    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
	    HttpTracing clientTemp = (HttpTracing) wac.getBean("httpTracing");
		httppost.setHeader("Content-Type", "application/json; charset=utf-8");

		// 生成 HTTP POST 实体
		StringEntity stringEntity = new StringEntity("", ContentType.TEXT_PLAIN);
		stringEntity.setContentEncoding("UTF-8");
		stringEntity.setContentType("application/json");// 发送json数据需要设置contentType
		httppost.setEntity(stringEntity);

		// 发送Post,并返回一个HttpResponse对象
		HttpResponse httpResponse = client.execute(httppost);
		HttpEntity httpEntity2 = httpResponse.getEntity();

		String outJson = null;
	}
}
