package com.egov.springboot.let.uat.uia.web;

import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.property.EgovPropertyService;

import javax.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.egov.springboot.com.cmm.EgovMessageSource;
import com.egov.springboot.com.cmm.LoginVO;
import com.egov.springboot.com.cmm.util.EgovUserDetailsHelper;
import com.egov.springboot.let.uat.uia.service.EgovLoginService;

import java.util.HashMap;
import java.util.Map;

/**
 * 일반 로그인을 처리하는 REST 컨트롤러 클래스
 * @author 공통서비스 개발팀 박지욱
 * @since 2009.03.06
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *  수정일      수정자      수정내용
 *  -------            --------        ---------------------------
 *  2009.03.06  박지욱     최초 생성
 *  2011.08.31  JJY            경량환경 템플릿 커스터마이징버전 생성
 *
 *  </pre>
 */
@RestController
public class EgovLoginController {

    /** EgovLoginService */
    @Resource(name = "loginService")
    private EgovLoginService loginService;

    /** EgovMessageSource */
    @Resource(name = "egovMessageSource")
    EgovMessageSource egovMessageSource;

    /** EgovPropertyService */
    @Resource(name = "propertiesService")
    protected EgovPropertyService propertiesService;

    /** TRACE */
    @Resource(name = "leaveaTrace")
    LeaveaTrace leaveaTrace;

    /**
     * 로그인 화면 정보를 반환한다
     * @return 로그인 페이지 정보
     * @exception Exception
     */
    @GetMapping("/uat/uia/egovLoginUsr.do")
    public Map<String, Object> loginUsrView() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", true);
        resultMap.put("message", "로그인 페이지 정보입니다.");
        return resultMap;
    }

    /**
     * 일반 로그인을 처리한다
     * @param loginVO - 아이디, 비밀번호가 담긴 LoginVO
     * @return ResponseEntity - 로그인 결과 및 사용자 정보
     * @exception Exception
     */
    @PostMapping("/uat/uia/actionLogin.do")
    public ResponseEntity<Map<String, Object>> actionLogin(@RequestBody LoginVO loginVO, HttpServletRequest request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        try {
            // 1. 일반 로그인 처리
            LoginVO resultVO = loginService.actionLogin(loginVO);
            
            boolean loginPolicyYn = true;
            
            if (resultVO != null && resultVO.getId() != null && !resultVO.getId().equals("") && loginPolicyYn) {
                // 세션에 사용자 정보 저장
                HttpSession session = request.getSession();
                session.setAttribute("LoginVO", resultVO);
                
                // 응답 데이터 구성
                resultMap.put("success", true);
                resultMap.put("message", "로그인에 성공하였습니다.");
                resultMap.put("user", resultVO);
                
                return ResponseEntity.ok(resultMap);
            } else {
                resultMap.put("success", false);
                resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
                return ResponseEntity.ok(resultMap);
            }
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "로그인 처리 중 오류가 발생했습니다.");
            resultMap.put("error", e.getMessage());
            return ResponseEntity.ok(resultMap);
        }
    }

    /**
     * 로그인 상태 확인
     * @return 로그인 상태 및 사용자 정보
     */
    @GetMapping("/uat/uia/checkLoginStatus.do")
    public Map<String, Object> checkLoginStatus() {
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자 인증 여부 확인
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        
        if (isAuthenticated) {
            LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
            
            resultMap.put("success", true);
            resultMap.put("message", "로그인 상태입니다.");
            resultMap.put("user", loginVO);
        } else {
            resultMap.put("success", false);
            resultMap.put("message", "로그인 상태가 아닙니다.");
        }
        
        return resultMap;
    }

    /**
     * 로그인 후 메인화면 정보를 반환한다
     * @return 메인 페이지 정보
     * @exception Exception
     */
    @GetMapping("/uat/uia/actionMain.do")
    public Map<String, Object> actionMain() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        // 1. 사용자 인증 처리
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        if (!isAuthenticated) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return resultMap;
        }

        // 2. 메인 페이지 정보 반환
        resultMap.put("success", true);
        resultMap.put("message", "메인 페이지 정보입니다.");
        return resultMap;
    }

    /**
     * 로그아웃 처리
     * @return 로그아웃 결과
     * @exception Exception
     */
    @PostMapping("/uat/uia/actionLogout.do")
    public Map<String, Object> actionLogout(HttpServletRequest request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        try {
            // 세션에서 로그인 정보 제거
            RequestContextHolder.getRequestAttributes().removeAttribute("LoginVO", RequestAttributes.SCOPE_SESSION);
            
            resultMap.put("success", true);
            resultMap.put("message", "로그아웃 되었습니다.");
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "로그아웃 처리 중 오류가 발생했습니다.");
            resultMap.put("error", e.getMessage());
        }
        
        return resultMap;
    }
}