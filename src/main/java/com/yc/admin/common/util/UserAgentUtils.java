package com.yc.admin.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户代理工具类
 * 用于解析浏览器和操作系统信息
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
public class UserAgentUtils {

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = request.getRemoteAddr();
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            // 根据网卡取本机配置的IP
            try {
                java.net.InetAddress inet = java.net.InetAddress.getLocalHost();
                ip = inet.getHostAddress();
            } catch (Exception e) {
                log.warn("获取本机IP失败: {}", e.getMessage());
            }
        }
        
        return ip;
    }

    /**
     * 获取用户代理字符串
     *
     * @param request HTTP请求
     * @return 用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "unknown";
    }

    /**
     * 解析浏览器信息
     *
     * @param userAgent 用户代理字符串
     * @return 浏览器名称
     */
    public static String getBrowser(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "unknown";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        } else if (userAgent.contains("postman")) {
            return "Postman";
        } else if (userAgent.contains("curl")) {
            return "cURL";
        } else if (userAgent.contains("wget")) {
            return "Wget";
        } else {
            return "unknown";
        }
    }

    /**
     * 解析操作系统信息
     *
     * @param userAgent 用户代理字符串
     * @return 操作系统名称
     */
    public static String getOperatingSystem(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "unknown";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("windows nt 10")) {
            return "Windows 10";
        } else if (userAgent.contains("windows nt 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("windows nt 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("windows nt 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("windows nt 6.0")) {
            return "Windows Vista";
        } else if (userAgent.contains("windows nt 5.1")) {
            return "Windows XP";
        } else if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac os x")) {
            Pattern pattern = Pattern.compile("mac os x ([\\d_]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                String version = matcher.group(1).replace("_", ".");
                return "macOS " + version;
            }
            return "macOS";
        } else if (userAgent.contains("linux")) {
            if (userAgent.contains("android")) {
                Pattern pattern = Pattern.compile("android ([\\d.]+)");
                Matcher matcher = pattern.matcher(userAgent);
                if (matcher.find()) {
                    return "Android " + matcher.group(1);
                }
                return "Android";
            } else if (userAgent.contains("ubuntu")) {
                return "Ubuntu";
            } else if (userAgent.contains("centos")) {
                return "CentOS";
            } else if (userAgent.contains("fedora")) {
                return "Fedora";
            } else {
                return "Linux";
            }
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            Pattern pattern = Pattern.compile("os ([\\d_]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                String version = matcher.group(1).replace("_", ".");
                return "iOS " + version;
            }
            return "iOS";
        } else {
            return "unknown";
        }
    }

    /**
     * 获取登录地点（简单实现，返回IP地址）
     * 实际项目中可以集成第三方IP地址库进行地理位置解析
     *
     * @param ipAddress IP地址
     * @return 登录地点
     */
    public static String getLoginLocation(String ipAddress) {
        if (!StringUtils.hasText(ipAddress) || "unknown".equals(ipAddress)) {
            return "unknown";
        }
        
        // 本地IP地址
        if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress) || 
            ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || 
            ipAddress.startsWith("172.")) {
            return "内网IP";
        }
        
        // TODO: 集成第三方IP地址库（如：ip2region、GeoLite2等）进行地理位置解析
        // 这里简单返回IP地址
        return ipAddress;
    }
}