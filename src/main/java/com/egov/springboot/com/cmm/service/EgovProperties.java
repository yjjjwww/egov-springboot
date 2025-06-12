package com.egov.springboot.com.cmm.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.Properties;
/**
 *  Class Name : EgovProperties.java
 *  Description : properties값들을 파일로부터 읽어와   Globals클래스의 정적변수로 로드시켜주는 클래스로
 *   문자열 정보 기준으로 사용할 전역변수를 시스템 재시작으로 반영할 수 있도록 한다.
 *  Modification Information
 *
 *     수정일         수정자                   수정내용
 *   -------    --------    ---------------------------
 *   2009.01.19    박지욱          최초 생성
 *	 2011.07.20    서준식 	      Globals파일의 상대경로를 읽은 메서드 추가
 *   2011.08.31  JJY            경량환경 템플릿 커스터마이징버전 생성
 *
 *  @author 공통 서비스 개발팀 박지욱
 *  @since 2009. 01. 19
 *  @version 1.0
 *  @see
 *
 */

public class EgovProperties{

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovProperties.class);

    // 프로퍼티값 로드시 에러발생하면 반환되는 에러문자열
    public static final String ERR_CODE = " EXCEPTION OCCURRED";
    public static final String ERR_CODE_FNFE = " EXCEPTION(FNFE) OCCURRED";
    public static final String ERR_CODE_IOE = " EXCEPTION(IOE) OCCURRED";

    // 파일구분자
    static final char FILE_SEPARATOR = File.separatorChar;

    // Spring Environment 객체
    private static Environment environment;
    
    // ApplicationContext 객체
    private static ApplicationContext context;

    @Autowired
    public EgovProperties(Environment environment, ApplicationContext context) {
        EgovProperties.environment = environment;
        EgovProperties.context = context;
    }

    /**
     * 인자로 주어진 문자열을 Key값으로 하는 프로퍼티 값을 반환한다(application.properties 사용)
     * @param keyName String
     * @return String
     */
    public static String getProperty(String keyName) {
        if (environment == null) {
            // ApplicationContext에서 Environment 가져오기 시도
            try {
                if (context != null) {
                    environment = context.getEnvironment();
                } else {
                    LOGGER.error("ApplicationContext is not initialized.");
                    return ERR_CODE;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to get Environment: {}", e.getMessage());
                return ERR_CODE;
            }
        }
        
        String value = environment.getProperty(keyName);
        
        if (value == null) {
            LOGGER.debug("Property '{}' not found in application.properties", keyName);
            return ERR_CODE;
        }
        
        return value.trim();
    }
	
	/**
	 * 주어진 파일에서 인자로 주어진 문자열을 Key값으로 하는 프로퍼티 상대 경로값을 절대 경로값으로 반환한다
	 * @param fileName String
	 * @param key String
	 * @return String

	public static String getPathProperty(String fileName, String key){
		FileInputStream fis = null;
		try{
			java.util.Properties props = new java.util.Properties();
			fis  = new FileInputStream(fileName);
			props.load(new java.io.BufferedInputStream(fis));
			fis.close();

			String value = props.getProperty(key);
			value = RELATIVE_PATH_PREFIX + "egovProps" + System.getProperty("file.separator") + value;
			return value;
		}catch(java.io.FileNotFoundException fne){
			return ERR_CODE_FNFE;
		}catch(java.io.IOException ioe){
			return ERR_CODE_IOE;
		}finally{
			try {
				if (fis != null) fis.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	*/

	/**
	 * 주어진 파일에서 인자로 주어진 문자열을 Key값으로 하는 프로퍼티 값을 반환한다
	 * @param fileName String
	 * @param key String
	 * @return String

	public static String getProperty(String fileName, String key){
		FileInputStream fis = null;
		try{
			java.util.Properties props = new java.util.Properties();
			fis  = new FileInputStream(fileName);
			props.load(new java.io.BufferedInputStream(fis));
			fis.close();

			String value = props.getProperty(key);
			return value;
		}catch(java.io.FileNotFoundException fne){
			return ERR_CODE_FNFE;
		}catch(java.io.IOException ioe){
			return ERR_CODE_IOE;
		}finally{
			try {
				if (fis != null) fis.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	*/
	/**
	 * 주어진 프로파일의 내용을 파싱하여 (key-value) 형태의 구조체 배열을 반환한다.
	 * @param property String
	 * @return ArrayList
	 */
	@SuppressWarnings("unused")
	public static ArrayList<Map<String, String>> loadPropertyFile(String property){

		// key - value 형태로 된 배열 결과
		ArrayList<Map<String, String>> keyList = new ArrayList<Map<String, String>>();

		String src = property.replace('\\', FILE_SEPARATOR).replace('/', FILE_SEPARATOR);
		FileInputStream fis = null;
		try
		{

			File srcFile = new File(src);
			if (srcFile.exists()) {

				java.util.Properties props = new java.util.Properties();
				fis  = new FileInputStream(src);
				props.load(new java.io.BufferedInputStream(fis));
				fis.close();

				int i = 0;
				Enumeration<?> plist = props.propertyNames();
				if (plist != null) {
					while (plist.hasMoreElements()) {
						Map<String, String> map = new HashMap<String, String>();
						String key = (String)plist.nextElement();
						map.put(key, props.getProperty(key));
						keyList.add(map);
					}
				}
			}
		} catch (Exception ex){
			debug("EX:"+ex);
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (Exception ex) {
				debug("EX:"+ex);
			}
		}

		return keyList;
	}

	/**
	 * 시스템 로그를 출력한다.
	 * @param obj Object
	 */
	private static void debug(Object obj) {
		if (obj instanceof java.lang.Exception) {
			LOGGER.debug("IGNORED: {}", ((Exception)obj).getMessage());
		}
	}
}

