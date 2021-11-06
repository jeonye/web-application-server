package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class UserHandler {
    private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

    /**
     * 사용자 등록
     * @param HTTP 본문 데이터
     * @return
     */
    public static void addUser(String bodyData) throws UnsupportedEncodingException{
        // HTTP 본문에 있는 데이터로 사용자 객체 생성
        User newUser = parseUserInfo(bodyData);
        // 사용자 등록
        DataBase.addUser(newUser);

        // 등록된 사용자 조회
        User user = DataBase.findUserById(newUser.getUserId());
        log.info("Success Create User : {}", user.getUserId());
    }

    /**
     * 등록된 사용자 여부 확인
     * @param HTTP 본문 데이터
     * @return 사용자 존재 여부
     */
    public static boolean confirmUser(String bodyData) throws UnsupportedEncodingException {
        // HTTP 본문에 있는 데이터로 사용자 객체 생성
        User loginUser = parseUserInfo(bodyData);
        // 사용자 존재 여부 확인
        return isSavedUser(loginUser);
    }

    /**
     * 사용자 존재 여부 확인
     * @param HTTP 본문 데이터
     * @return 사용자 존재 여부
     */
    private static boolean isSavedUser(User targetUser) {
        // DB에 저장된 사용자 정보 조회
        User stdUser = DataBase.findUserById(targetUser.getUserId());

        if(stdUser != null) {
            return (stdUser.getPassword().equals(targetUser.getPassword()));
        }
        return false;
    }

    /**
     * 사용자 목록 조회
     * @param HTTP 본문 데이터
     * @return 사용자 목록
     */
    public static Collection<User> getUserList(Map<String, String> headerMap) {
        // 로그인 되어 있는 경우에만 사용자 목록 조회
        if(checkLogined(headerMap)) {
            return DataBase.findAll();
        };

        return new ArrayList<User>();
    }

    /**
     * 로그인 상태 확인
     * @param HTTP header 정보
     * @return 로그인 여부
     */
    public static boolean checkLogined(Map<String, String> headerMap) {
        // header에서 쿠키 정보 조회
        String cookies = headerMap.get("Cookie");
        Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookies);
        // 로그인 상태 조회
        return Boolean.parseBoolean(cookieMap.get("logined"));
    }

    /**
     * 사용자 객체 생성
     * @param HTTP header 정보
     * @return 사용자 정보
     */
    private static User parseUserInfo(String params) throws UnsupportedEncodingException {
        log.debug("Parse User Info - Params : {}", params);

        // URL 파라미터 파싱
        Map<String, String> paramMap = HttpRequestUtils.parseQueryString(params);
        log.debug("ParamMap : {}", paramMap.toString());

        // 사용자 객체 생성
        String userId = paramMap.get("userId");
        String password = paramMap.get("password");
        String name = paramMap.get("name");
        String email = paramMap.get("email");
        if(!"".equals(email) && email != null) {
            email = URLDecoder.decode(email, "UTF-8");
        }
        return new User(userId, password, name, email);
    }
}
