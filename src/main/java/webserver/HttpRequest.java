package webserver;

/*
1. 클라이언트 요청 데이터를 담고 있는 InputStream을 생성자로 받아 HTTP 메소드, URL, 헤더, 본문을 분리하는 작업
2. 헤더는 Map<String, String>에 저장해 관리하고, getHeader("필드 이름") 메소드를 통해 접근 가능
3. GET과 POST 메소드에 따라 전달되는 인자를 Map<String, String>에 저장해 관리하고 getParameter("인자이름") 메소드를 통해 접근 가능
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    String header = "";
    Map<String, String> headerMap = new HashMap<String, String>();
    Map<String, String> paramMap = new HashMap<String, String>();

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String header = br.readLine();
            this.header = header;
            log.debug("[HttpRequest] header : {}", header);

            if (this.header == null) {
                return;
            }

            // 헤더 정보 조회
            while (!"".equals(header) && header != null) {
                HttpRequestUtils.Pair token = HttpRequestUtils.parseHeader(header);

                if (token != null) {
                    this.headerMap.put(token.getKey(), token.getValue());
                }

                header = br.readLine();
            }

            // 본문 데이터 조회
            if("GET".equals(getMethod())) {
                String url = this.header.split(" ")[1];
                String urlParam = url.substring(url.indexOf("?")+1);
                this.paramMap = HttpRequestUtils.parseQueryString(urlParam);
            }

            if("POST".equals(getMethod())) {
                String bodyData = IOUtils.readData(br, Integer.parseInt(getHeader("Content-Length")));
                this.paramMap = HttpRequestUtils.parseQueryString(bodyData);
            }

        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * 요청 데이터로부터 HTTP 메소드 조회
     * @param
     * @return String
     */
    public String getMethod() throws IOException {
        log.debug("[HttpRequest] [getMethod] header : {}", this.header);
        return HttpRequestUtils.parseMethod(this.header);
    }

    /**
     * 요청 데이터로부터 URL 조회
     * @param
     * @return String
     */
    public String getPath() throws IOException {
        String path = HttpRequestUtils.parseUrl(this.header).split("\\?")[0];
        log.info("[HttpRequest] [getPath] Header : {} / Path : {}", this.header, path);

        return path;
    }

    /**
     * 요청 데이터로부터 헤더 조회
     * @param headerName
     * @return String
     */
    public String getHeader(String headerName) throws IOException {
        String headerValue = this.headerMap.get(headerName);
        log.info("[HttpRequest] [getHeader] Header Value : {}", headerValue);

        return headerValue;
    }

    /**
     * 요청 데이터로부터 파라미터 조회
     * @param paramName
     * @return String
     */
    public String getParameter(String paramName) throws IOException {
        String paramValue = this.paramMap.get(paramName);
        log.info("[HttpRequest] [getParameter] Parameter Value : {}", paramValue);

        return paramValue;
    }
}
