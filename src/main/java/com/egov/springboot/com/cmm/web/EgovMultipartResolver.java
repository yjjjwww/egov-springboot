package com.egov.springboot.com.cmm.web;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.egov.springboot.com.cmm.service.EgovProperties;
import com.egov.springboot.let.utl.fcc.service.EgovFileUploadUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 실행환경의 파일업로드 처리를 위한 기능 클래스 (StandardServletMultipartResolver 기반)
 */
public class EgovMultipartResolver extends StandardServletMultipartResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovMultipartResolver.class);

    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        MultipartHttpServletRequest multipartRequest = super.resolveMultipart(request);

        String whiteListFileUploadExtensions = EgovProperties.getProperty("Globals.fileUpload.Extensions");

        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            MultipartFile file = entry.getValue();

            String fileName = file.getOriginalFilename();
            String fileExtension = EgovFileUploadUtil.getFileExtension(fileName);
            LOGGER.debug("Found File Extension = " + fileExtension);

            if (whiteListFileUploadExtensions == null || "".equals(whiteListFileUploadExtensions)) {
                LOGGER.debug("The file extension whitelist has not been set.");
            } else {
                if (fileName == null || "".equals(fileName)) {
                    LOGGER.debug("No file name.");
                } else {
                    if ("".equals(fileExtension)) {
                        throw new SecurityException("[No file extension] File extension not allowed.");
                    }
                    if ((whiteListFileUploadExtensions + ".").contains("." + fileExtension.toLowerCase() + ".")) {
                        LOGGER.debug("File extension allowed.");
                    } else {
                        throw new SecurityException("[" + fileExtension + "] File extension not allowed.");
                    }
                }
            }
        }

        return multipartRequest;
    }

}
