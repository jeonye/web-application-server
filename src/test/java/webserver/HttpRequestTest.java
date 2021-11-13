package webserver;

import constants.HttpMethod;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class HttpRequestTest {

    private HttpMethod httpMethod;
    private String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
        HttpRequest request = new HttpRequest(in);

        assertEquals(HttpMethod.GET, HttpMethod.valueOf(request.getMethod()));
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("jeonye", request.getParameter("userId"));
    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        HttpRequest request = new HttpRequest(in);

        assertEquals(HttpMethod.POST, HttpMethod.valueOf(request.getMethod()));
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("jeonye", request.getParameter("userId"));
    }
}
