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
    public static void addUser(User newUser) {
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
    public static boolean confirmUser(User user) throws UnsupportedEncodingException {
        // 사용자 존재 여부 확인
        return isSavedUser(user);
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

}
