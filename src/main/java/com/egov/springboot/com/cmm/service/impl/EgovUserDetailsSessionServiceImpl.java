package com.egov.springboot.com.cmm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.egov.springboot.com.cmm.service.EgovUserDetailsService;


/**
 * 
 * @author 공통서비스 개발팀 서준식
 * @since 2011. 6. 25.
 * @version 1.0
 * @see
 *
 *      <pre>
 * 개정이력(Modification Information) 
 * 
 *   수정일      수정자          수정내용
 *  -------    --------    ---------------------------
 *   2011.08.12  서준식          최초생성
 * 
 *      </pre>
 */

public class EgovUserDetailsSessionServiceImpl extends EgovAbstractServiceImpl implements EgovUserDetailsService {

	@Override
	public Object getAuthenticatedUser() {

		return RequestContextHolder.getRequestAttributes().getAttribute("LoginVO", RequestAttributes.SCOPE_SESSION);

	}

	@Override
	public List<String> getAuthorities() {

		// 권한 설정을 리턴한다.
		List<String> listAuth = new ArrayList<String>();

		return listAuth;
	}

	@Override
	public Boolean isAuthenticated() {
		// 인증된 유저인지 확인한다.

		if (RequestContextHolder.getRequestAttributes() == null) {
			return false;
		} else {

			if (RequestContextHolder.getRequestAttributes().getAttribute("LoginVO",
					RequestAttributes.SCOPE_SESSION) == null) {
				return false;
			} else {
				return true;
			}
		}

	}

}
