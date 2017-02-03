import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Handler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LocalServerTest {
    private static final String SERVER_URL = "http://localhost:" + LocalSudokuServer.PORT;

    private HttpClient httpClient;

    private final static LocalSudokuServer serverWithLoopback = new LocalSudokuServer() {
        @Override
        Handler[] createHandlers() {
            ParsingHandler parsingHandlerWithLoopback = new ParsingHandler(server,
                    (req, res) -> new StandardRequestProcessor(req, res) {
                        @Override
                        public void loadField() throws IOException {
                            final Part upload;
                            try {
                                upload = request.getPart("upload");
                            } catch (ServletException e) {
                                throw new RuntimeException(e);
                            }

                            final String fileName = upload.getSubmittedFileName();
                            final String fileContent = IOUtils.toString(upload.getInputStream(), StandardCharsets.UTF_8);
                            final PrintWriter writer = response.getWriter();
                            writer.printf(fileName + "=" + fileContent);
                            writer.close();
                        }
                    });

            return new Handler[]{ multipartSafeWrapper(parsingHandlerWithLoopback) };
        }
    };


    @BeforeClass
    public static void setUp() throws Exception {
        serverWithLoopback.start();
    }

    @Before
    public void setUpClient() throws Exception {
        httpClient = HttpClientBuilder.create().build();
    }

    @Test
    public void getFromLocalConnection() throws Exception {
        HttpGet httpGet = new HttpGet(SERVER_URL);
        HttpResponse response = httpClient.execute(httpGet);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpServletResponse.SC_OK));
    }

    @Test
    public void multipartFileUpload() throws Exception {
        String rawDataText = "SAMPLE FILE CONTENTS";
        String rawFileName = "rawData.txt";

        InputStream rawData = new ByteArrayInputStream(rawDataText.getBytes());

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("upload", new InputStreamBody(rawData, rawFileName))
                .build();

        HttpPost httpPost = new HttpPost(SERVER_URL + "/load");
        httpPost.addHeader("SomeHeader", "SomeValue");
        httpPost.setEntity(reqEntity);

        HttpResponse response = httpClient.execute(httpPost);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpServletResponse.SC_OK));
        String talkback = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        assertThat(talkback, is(rawFileName + "=" + rawDataText));
    }
}
