/*
 * $Id: LocaleAction.java 471754 2006-11-06 14:55:09Z husted $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts.apps.mailreader.actions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import brave.http.HttpTracing;
import org2.apache.brave.HttpClientFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;



/**
 * <p>
 * Change user's Struts {@link java.util.Locale}.
 * </p>
 */
@Controller
public final class LocaleAction extends BaseAction {

    /**
     * <p>
     * Return true if parameter is null or trims to empty.
     * </p>
     *
     * @param string The string to text; may be  null
     * @return true if parameter is null or empty
     */
    private boolean isBlank(String string) {
        return ((string == null) || (string.trim().length() == 0));
    }

    /**
     * <p>
     * Parameter for {@link java.util.Locale} language property. ["language"]
     * </p>
     */
    private static final String LANGUAGE = "language";

    /**
     * <p>
     * Parameter for {@link java.util.Locale} country property. ["country"]
     * </p>
     */
    private static final String COUNTRY = "country";

    /**
     * <p>
     * Parameter for response page URI. ["page"]
     * </p>
     */
    private static final String PAGE = "page";

    /**
     * <p>
     * Parameter for response forward name. ["forward"]
     * </p>
     */
    private static final String FORWARD = "forward";

    /**
     * <p>
     * Logging message if LocaleAction is missing a target parameter.
     * </p>
     */
    private static final String LOCALE_LOG =
            "LocaleAction: Missing page or forward parameter";

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {

    	//String address = "http://10.4.120.77:8081/brave-hc/";
    	String address = "http://localhost:9080/dubbo-consumer-web/dubbo/Emily";
		HttpPost httppost = new HttpPost(address);

	    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
	    HttpClient clientTemp1 = (HttpClient)wac.getBean("httpClient");
	    HttpTracing clientTemp = (HttpTracing) wac.getBean("httpTracing");
		HttpClient httpClient =HttpClientFactory.getHttpClient();// (HttpClient)wac.getBean("httpClient");
		httppost.setHeader("Content-Type", "application/json; charset=utf-8");

		// 生成 HTTP POST 实体
		StringEntity stringEntity = new StringEntity("", ContentType.TEXT_PLAIN);
		stringEntity.setContentEncoding("UTF-8");
		stringEntity.setContentType("application/json");// 发送json数据需要设置contentType
		httppost.setEntity(stringEntity);

		// 发送Post,并返回一个HttpResponse对象
		HttpResponse httpResponse = httpClient.execute(httppost);
		HttpEntity httpEntity2 = httpResponse.getEntity();

		String outJson = null;
		outJson = EntityUtils.toString(httpEntity2);

		System.err.println(outJson);
		
        String language = request.getParameter(LANGUAGE);
        String country = request.getParameter(COUNTRY);

        Locale locale = getLocale(request);

        if ((!isBlank(language)) && (!isBlank(country))) {
            locale = new Locale(language, country);
        } else if (!isBlank(language)) {
            locale = new Locale(language, "");
        }

        HttpSession session = request.getSession();
        session.setAttribute(Globals.LOCALE_KEY, locale);

        String target = request.getParameter(PAGE);
        if (!isBlank(target)) {
            return new ActionForward(target);
        }

        target = request.getParameter(FORWARD);
        if (isBlank(target)) {
            target = mapping.getParameter();
        }
        if (isBlank(target)) {
            log.warn(LOCALE_LOG);
            return null;
        }
        return mapping.findForward(target);
    }
}
