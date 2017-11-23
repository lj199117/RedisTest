/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redis.util;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 与系统有关，与环境有关的工具类
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 * @since 0.1.0
 */
public class UtilSys {
	public static Runtime runtime = Runtime.getRuntime();
	public static Map<String, Locale> locales = new HashMap<>();
	public static Map<String, TimeZone> timeZones = new ConcurrentHashMap<>(1024);
	public static Set<String> BASE_SHELL_VAR = Collections.unmodifiableSet(new HashSet<>(UtilString.split(
			"SSH_CLIENT, MAIL, NLSPATH, XDG_SESSION_ID, SSH_AGENT_PID, XFILESEARCHPATH, SSH_TTY,"
			+ "SSH_CONNECTION, XDG_RUNTIME_DIR, GLADE_PIXMAP_PATH, TERM, SHELL, XDG_MENU_PREFIX, XDG_SESSION_COOKIE,"
			+ "WINDOWID, USER, LS_COLORS, XDG_SESSION_PATH, GLADE_MODULE_PATH, XDG_SEAT_PATH,"
			+ "SSH_AUTH_SOCK, SESSION_MANAGER, DEFAULTS_PATH, XDG_CONFIG_DIRS, PATH, DESKTOP_SESSION,"
			+ "QT_IM_MODULE, PWD, XMODIFIERS, LANG, MANDATORY_PATH, GDMSESSION, SHLVL, HOME, LANGUAGE,"
			+ "LOGNAME, XDG_DATA_DIRS, DBUS_SESSION_BUS_ADDRESS, LESSOPEN, DISPLAY, GLADE_CATALOG_PATH,"
			+ "LIBGLADE_MODULE_PATH, XDG_CURRENT_DESKTOP, GTK_IM_MODULE, LESSCLOSE, COLORTERM, XAUTHORITY,"
			+ "OLDPWD, _", ",")));
	
	
	static {
		for (Locale locale : Locale.getAvailableLocales()) {
			locales.put(locale.toString(), locale);
		}
		for (String timeZoneId : TimeZone.getAvailableIDs()) {
			timeZones.put(timeZoneId, TimeZone.getTimeZone(timeZoneId));
		}
	}
	/** 获取配置器，优先顺序：env &gt; -D &gt; appProperties */
	public static Properties getSysProp(String appProperties) {
		Properties appProps = UtilIo.loadProperties(appProperties);
		
		Properties jvmProps = new Properties(appProps);
		jvmProps.putAll(System.getProperties());
		
		Properties envProps = new Properties(jvmProps);
		envProps.putAll(System.getenv());
		return envProps;
	}
	
	public static void setPropIfEmpty(String name, String value) {
		String old = System.getProperty(name);
		if (UtilObj.isEmpty(old) && !Objects.equals(old, value)) System.setProperty(name, value);
	}

