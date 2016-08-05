package com.paymentprocessor.demo.controller;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Controller
public class StripeOauthClientController {
	private static final Logger LOGGER = LoggerFactory.getLogger(StripeOauthClientController.class);
	
	//Stripe settings: should not be here
    public static final String STRIPE_AUTHORIZE_URI = "https://connect.stripe.com/oauth/authorize";
    public static final String STRIPE_TOKEN_URI = "https://connect.stripe.com/oauth/token";
    // Read Stripe platform's client ID and secret API key: TODO put these in settings file
    final String clientId = "YOUR-STRIPE-CLIENT-ID";
    final String apiKey = "YOUR-STRIPE-API-KEY";
	
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String connectToStripe() {
        return "connect-to-stripe-ftl";
    }
    
    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public void authorize(HttpServletResponse httpServletResponse) {
        URI uri = null;
		try {
		    uri = new URIBuilder(STRIPE_AUTHORIZE_URI)
				    .setParameter("response_type", "code")
				    .setParameter("scope", "read_write")
				    .setParameter("client_id", clientId)
				    .build();
		    //redirect to Stripe Authorization URI
			httpServletResponse.setStatus(302);  //TODO: 302 or 201 (HttpStatus.SC_CREATED)?
			httpServletResponse.setHeader("Location", uri.toString());
		} catch (Exception e) {
			LOGGER.debug(e.toString());
			httpServletResponse.setStatus(500);
        }
    }
    
    @RequestMapping(value = "/oauth/callback", method = RequestMethod.GET)
    @ResponseBody
    public String callback(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    	Map<String, Object> viewObjects = new HashMap<String, Object>();
    	
    	try {
    		String code = httpServletRequest.getParameter("code");
    		LOGGER.debug("The authorization code is {}", code);
    		URI uri = new URIBuilder(STRIPE_TOKEN_URI)
                .setParameter("client_secret", apiKey)
                .setParameter("grant_type", "authorization_code")
                .setParameter("client_id", clientId)
                .setParameter("code", code)
                .build();

    		// Make /oauth/token endpoint POST request
    		HttpPost httpPost = new HttpPost(uri);
    		CloseableHttpClient httpClient = HttpClients.createDefault();
    		CloseableHttpResponse resp = httpClient.execute(httpPost);

    		// Grab stripe_user_id (use this to authenticate as the connected account: access_token in Oauth 2)
    		String bodyAsString = EntityUtils.toString(resp.getEntity());
    		Type t = new TypeToken<Map<String, String>>() { }.getType();
    		Map<String, String> map = new GsonBuilder().create().fromJson(bodyAsString, t);
    		String accountId = map.get("stripe_user_id");
    		
    		viewObjects.put("account_id", accountId);
    		viewObjects.put("raw_body", bodyAsString);

    		return bodyAsString;
    		//return new ModelAndView("callback.ftl", viewObjects);
    		
    	} catch (Exception e) {
    		httpServletResponse.setStatus(500);
    		viewObjects.put("error", e.getMessage());
    		return e.getMessage();
    		//return new ModelAndView("error.ftl", viewObjects);
    	}
    }
    
}