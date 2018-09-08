package com.cn.xmf.job.admin.controller.interceptor;

import com.cn.xmf.job.admin.controller.annotation.PermessionLimit;
import com.cn.xmf.job.admin.config.XxlJobAdminConfig;
import com.cn.xmf.job.admin.controller.annotation.PermessionLimit;
import com.cn.xmf.job.admin.core.util.CookieUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

/**
 * 权限拦截, 简易版
 *
 * @author xuxueli 2015-12-12 18:09:04
 */
public class PermissionInterceptor extends HandlerInterceptorAdapter {


	private static final String LOGIN_IDENTITY_KEY = "XXL_JOB_LOGIN_IDENTITY";
	private static final String LOGIN_IDENTITY_TOKEN;
    static {
		XxlJobAdminConfig adminConfig = XxlJobAdminConfig.getAdminConfig();
		String username = adminConfig.getLoginUsername();
		String password = adminConfig.getLoginPassword();

        // login token
        String tokenTmp = DigestUtils.md5Hex(username + "_" + password);
		tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

		LOGIN_IDENTITY_TOKEN = tokenTmp;
    }



	public static boolean login(HttpServletResponse response, String username, String password, boolean ifRemember){

    	// login token
		String tokenTmp = DigestUtils.md5Hex(username + "_" + password);
		tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

		if (!LOGIN_IDENTITY_TOKEN.equals(tokenTmp)){
			return false;
		}

		// do login
		CookieUtil.set(response, LOGIN_IDENTITY_KEY, LOGIN_IDENTITY_TOKEN, ifRemember);
		return true;
	}
	public static void logout(HttpServletRequest request, HttpServletResponse response){
		CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
	}
	public static boolean ifLogin(HttpServletRequest request){
		String indentityInfo = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
		return indentityInfo != null && LOGIN_IDENTITY_TOKEN.equals(indentityInfo.trim());
	}



	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		if (!(handler instanceof HandlerMethod)) {
			return super.preHandle(request, response, handler);
		}
		
		if (!ifLogin(request)) {
			HandlerMethod method = (HandlerMethod)handler;
			PermessionLimit permission = method.getMethodAnnotation(PermessionLimit.class);
			if (permission == null || permission.limit()) {
				response.sendRedirect(request.getContextPath() + "/jobadmin/toLogin");
				//request.getRequestDispatcher("/toLogin").forward(request, response);
				return false;
			}
		}
		
		return super.preHandle(request, response, handler);
	}
	
}