	public static Map<String, Object> getJvmInfo(boolean includeStack) {
		Map<String, Object> info = new LinkedHashMap<>();
		Runtime runtime = Runtime.getRuntime();
		info.put("processors", runtime.availableProcessors());
		

		long total = runtime.totalMemory(), free = runtime.freeMemory(), used = total - free, max = runtime.maxMemory();
		Map<String, Object> mem = new LinkedHashMap<>();
		mem.put("max", max);
		mem.put("total", total);
		mem.put("free", free);
		mem.put("used", used);
		info.put("mem", mem);
		
		Map<String, Object> memHuman = new LinkedHashMap<>();
		memHuman.put("max", UtilIo.prettyByte(max));
		memHuman.put("total", UtilIo.prettyByte(total));
		memHuman.put("free", UtilIo.prettyByte(free));
		memHuman.put("used", UtilIo.prettyByte(used));
		info.put("memHuman", memHuman);
		
		
		if (includeStack) {
			Map<String, List<String>> allStackTraces = new HashMap<>();
			for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
				List<String> traces = new ArrayList<>(entry.getValue().length);
				for (StackTraceElement el : entry.getValue()) {
					traces.add(el.getClassName() + "." + el.getMethodName() + "(" + el.getFileName() + ":" + el.getLineNumber() + ")");
				}
				allStackTraces.put(entry.getKey().toString(), traces);
			}
			info.put("allStackTraces", allStackTraces);
		}
		return info;
	}
	
	private static Integer jvmPid;
	public static Integer getJvmPid() {
		if (jvmPid == null) jvmPid = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@", 2)[0]);
		return jvmPid;
	}
	public static long getUseMem() {
		return runtime.totalMemory() - runtime.freeMemory();
	}
	public static String getUseMemHuman() {
		return UtilIo.prettyByte(runtime.totalMemory() - runtime.freeMemory());
	}
    /**
     * @return <pre>{
     *  host: 'hostname',
     *  ip: 'hostname's ip'
     * }</pre>
     */
    public static Map<String, String> getHostIp() {
        Map<String, String> result = getNets().get(0);
        result.put("host", result.remove("hostName"));
        return result;
    }
    
    public static Set<String> getHardwareAddresses() {
    	Set<String> macs = new HashSet<>();
    	try {
	    	for (Enumeration<NetworkInterface> iter = NetworkInterface.getNetworkInterfaces(); iter.hasMoreElements();) {
	            NetworkInterface net = iter.nextElement();
	            byte[] mac = net.getHardwareAddress();
	            if (mac != null) macs.add(UtilString.toHexString(mac));
	    	}
    	} catch (SocketException e) {
    		throw new IllegalArgumentException(e);
    	}
    	return macs;
    }

    /**
     * 返回所有网卡信息<br>
     * 主机ip应该是有意义的，且是当前网卡列表中的ip<br>
     * 如果是127，那么取第一个物理网卡的地址
     * 
     * @return <pre>
     * {
     * 	host: 'hostname',
     * 	ip: 'hostname's ip',
     *  nets: [{
     *      name: ''
     *  }]
     * }
     * </pre>
     */
    public static List<Map<String, String>> getNets() {
        List<Map<String, String>> result = new ArrayList<>();
        final Map<String, String> local;

        try {
            InetAddress localNet = InetAddress.getLocalHost();
            String hostName = localNet.getHostName();
            local = new HashMap<>();
            local.put("name", "lo");
            local.put("hostName", hostName);
            local.put("ip", localNet.getHostAddress());

            for (Enumeration<NetworkInterface> iter = NetworkInterface.getNetworkInterfaces(); iter.hasMoreElements();) {
                NetworkInterface net = iter.nextElement();
                if (!net.isUp() || net.isLoopback() || net.isPointToPoint() || net.isVirtual()) {
                    continue;
                }
                
                String mac = UtilString.toHexString(net.getHardwareAddress());
                for (Enumeration<InetAddress> iter2 = net.getInetAddresses(); iter2.hasMoreElements();) {
                    InetAddress addr = iter2.nextElement();
                    if (addr instanceof Inet4Address) {
                        Map<String, String> one = new LinkedHashMap<>();
                        one.put("name", net.getName());
                        one.put("hostName", addr.getHostName().equals(addr.getHostAddress()) ? hostName : addr.getHostAddress());
                        one.put("ip", addr.getHostAddress());
                        one.put("mac", mac);
                        result.add(one);
                    }
                }
            }
        } catch (UnknownHostException | SocketException e) {
			throw new IllegalArgumentException(e);
		}

        if (result.isEmpty()) {
            result.add(local);
            return result;
        }

        // 先匹配(local.hostName + local.ip); 匹配(local.hostName)[name, ip]; 最后(name, ip)
        Collections.sort(result, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                if (UtilObj.compare(o1.get("hostName"), local.get("hostName")) == 0) { // o1匹配local
                    if (UtilObj.compare(o1.get("ip"), local.get("ip")) == 0) { // o1完整匹配local
                        return -1;
                    }
                    
                    if (UtilObj.compare(o2.get("hostName"), local.get("hostName")) != 0) { // o2不匹配local
                        return -1;
                    }
                    
                    if (UtilObj.compare(o2.get("ip"), local.get("ip")) == 0) {// o2完整匹配local
                        return 1;
                    }
                } else if (UtilObj.compare(o2.get("hostName"), local.get("hostName")) == 0) { // o1不匹配，o2匹配local
                    return 1;
                }
                
                return (o1.get("name") + "=" + o1.get("ip")).compareTo(o2.get("name") + "=" + o2.get("ip"));
            }
        });
        return result;
	}
    
    public static String getLocalNetInfo() {
    	try {
    		InetAddress address = InetAddress.getLocalHost();
			return address.getHostAddress() + "/" + address.getHostName();
		} catch (UnknownHostException e) {
			throw new IllegalStateException(e);
		}
    }
    
    public static Locale getLocale(String locale) {
    	return locales.get(locale);
    }
    /** 参考locale生成新的language的locale */
    public static Locale getLocale(Locale locale, String language) {
    	if (locale == null && language == null) return null;
    	if (language == null || language.isEmpty()) return locale;
    	
    	if (locale == null) locale = Locale.getDefault();
    	return new Locale(language.toLowerCase(), locale.getCountry(), locale.getVariant());
    }
    
    public static TimeZone getTimeZone(String timeZoneId) {
    	return timeZones.get(timeZoneId);
    }
    
    
    public static String getLanguage(String language) {
    	if (language == null) return "en";
    	
    	language = language.trim().toLowerCase();
    	if (language.isEmpty()) return "en";
    	
		if (language.startsWith("zh-")) return language.equals("zh-hant") ? language : "zh";
		
		return UtilString.split(language, '-').get(0);
    }
    public static void sleep(long millis) {
    	try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			interrupt();
		}
    }
    
    /** @see Thread#interrupt() */
	public static void interrupt() {
		Thread thread = Thread.currentThread();
		if (!thread.isInterrupted()) thread.interrupt();
	}
	public static void interrupt(InterruptedException e) {
		if (e != null) interrupt();
	}
	public static void interrupt(Throwable cause) {
		if (cause instanceof InterruptedException) interrupt();;
	}
}
