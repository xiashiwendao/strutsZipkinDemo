package org2.apache.brave;

import brave.http.HttpTracing;
import brave.httpclient.TracingHttpClientBuilder;
import brave.spring.webmvc.DelegatingTracingFilter;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This adds tracing configuration to any web mvc controllers or rest template
 * clients.
 *
 * <p>
 * This is a {@link Initializer#getRootConfigClasses() root config class}, so
 * the {@linkplain DelegatingTracingFilter} added in
 * {@link Initializer#getServletFilters()} can wire up properly.
 */
public class HttpClientFactory {
	private static HttpClientFactory instance;
	private static HttpClient httpClient;
	private static Object lock = new Object();

	public static HttpClient getHttpClient() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new HttpClientFactory();
					httpClient = instance.httClient();
				}
			}
		}

		return httpClient;
	}

	public HttpClient httClient() {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "applicationContext.xml","spring-webmvc-servlet.xml" });
		BeanFactory factory = (BeanFactory) appContext;
		HttpClient httpClient = (HttpClient)factory.getBean("httpClient");

		return httpClient;
	}
}
