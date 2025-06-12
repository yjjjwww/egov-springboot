package com.egov.springboot.let.cop.bbs.web;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springmodules.validation.commons.DefaultBeanValidator;

import com.egov.springboot.com.cmm.EgovMessageSource;
import com.egov.springboot.com.cmm.LoginVO;
import com.egov.springboot.com.cmm.service.EgovFileMngService;
import com.egov.springboot.com.cmm.service.EgovFileMngUtil;
import com.egov.springboot.com.cmm.service.FileVO;
import com.egov.springboot.com.cmm.util.EgovUserDetailsHelper;
import com.egov.springboot.let.cop.bbs.service.Board;
import com.egov.springboot.let.cop.bbs.service.BoardMaster;
import com.egov.springboot.let.cop.bbs.service.BoardMasterVO;
import com.egov.springboot.let.cop.bbs.service.BoardVO;
import com.egov.springboot.let.cop.bbs.service.EgovBBSAttributeManageService;
import com.egov.springboot.let.cop.bbs.service.EgovBBSManageService;

/**
 * 게시물 관리를 위한 REST 컨트롤러 클래스
 * @author 공통 서비스 개발팀 이삼섭
 * @since 2009.03.19
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일      수정자          수정내용
 *  -------    --------    ---------------------------
 *  2009.03.19  이삼섭          최초 생성
 *  2009.06.29  한성곤         2단계 기능 추가 (댓글관리, 만족도조사)
 *  2011.08.31  JJY            경량환경 템플릿 커스터마이징버전 생성
 *
 *  </pre>
 */
@RestController
public class EgovBBSManageController {

    @Resource(name = "EgovBBSManageService")
    private EgovBBSManageService bbsMngService;

    @Resource(name = "EgovBBSAttributeManageService")
    private EgovBBSAttributeManageService bbsAttrbService;

    @Resource(name = "EgovFileMngService")
    private EgovFileMngService fileMngService;

    @Resource(name = "EgovFileMngUtil")
    private EgovFileMngUtil fileUtil;

    @Resource(name = "propertiesService")
    protected EgovPropertyService propertyService;

    @Resource(name="egovMessageSource")
    EgovMessageSource egovMessageSource;

    @Autowired
    private DefaultBeanValidator beanValidator;

    /**
     * XSS 방지 처리.
     *
     * @param data
     * @return
     */
    protected String unscript(String data) {
        if (data == null || data.trim().equals("")) {
            return "";
        }

        String ret = data;

        ret = ret.replaceAll("<(S|s)(C|c)(R|r)(I|i)(P|p)(T|t)", "&lt;script");
        ret = ret.replaceAll("</(S|s)(C|c)(R|r)(I|i)(P|p)(T|t)", "&lt;/script");

        ret = ret.replaceAll("<(O|o)(B|b)(J|j)(E|e)(C|c)(T|t)", "&lt;object");
        ret = ret.replaceAll("</(O|o)(B|b)(J|j)(E|e)(C|c)(T|t)", "&lt;/object");

        ret = ret.replaceAll("<(A|a)(P|p)(P|p)(L|l)(E|e)(T|t)", "&lt;applet");
        ret = ret.replaceAll("</(A|a)(P|p)(P|p)(L|l)(E|e)(T|t)", "&lt;/applet");

        ret = ret.replaceAll("<(E|e)(M|m)(B|b)(E|e)(D|d)", "&lt;embed");
        ret = ret.replaceAll("</(E|e)(M|m)(B|b)(E|e)(D|d)", "&lt;embed");

        ret = ret.replaceAll("<(F|f)(O|o)(R|r)(M|m)", "&lt;form");
        ret = ret.replaceAll("</(F|f)(O|o)(R|r)(M|m)", "&lt;form");

        return ret;
    }

