package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private boolean isLogined = false;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            DataOutputStream dos = new DataOutputStream(out);

            // 요청 URL에 따라 로직 처리
            String url = processByUrl(dos, br);
            log.debug("Response URL : {}", url);

            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 요청 URL 별로 로직 처리
     * @param
     * @return 처리 후 이동할 Page Url
     */
    private String processByUrl(DataOutputStream dos, BufferedReader br) throws IOException {
        String header = br.readLine();
        String url = HttpRequestUtils.parseUrl(header);
        log.debug("Request URL : {}", url);

        // 회원 가입
        if("/user/create".equals(url)) {
            userJoin(dos, br, header);
        }

        // 로그인
        if("/user/login".equals(url)) {
            login(dos, br, header);
        }

        // 사용자 목록 조회
        if("/user/list".equals(url)) {
            getUserList(dos, br, header);
        }

        return url;
    }

    /**
     * 회원 가입
     * @param header 시작줄
     * @return
     */
    private void userJoin(DataOutputStream dos, BufferedReader br, String header) throws IOException{
        try {
            // HTTP 본문 데이터 조회
            String bodyData = readBodyData(br, header);
            // 사용자 등록
            UserHandler.addUser(bodyData);
            // 처리 결과 응답
            response302Header(dos, "/index.html", false);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 로그인
     * @param header 시작줄
     * @return
     */
    private void login(DataOutputStream dos, BufferedReader br, String header) throws IOException{
        // HTTP 본문 데이터 조회
        String bodyData = readBodyData(br, header);
        // 처리 결과 응답
        response302Header(dos, resLoginUrl(bodyData), true);
    }

    /**
     * 로그인
     * @param header 시작줄
     * @return
     */
    private void getUserList(DataOutputStream dos, BufferedReader br, String header) throws IOException{
        // 요청 header의 데이터 파싱
        Map<String, String> headerMap = readHeaderData(br, header);
        // 로그인 상태 확인
        boolean isLogined = UserHandler.checkLogined(headerMap);

        if(isLogined) { // 로그인 되어있는 경우
            String location = createHTML(headerMap);
            // 처리 결과 응답
            response302Header(dos, location, false);
        } else {
            log.debug("You need to log in");
            // 처리 결과 응답
            response302Header(dos, "/user/login.html", false);
        }
    }

    /**
     * HTML 파일 생성
     * @param
     * @return 생성한 HTML 파일 명
     */
    private String createHTML(Map<String, String> headerMap) throws IOException {
        // HTML 파일 생성
        String filename = "/user/user_list.html";
        File file = new File("./webapp" + filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        // HTML 내용 작성
        writer.write(writeHTML(headerMap));

        writer.close();

        return filename;
    }

    /**
     * HTML 내용 작성
     * @param
     * @return HTML에 기재될 내용
     */
    private String writeHTML(Map<String, String> headerMap) {
        // 사용자 목록 조회
        List<User> users = new ArrayList<User>(UserHandler.getUserList(headerMap));
        StringBuilder sb = new StringBuilder();

        // HTML 기본 포맷 작성
        sb.append("<!DOCTYPE html> \r\n");
        sb.append("<html lang=\"kr\"> \r\n");
        sb.append("<head> \r\n");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"> \r\n");
        sb.append("<meta charset=\"utf-8\"> \r\n");
        sb.append("<title>SLiPP Java Web Programming</title> \r\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\"> \r\n");
        sb.append("<link href=\"../css/bootstrap.min.css\" rel=\"stylesheet\"> \r\n");
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

    /**
     * Login 요청에 대한 결과 페이지 조회
     * @param
     * @return 응답 페이지
     */
    private String resLoginUrl(String bodyData) {
        // Login 실패 시 이동할 페이지
        String url = "/user/login_failed.html";

        try {
            // 로그인 성공할 경우
            if(isLogined = UserHandler.confirmUser(bodyData)) {
                log.debug("Success Login");
                url = "/index.html";
            }
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }

        return url;
    }

    /**
     * 요청 header의 데이터 파싱
     * @param header
     * @return header 데이터
     */
    private Map<String, String> readHeaderData(BufferedReader br, String header) throws IOException {
        Map<String, String> headerMap = new HashMap<String, String>();

        // Read Header
        while (!"".equals(header))  {
            if(header == null) {
                break;
            }

            log.trace("Read Header Info : {}", header);

            // Header 내용 파싱
            HttpRequestUtils.Pair token = HttpRequestUtils.parseHeader(header);
            if(token != null) {
                headerMap.put(token.getKey(), token.getValue());
            }

            // 다음 라인 Read
            header = br.readLine();
        }

        return headerMap;
    }

    /**
     * 요청 body의 데이터 파싱
     * @param header
     * @return body 데이터
     */
    private String readBodyData(BufferedReader br, String header) throws IOException {
        // 요청 header의 데이터 파싱
        Map<String, String> headerMap = readHeaderData(br, header);
        // header에 있는 Content 길이 조회
        int contentLength = Integer.parseInt(headerMap.get("Content-Length"));

        // 요청 body 데이터 파싱
        return IOUtils.readData(br, contentLength);
    }

    /**
     * 200 Response 내용 작성
     * @param
     * @return
     */
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Accept: text/css,*/*;q=0.1 \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 302 Response 내용 작성
     * @param
     * @return
     */
    private void response302Header(DataOutputStream dos, String location, boolean isLoginReq) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n" );
            // 로그인 요청한 경우, 로그인 결과를 쿠키에 설정
            if(isLoginReq) {
                dos.writeBytes("Set-Cookie: logined=" + isLogined + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 요청에 대해 Response
     * @param
     * @return
     */
    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
