package com.km.sglototo.component;


import com.km.sglototo.sys.entity.SysAdminLog;
import com.km.sglototo.sys.service.ISysAdminLogService;
import com.km.sglototo.util.IpAddressUtil;
import com.km.sglototo.util.JwtTokenUtil;
import com.km.sglototo.utils.ValidatorUtils;
import com.km.sglototo.ApiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);
    @Resource
    public ISysAdminLogService fopSystemOperationLogService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private ApiContext apiContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long startTime, endTime;
        String requestType = ((HttpServletRequest) request).getMethod();
        SysAdminLog sysLog = new SysAdminLog();
        StringBuffer sbParams = new StringBuffer();
        if (!"get".equals(requestType.toLowerCase())) {
            Map<String, String[]> params = new HashMap<String, String[]>(request.getParameterMap());
            sbParams.append("?");
            for (String key : params.keySet()) {
                if (null == key || null == params.get(key) || null == params.get(key)[0]) {
                    continue;
                }
                sbParams.append(key).append("=").append(params.get(key)[0]).append("&");
            }

            if (sbParams.length() > 1) {
                sbParams = sbParams.delete(sbParams.length() - 1, sbParams.length());
            }
            sysLog.setParams(sbParams.toString());
        } else {
            sysLog.setParams(getBodyString(request));
        }


        String fullUrl = ((HttpServletRequest) request).getRequestURL().toString();
        String storeId = request.getParameter("storeid");
        if (ValidatorUtils.notEmpty(storeId)) {
            apiContext.setCurrentProviderId(Long.valueOf(storeId));
        } else {
            storeId = request.getHeader("storeid");
            if (ValidatorUtils.notEmpty(storeId)) {
                apiContext.setCurrentProviderId(Long.valueOf(storeId));
            }
        }
        String username = null;
        String authHeader = request.getHeader(this.tokenHeader);
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            String authToken = authHeader.substring(this.tokenHead.length());
            username = jwtTokenUtil.getUserNameFromToken(authToken);
            LOGGER.info("checking username:{}", username);
            if (fullUrl.contains("logout") || fullUrl.contains("login")) {

            } else {
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        LOGGER.info("checking username:{}", username);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }

        }
        startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        endTime = System.currentTimeMillis();


        logger.info(formMapKey(11, fullUrl, requestType,
                IpAddressUtil.getIpAddr((HttpServletRequest) request), sbParams.toString(), authHeader)
                + ",\"cost\":\"" + (endTime - startTime) + "ms\"");
        int startIntercept = fullUrl.replace("//", "a").indexOf("/") + 1;
        String interfaceName = fullUrl.substring(startIntercept, fullUrl.length());
        sysLog.setCreateTime(new Date());
        sysLog.setIp(IpAddressUtil.getIpAddr(request));
        sysLog.setMethod(interfaceName);
        sysLog.setServiceName(requestType);

        sysLog.setOperationDesc(authHeader);
        sysLog.setUserName(username);
        sysLog.setTimeMin((endTime - startTime));
        if (!"OPTIONS".equals(requestType) && !interfaceName.contains("webjars")
                && !interfaceName.contains("api-docs")) {
       //     fopSystemOperationLogService.save(sysLog);
        }
    }

    private String formMapKey(Object userId, String mothedName, String requestType,
                              String ip, String params, String token) {
        return "\"time\"" + ":\"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date())
                + "\",\"name\"" + ":\"" + mothedName + "\",\"uid\":\"" + userId
                + "\",\"type\":\"" + requestType + "\",\"ip\":\"" + ip
                + "\",\"token\":\"" + token + "\",\"params\":\"" + params + "\"";
    }

    /**
     * 获取请求Body
     *
     * @param request
     * @return
     */
    public String getBodyString(final ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = cloneInputStream(request.getInputStream());
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    /**
     * Description: 复制输入流</br>
     *
     * @param inputStream
     * @return</br>
     */
    public InputStream cloneInputStream(ServletInputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return byteArrayInputStream;
    }
}
