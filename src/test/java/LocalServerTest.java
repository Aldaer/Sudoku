import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LocalServerTest {
    private final static LocalSudokuServer server = new LocalSudokuServer();

    @BeforeClass
    public static void setUp() throws Exception {
        server.start();
    }

    @Test
    public void getFromLocalConnection() throws Exception {
        URL url = new URL("http", "localhost", LocalSudokuServer.PORT, "/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        assertThat(conn.getResponseCode(), is(HttpURLConnection.HTTP_OK));
        System.out.println(IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8));
        conn.disconnect();
    }
}
