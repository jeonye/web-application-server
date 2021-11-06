package webserver;

import model.User;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RequestHandlerTest {

    @Test
    public void createHTML() throws Exception {
        Map<String, String> headerMap = new HashMap<String, String>();
        RequestHandler reqHandler = new RequestHandler(new Socket());
        Method method = reqHandler.getClass().getDeclaredMethod("createHTML", Map.class);
        method.setAccessible(true);

        String filename = (String) method.invoke(reqHandler, headerMap);
        assertEquals("/user/user_list.html", filename);
    }

    @Test
    public void writeHTML() throws Exception {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Cookie","logined=true");
        RequestHandler reqHandler = new RequestHandler(new Socket());
        Method method = reqHandler.getClass().getDeclaredMethod("writeHTML", Map.class);
        method.setAccessible(true);

        String html = (String) method.invoke(reqHandler, headerMap);
        assertEquals(getHtmlFormat(), html);
    }

    private String getHtmlFormat() {
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
        sb.append("</tbody> \r\n");
        sb.append("</table> \r\n");
        sb.append("</body> \r\n");
        sb.append("</html> \r\n");

        return sb.toString();
    }

    @Test
    public void resLoginUrl_fail() throws Exception {
        String bodyData = "userId=jeonye&password=jeonye";
        RequestHandler reqHandler = new RequestHandler(new Socket());
        Method method = reqHandler.getClass().getDeclaredMethod("resLoginUrl", String.class);
        method.setAccessible(true);

        String url = (String) method.invoke(reqHandler, bodyData);
        assertEquals("/user/login_failed.html", url);
    }

    @Test
    public void resLoginUrl_success() throws Exception {
        String bodyData = "userId=jeonye&password=jeonye&name=jeonye&email=jeonye";
        RequestHandler reqHandler = new RequestHandler(new Socket());
        Method method = reqHandler.getClass().getDeclaredMethod("resLoginUrl", String.class);
        method.setAccessible(true);

        String url = (String) method.invoke(reqHandler, bodyData);
        assertEquals("/index.html", url);
    }

    @Test
    public void parseUserInfo() throws Exception {
        String url="/user/create?userId=jeonye&password=password&name=JeonYe&email=jeonye%40gmail.com";

        RequestHandler reqHandler = new RequestHandler(new Socket());
        Method method = reqHandler.getClass().getDeclaredMethod("parseUserInfo", String.class);
        method.setAccessible(true);

        User user = (User) method.invoke(reqHandler, url);
        assertEquals("jeonye", user.getUserId());
        assertEquals("password", user.getPassword());
        assertEquals("JeonYe", user.getName());
        assertEquals("jeonye%40gmail.com", user.getEmail());

    }

    @Test
    public void parseContentLength() throws Exception {
        String header = "Content-Length: 68";
        RequestHandler reqHandler = new RequestHandler(new Socket());
        Method method = reqHandler.getClass().getDeclaredMethod("parseContentLength", String.class);
        method.setAccessible(true);

        int contentLength = (int) method.invoke(reqHandler, header);
        assertEquals(68, contentLength);

        header = "Cache-Control: max-age=0";
        contentLength = (int) method.invoke(reqHandler, header);
        assertEquals(-1, contentLength);
    }
}
