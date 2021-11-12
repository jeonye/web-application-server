package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

public class UserHandler {
    private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

    /**
     * 사용자 등록
     * @param paramMap
     * @return
     */
    public static void addUser(Map<String, String> paramMap) throws UnsupportedEncodingException{
        // HTTP 본문에 있는 데이터로 사용자 객체 생성
        User newUser = parseUserInfo(paramMap);
        // 사용자 등록
        DataBase.addUser(newUser);

        // 등록된 사용자 조회
        User user = DataBase.findUserById(newUser.getUserId());
        log.info("Success Create User : {}", user.getUserId());
    }

    /**
     * 등록된 사용자 여부 확인
     * @param paramMap
     * @return 사용자 존재 여부
     */
    public static boolean confirmUser(Map<String, String> paramMap) throws UnsupportedEncodingException {
        // HTTP 본문에 있는 데이터로 사용자 객체 생성
        User loginUser = parseUserInfo(paramMap);
        // 사용자 존재 여부 확인
        return isSavedUser(loginUser);
    }

    /**
     * 사용자 존재 여부 확인
     * @param targetUser
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
     * 로그인 상태 확인
     * @param cookies
     * @return 로그인 여부
     */
    public static boolean checkLogined(String cookies) {
        Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookies);
        // 로그인 상태 조회
        return Boolean.parseBoolean(cookieMap.get("logined"));
    }

    /**
     * 사용자 객체 생성
     * @param paramMap
     * @return 사용자 정보
     */
    private static User parseUserInfo(Map<String, String> paramMap) throws UnsupportedEncodingException {
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
