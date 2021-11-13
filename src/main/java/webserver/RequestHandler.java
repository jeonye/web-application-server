package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private String defaultPath = "/index.html";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            if("/user/create".equals(request.getPath())) {   // 회원가입
                userJoin(request, response);
            } else if("/user/login".equals(request.getPath())) {   // 로그인
                login(request, response);
            } else if("/user/list".equals(request.getPath())) {    // 사용자 목록 조회
                getUserList(request, response);
            } else {
                response.forward(request.getPath());
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 회원 가입
     * @param request, response
     * @return
     */
    private void userJoin(HttpRequest request, HttpResponse response) throws IOException{
        UserHandler.addUser(createUser(request));
        response.sendRedirect(defaultPath);
    }

    /**
     * 로그인
     * @param request, response
     * @return
     */
    private void login(HttpRequest request, HttpResponse response) {
        // Login 실패 시 이동할 페이지
        String url = "/user/login_failed.html";

        try {
            // 로그인 성공할 경우
            if(UserHandler.confirmUser(createUser(request))) {
                log.debug("Success Login");
                response.addHeader("Set-Cookie", "logined=true");
                url = defaultPath;
            }
        } catch (UnsupportedEncodingException uee) {
            log.error(uee.getMessage());
        } catch (IOException ie) {
            log.error(ie.getMessage());
        }

        response.sendRedirect(url);
    }

    /**
     * 로그인
     * @param request, response
     * @return
     */
    private void getUserList(HttpRequest request, HttpResponse response) throws IOException{
        // 로그인 상태 확인
        boolean isLogined = UserHandler.checkLogined(request.getHeader("Cookie"));

        if(isLogined) { // 로그인 되어있는 경우
            ArrayList<User> users = new ArrayList<User>(DataBase.findAll());
            String html = writeHTML(users);
            // 처리 결과 응답
            response.forwardBody(html);
        } else {
            log.debug("You need to log in");
            // 처리 결과 응답
            response.sendRedirect("/user/login.html");
        }
    }

    /**
     * HTML 내용 작성
     * @param
     * @return HTML에 기재될 내용
     */
    private String writeHTML(ArrayList<User> users) {
        StringBuilder sb = new StringBuilder();

        // HTML 기본 포맷 작성
        sb.append("<!DOCTYPE html> \r\n");
        sb.append("<html lang=\"kr\"> \r\n");
        sb.append("<head> \r\n");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"> \r\n");
        sb.append("<meta charset=\"utf-8\"> \r\n");
        sb.append("<title>SLiPP Java Web Programming</title> \r\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\"> \r\n");
        sb.append("<link href=\"/css/bootstrap.min.css\" rel=\"stylesheet\"> \r\n");
        sb.append("</head> \r\n");
        sb.append("<body> \r\n");

        sb.append("<table class=\"table table-hover\"> \r\n");
        sb.append("<thead> \r\n");
        sb.append("<tr> \r\n");
        sb.append("<th>#</th> <th>사용자 아이디</th> <th>이름</th> <th>이메일</th><th></th> \r\n");
        sb.append("</tr> \r\n");
        sb.append("</thead> \r\n");
        sb.append("<tbody> \r\n");

        // 사용자 수만큼 반복하며 출력
        for(int idx=0; idx<users.size(); idx++) {
            User user = users.get(idx);

            sb.append("<tr> \r\n");
            sb.append("<th scope=\"row\">" + (idx+1) + "</th> \r\n");
            sb.append("<td>" + user.getUserId() + "</td> \r\n");
            sb.append("<td>" + user.getName() + "</td> \r\n");
            sb.append("<td>" + user.getEmail() + "</td> \r\n");
            sb.append("</tr> \r\n");
        }

        sb.append("</tbody> \r\n");
        sb.append("</table> \r\n");
        sb.append("</body> \r\n");
        sb.append("</html> \r\n");

        return sb.toString();
    }

    private static User createUser(HttpRequest request) throws IOException {
        // 사용자 객체 생성
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        if(!"".equals(name) && name != null) {
            name = URLDecoder.decode(name, "UTF-8");
        }

        if(!"".equals(email) && email != null) {
            email = URLDecoder.decode(email, "UTF-8");
        }
        return new User(userId, password, name, email);
    }

}
