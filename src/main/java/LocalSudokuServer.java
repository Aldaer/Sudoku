import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class LocalSudokuServer extends Thread {
    static final int PORT = 80; //8888;

    final Server server = new Server(PORT);

    @Override
    public void run() {
        try {
            HandlerList hList = new HandlerList();
            hList.setHandlers(createHandlers());
            server.setHandler(hList);
            server.start();
        } catch (Exception e) {
            System.out.println("Error running server");
            e.printStackTrace();
        }
    }

    Handler[] createHandlers() {
        ResourceHandler res = new ResourceHandler();
        @SuppressWarnings("ConstantConditions")
        String webDir = this.getClass().getClassLoader().getResource("WEB-INF").toExternalForm();
        res.setResourceBase(webDir);
        res.setDirAllowed(true);

        Handler main = new MainHandler(this);

        return new Handler[]{main, res};
    }

    public static void main(String[] args) {
        new LocalSudokuServer().start();
    }
}