    /**
     * 게시물에 대한 목록을 조회한다.
     *
     * @param boardVO
     * @param session
     * @return ResponseEntity - 게시물 목록 정보
     * @throws Exception
     */
    @GetMapping("/cop/bbs/selectBoardList.do")
    public ResponseEntity<Map<String, Object>> selectBoardArticles(HttpSession session, 
            @RequestParam(value="menuNo", required=false) String menuNo,
            @ModelAttribute("searchVO") BoardVO boardVO) throws Exception {
    
        Map<String, Object> resultMap = new HashMap<>();
        
        // 선택된 메뉴정보를 세션으로 등록한다.
        if (menuNo!=null && !menuNo.equals("")){
            session.setAttribute("menuNo", menuNo);
        }
            
        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        boardVO.setBbsId(boardVO.getBbsId());
        boardVO.setBbsNm(boardVO.getBbsNm());

        BoardMasterVO vo = new BoardMasterVO();
        vo.setBbsId(boardVO.getBbsId());
        vo.setUniqId(user.getUniqId());

        BoardMasterVO master = bbsAttrbService.selectBBSMasterInf(vo);

        //-------------------------------
        // 방명록이면 방명록 URL로 forward
        //-------------------------------
        if (master.getBbsTyCode().equals("BBST04")) {
            resultMap.put("success", false);
            resultMap.put("message", "방명록은 별도 API를 사용하세요");
            resultMap.put("redirectUrl", "/cop/bbs/selectGuestList.do");
            return ResponseEntity.ok(resultMap);
        }
        ////-----------------------------

        boardVO.setPageUnit(propertyService.getInt("pageUnit"));
        boardVO.setPageSize(propertyService.getInt("pageSize"));

        PaginationInfo paginationInfo = new PaginationInfo();

        paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(boardVO.getPageUnit());
        paginationInfo.setPageSize(boardVO.getPageSize());

        boardVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        boardVO.setLastIndex(paginationInfo.getLastRecordIndex());
        boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Map<String, Object> map = bbsMngService.selectBoardArticles(boardVO, vo.getBbsAttrbCode());
        int totCnt = Integer.parseInt((String)map.get("resultCnt"));

        paginationInfo.setTotalRecordCount(totCnt);

        //-------------------------------
        // 기본 BBS template 지정
        //-------------------------------
        if (master.getTmplatCours() == null || master.getTmplatCours().equals("")) {
            master.setTmplatCours("/css/egovframework/cop/bbs/egovBaseTemplate.css");
        }
        ////-----------------------------

        resultMap.put("success", true);
        resultMap.put("resultList", map.get("resultList"));
        resultMap.put("resultCnt", map.get("resultCnt"));
        resultMap.put("boardVO", boardVO);
        resultMap.put("brdMstrVO", master);
        resultMap.put("paginationInfo", paginationInfo);

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물에 대한 상세 정보를 조회한다.
     *
     * @param boardVO
     * @return ResponseEntity - 게시물 상세 정보
     * @throws Exception
     */
    @GetMapping("/cop/bbs/selectBoardArticle.do")
    public ResponseEntity<Map<String, Object>> selectBoardArticle(@ModelAttribute("searchVO") BoardVO boardVO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        LoginVO user = new LoginVO();
        if(EgovUserDetailsHelper.isAuthenticated()){
            user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        } else {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        // 조회수 증가 여부 지정
        boardVO.setPlusCount(true);

        //---------------------------------
        // 2009.06.29 : 2단계 기능 추가
        //---------------------------------
        if (!boardVO.getSubPageIndex().equals("")) {
            boardVO.setPlusCount(false);
        }
        ////-------------------------------

        boardVO.setLastUpdusrId(user.getUniqId());
        BoardVO vo = bbsMngService.selectBoardArticle(boardVO);

        //----------------------------
        // template 처리 (기본 BBS template 지정 포함)
        //----------------------------
        BoardMasterVO master = new BoardMasterVO();

        master.setBbsId(boardVO.getBbsId());
        master.setUniqId(user.getUniqId());

        BoardMasterVO masterVo = bbsAttrbService.selectBBSMasterInf(master);

        if (masterVo.getTmplatCours() == null || masterVo.getTmplatCours().equals("")) {
            masterVo.setTmplatCours("/css/egovframework/cop/bbs/egovBaseTemplate.css");
        }

        resultMap.put("success", true);
        resultMap.put("result", vo);
        resultMap.put("sessionUniqId", user.getUniqId());
        resultMap.put("brdMstrVO", masterVo);

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물 등록을 위한 정보를 조회한다.
     *
     * @param boardVO
     * @return ResponseEntity - 게시물 등록 페이지 정보
     * @throws Exception
     */
    @GetMapping("/cop/bbs/addBoardArticle.do")
    public ResponseEntity<Map<String, Object>> addBoardArticle(@ModelAttribute("searchVO") BoardVO boardVO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자권한 처리
        if(!EgovUserDetailsHelper.isAuthenticated()) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

        BoardMasterVO bdMstr = new BoardMasterVO();

        if (isAuthenticated) {
            BoardMasterVO vo = new BoardMasterVO();
            vo.setBbsId(boardVO.getBbsId());
            vo.setUniqId(user.getUniqId());

            bdMstr = bbsAttrbService.selectBBSMasterInf(vo);
        }

        //----------------------------
        // 기본 BBS template 지정
        //----------------------------
        if (bdMstr.getTmplatCours() == null || bdMstr.getTmplatCours().equals("")) {
            bdMstr.setTmplatCours("/css/egovframework/cop/bbs/egovBaseTemplate.css");
        }

        resultMap.put("success", true);
        resultMap.put("bdMstr", bdMstr);
        resultMap.put("brdMstrVO", bdMstr);

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물을 등록한다.
     *
     * @param multiRequest
     * @param boardVO
     * @param bdMstr
     * @param board
     * @param bindingResult
     * @return ResponseEntity - 게시물 등록 결과
     * @throws Exception
     */
    @PostMapping("/cop/bbs/insertBoardArticle.do")
    public ResponseEntity<Map<String, Object>> insertBoardArticle(
            final MultipartHttpServletRequest multiRequest, 
            @ModelAttribute("searchVO") BoardVO boardVO,
            @ModelAttribute("bdMstr") BoardMaster bdMstr, 
            @ModelAttribute("board") Board board, 
            BindingResult bindingResult) throws Exception {
        
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자권한 처리
        if(!EgovUserDetailsHelper.isAuthenticated()) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

        beanValidator.validate(board, bindingResult);
        if (bindingResult.hasErrors()) {
            BoardMasterVO master = new BoardMasterVO();
            BoardMasterVO vo = new BoardMasterVO();

            vo.setBbsId(boardVO.getBbsId());
            vo.setUniqId(user.getUniqId());

            master = bbsAttrbService.selectBBSMasterInf(vo);

            resultMap.put("success", false);
            resultMap.put("message", "입력값 검증에 실패했습니다.");
            resultMap.put("errors", bindingResult.getAllErrors());
            resultMap.put("bdMstr", master);
            resultMap.put("brdMstrVO", master);
            
            return ResponseEntity.badRequest().body(resultMap);
        }

        if (isAuthenticated) {
            List<FileVO> result = null;
            String atchFileId = "";

            final Map<String, MultipartFile> files = multiRequest.getFileMap();
            if (!files.isEmpty()) {
                result = fileUtil.parseFileInf(files, "BBS_", 0, "", "");
                atchFileId = fileMngService.insertFileInfs(result);
            }
            board.setAtchFileId(atchFileId);
            board.setFrstRegisterId(user.getUniqId());
            board.setBbsId(board.getBbsId());

            board.setNtcrNm("");    // dummy 오류 수정 (익명이 아닌 경우 validator 처리를 위해 dummy로 지정됨)
            board.setPassword("");  // dummy 오류 수정 (익명이 아닌 경우 validator 처리를 위해 dummy로 지정됨)
            //board.setNttCn(unscript(board.getNttCn()));    // XSS 방지

            bbsMngService.insertBoardArticle(board);
            
            resultMap.put("success", true);
            resultMap.put("message", "게시물이 등록되었습니다.");
            resultMap.put("bbsId", boardVO.getBbsId());
            resultMap.put("nttId", board.getNttId());
        } else {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물에 대한 답변 등록을 위한 정보를 조회한다.
     *
     * @param boardVO
     * @return ResponseEntity - 답변 등록 페이지 정보
     * @throws Exception
     */
    @GetMapping("/cop/bbs/addReplyBoardArticle.do")
    public ResponseEntity<Map<String, Object>> addReplyBoardArticle(@ModelAttribute("searchVO") BoardVO boardVO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자권한 처리
        if(!EgovUserDetailsHelper.isAuthenticated()) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();

        BoardMasterVO master = new BoardMasterVO();
        BoardMasterVO vo = new BoardMasterVO();

        vo.setBbsId(boardVO.getBbsId());
        vo.setUniqId(user.getUniqId());

        master = bbsAttrbService.selectBBSMasterInf(vo);

        //----------------------------
        // 기본 BBS template 지정
        //----------------------------
        if (master.getTmplatCours() == null || master.getTmplatCours().equals("")) {
            master.setTmplatCours("/css/egovframework/cop/bbs/egovBaseTemplate.css");
        }

        resultMap.put("success", true);
        resultMap.put("bdMstr", master);
        resultMap.put("result", boardVO);
        resultMap.put("brdMstrVO", master);

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물에 대한 답변을 등록한다.
     *
     * @param multiRequest
     * @param boardVO
     * @param bdMstr
     * @param board
     * @param bindingResult
     * @return ResponseEntity - 답변 등록 결과
     * @throws Exception
     */
    @PostMapping("/cop/bbs/replyBoardArticle.do")
    public ResponseEntity<Map<String, Object>> replyBoardArticle(
            final MultipartHttpServletRequest multiRequest, 
            @ModelAttribute("searchVO") BoardVO boardVO,
            @ModelAttribute("bdMstr") BoardMaster bdMstr, 
            @ModelAttribute("board") Board board, 
            BindingResult bindingResult) throws Exception {
        
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자권한 처리
        if(!EgovUserDetailsHelper.isAuthenticated()) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

        beanValidator.validate(board, bindingResult);
        if (bindingResult.hasErrors()) {
            BoardMasterVO master = new BoardMasterVO();
            BoardMasterVO vo = new BoardMasterVO();

            vo.setBbsId(boardVO.getBbsId());
            vo.setUniqId(user.getUniqId());

            master = bbsAttrbService.selectBBSMasterInf(vo);

            resultMap.put("success", false);
            resultMap.put("message", "입력값 검증에 실패했습니다.");
            resultMap.put("errors", bindingResult.getAllErrors());
            resultMap.put("bdMstr", master);
            resultMap.put("result", boardVO);
            resultMap.put("brdMstrVO", master);
            
            return ResponseEntity.badRequest().body(resultMap);
        }

        if (isAuthenticated) {
            final Map<String, MultipartFile> files = multiRequest.getFileMap();
            String atchFileId = "";

            if (!files.isEmpty()) {
                List<FileVO> result = fileUtil.parseFileInf(files, "BBS_", 0, "", "");
                atchFileId = fileMngService.insertFileInfs(result);
            }

            board.setAtchFileId(atchFileId);
            board.setReplyAt("Y");
            board.setFrstRegisterId(user.getUniqId());
            board.setBbsId(board.getBbsId());
            board.setParnts(Long.toString(boardVO.getNttId()));
            board.setSortOrdr(boardVO.getSortOrdr());
            board.setReplyLc(Integer.toString(Integer.parseInt(boardVO.getReplyLc()) + 1));

            board.setNtcrNm("");    // dummy 오류 수정 (익명이 아닌 경우 validator 처리를 위해 dummy로 지정됨)
            board.setPassword("");  // dummy 오류 수정 (익명이 아닌 경우 validator 처리를 위해 dummy로 지정됨)

            board.setNttCn(unscript(board.getNttCn()));    // XSS 방지

            bbsMngService.insertBoardArticle(board);
            
            resultMap.put("success", true);
            resultMap.put("message", "답변이 등록되었습니다.");
            resultMap.put("bbsId", boardVO.getBbsId());
            resultMap.put("nttId", board.getNttId());
        } else {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물 수정을 위한 정보를 조회한다.
     *
     * @param boardVO
     * @param vo
     * @return ResponseEntity - 게시물 수정 페이지 정보
     * @throws Exception
     */
    @GetMapping("/cop/bbs/forUpdateBoardArticle.do")
    public ResponseEntity<Map<String, Object>> selectBoardArticleForUpdt(
            @ModelAttribute("searchVO") BoardVO boardVO, 
            @ModelAttribute("board") BoardVO vo) throws Exception {
        
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자권한 처리 (자유게시판에 대한 요청이 아닌 경우는 로그인 필요)
        if(!boardVO.getBbsId().equals("BBSMSTR_BBBBBBBBBBBB") && !EgovUserDetailsHelper.isAuthenticated()) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

        boardVO.setFrstRegisterId(user.getUniqId());

        BoardMaster master = new BoardMaster();
        BoardMasterVO bmvo = new BoardMasterVO();
        BoardVO bdvo = new BoardVO();

        vo.setBbsId(boardVO.getBbsId());

        master.setBbsId(boardVO.getBbsId());
        master.setUniqId(user.getUniqId());

        if (isAuthenticated) {
            bmvo = bbsAttrbService.selectBBSMasterInf(master);
            bdvo = bbsMngService.selectBoardArticle(boardVO);
        }

        //----------------------------
        // 기본 BBS template 지정
        //----------------------------
        if (bmvo.getTmplatCours() == null || bmvo.getTmplatCours().equals("")) {
            bmvo.setTmplatCours("/css/egovframework/cop/bbs/egovBaseTemplate.css");
        }

        resultMap.put("success", true);
        resultMap.put("result", bdvo);
        resultMap.put("bdMstr", bmvo);
        resultMap.put("brdMstrVO", bmvo);

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물에 대한 내용을 수정한다.
     *
     * @param multiRequest
     * @param boardVO
     * @param bdMstr
     * @param board
     * @param bindingResult
     * @return ResponseEntity - 게시물 수정 결과
     * @throws Exception
     */
    @PostMapping("/cop/bbs/updateBoardArticle.do")
    public ResponseEntity<Map<String, Object>> updateBoardArticle(
            final MultipartHttpServletRequest multiRequest, 
            @ModelAttribute("searchVO") BoardVO boardVO,
            @ModelAttribute("bdMstr") BoardMaster bdMstr, 
            @ModelAttribute("board") Board board, 
            BindingResult bindingResult) throws Exception {
        
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자권한 처리
        if(!EgovUserDetailsHelper.isAuthenticated()) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

        String atchFileId = boardVO.getAtchFileId();

        beanValidator.validate(board, bindingResult);
        if (bindingResult.hasErrors()) {
            boardVO.setFrstRegisterId(user.getUniqId());

            BoardMaster master = new BoardMaster();
            BoardMasterVO bmvo = new BoardMasterVO();
            BoardVO bdvo = new BoardVO();

            master.setBbsId(boardVO.getBbsId());
            master.setUniqId(user.getUniqId());

            bmvo = bbsAttrbService.selectBBSMasterInf(master);
            bdvo = bbsMngService.selectBoardArticle(boardVO);

            resultMap.put("success", false);
            resultMap.put("message", "입력값 검증에 실패했습니다.");
            resultMap.put("errors", bindingResult.getAllErrors());
            resultMap.put("result", bdvo);
            resultMap.put("bdMstr", bmvo);
            
            return ResponseEntity.badRequest().body(resultMap);
        }

        if (isAuthenticated) {
            final Map<String, MultipartFile> files = multiRequest.getFileMap();
            if (!files.isEmpty()) {
                if ("".equals(atchFileId)) {
                    List<FileVO> result = fileUtil.parseFileInf(files, "BBS_", 0, atchFileId, "");
                    atchFileId = fileMngService.insertFileInfs(result);
                    board.setAtchFileId(atchFileId);
                } else {
                    FileVO fvo = new FileVO();
                    fvo.setAtchFileId(atchFileId);
                    int cnt = fileMngService.getMaxFileSN(fvo);
                    List<FileVO> _result = fileUtil.parseFileInf(files, "BBS_", cnt, atchFileId, "");
                    fileMngService.updateFileInfs(_result);
                }
            }

            board.setLastUpdusrId(user.getUniqId());

            board.setNtcrNm("");    // dummy 오류 수정 (익명이 아닌 경우 validator 처리를 위해 dummy로 지정됨)
            board.setPassword("");  // dummy 오류 수정 (익명이 아닌 경우 validator 처리를 위해 dummy로 지정됨)
            board.setNttCn(unscript(board.getNttCn()));    // XSS 방지

            bbsMngService.updateBoardArticle(board);
            
            resultMap.put("success", true);
            resultMap.put("message", "게시물이 수정되었습니다.");
            resultMap.put("bbsId", boardVO.getBbsId());
            resultMap.put("nttId", board.getNttId());
        } else {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 게시물에 대한 내용을 삭제한다.
     *
     * @param boardVO
     * @param board
     * @param bdMstr
     * @return ResponseEntity - 게시물 삭제 결과
     * @throws Exception
     */
    @PostMapping("/cop/bbs/deleteBoardArticle.do")
    public ResponseEntity<Map<String, Object>> deleteBoardArticle(
            @ModelAttribute("searchVO") BoardVO boardVO, 
            @ModelAttribute("board") Board board,
            @ModelAttribute("bdMstr") BoardMaster bdMstr) throws Exception {
        
        Map<String, Object> resultMap = new HashMap<>();
        
        // 사용자권한 처리
        if(!EgovUserDetailsHelper.isAuthenticated()) {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

        if (isAuthenticated) {
            board.setLastUpdusrId(user.getUniqId());
            bbsMngService.deleteBoardArticle(board);
            
            resultMap.put("success", true);
            resultMap.put("message", "게시물이 삭제되었습니다.");
            resultMap.put("bbsId", boardVO.getBbsId());
        } else {
            resultMap.put("success", false);
            resultMap.put("message", egovMessageSource.getMessage("fail.common.login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
        }

        return ResponseEntity.ok(resultMap);
    }

    /**
     * 템플릿에 대한 미리보기용 게시물 목록을 조회한다.
     *
     * @param boardVO
     * @return ResponseEntity - 미리보기 게시물 목록
     * @throws Exception
     */
    @GetMapping("/cop/bbs/previewBoardList.do")
    public ResponseEntity<Map<String, Object>> previewBoardArticles(@ModelAttribute("searchVO") BoardVO boardVO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        String template = boardVO.getSearchWrd();    // 템플릿 URL

        BoardMasterVO master = new BoardMasterVO();
        master.setBbsNm("미리보기 게시판");

        boardVO.setPageUnit(propertyService.getInt("pageUnit"));
        boardVO.setPageSize(propertyService.getInt("pageSize"));

        PaginationInfo paginationInfo = new PaginationInfo();

        paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(boardVO.getPageUnit());
        paginationInfo.setPageSize(boardVO.getPageSize());

        boardVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        boardVO.setLastIndex(paginationInfo.getLastRecordIndex());
        boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        BoardVO target = null;
        List<BoardVO> list = new ArrayList<BoardVO>();

        target = new BoardVO();
        target.setNttSj("게시판 기능 설명");
        target.setFrstRegisterId("ID");
        target.setFrstRegisterNm("관리자");
        target.setFrstRegisterPnttm("2009-01-01");
        target.setInqireCo(7);
        target.setParnts("0");
        target.setReplyAt("N");
        target.setReplyLc("0");
        target.setUseAt("Y");

        list.add(target);

        target = new BoardVO();
        target.setNttSj("게시판 부가 기능 설명");
        target.setFrstRegisterId("ID");
        target.setFrstRegisterNm("관리자");
        target.setFrstRegisterPnttm("2009-01-01");
        target.setInqireCo(7);
        target.setParnts("0");
        target.setReplyAt("N");
        target.setReplyLc("0");
        target.setUseAt("Y");

        list.add(target);

        boardVO.setSearchWrd("");

        int totCnt = list.size();

        paginationInfo.setTotalRecordCount(totCnt);

        master.setTmplatCours(template);

        resultMap.put("success", true);
        resultMap.put("resultList", list);
        resultMap.put("resultCnt", Integer.toString(totCnt));
        resultMap.put("boardVO", boardVO);
        resultMap.put("brdMstrVO", master);
        resultMap.put("paginationInfo", paginationInfo);
        resultMap.put("preview", "true");

        return ResponseEntity.ok(resultMap);
    }
}
