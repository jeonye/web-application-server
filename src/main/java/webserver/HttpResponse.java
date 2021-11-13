package webserver;

/*
1. 응답 데이터 처리 중복 제거
2. 응답 헤더는 Map<String, String>에 저장해 관리
3. 응답을 보낼 때 HTML, CSS, 자바스크립트 파일을 직접 읽어 응답으로 보내는 메소드는 forward()
   다른 URL로 리다이렉트하는 메소드는 sendRedirect() 메소드를 나누어 구현
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    DataOutputStream dos = null;
    Map<String, String> headerMap = new HashMap<String, String>();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String url) {
        try {
            log.debug("Forward Page : {}", url);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

            if(url.endsWith(".css")) {
                headerMap.put("Content-Type", "text/css");
            } else if(url.endsWith(".js")) {
                headerMap.put("Content-Type", "application/javascript");
            } else {
                headerMap.put("Content-Type", "text/html;charset=utf-8");
            }

            headerMap.put("Content-Length", String.valueOf(body.length));
            response200Header();
            responseBody(body);
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public void forwardBody(String body) {
        byte[] contents = body.getBytes();
        headerMap.put("Content-Type", "text/html;charset=utf-8");
        headerMap.put("Content-Length", String.valueOf(contents.length));
        response200Header();
        responseBody(contents);
    }

    private void response200Header() {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            writeHeader();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n" );
            writeHeader();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addHeader(String option, String value) {
        headerMap.put(option, value);
    }

    private void writeHeader() {
        try {
            for(Map.Entry<String, String> element : headerMap.entrySet()) {
                dos.writeBytes(element.getKey() + ": " + element.getValue() + "\r\n");
            }
        } catch (IOException ie) {
            log.error(ie.getMessage());
        }
    }
}
