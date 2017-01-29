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
            String webDir = this.getClass().getClassLoader().getResource("WEB-INF").toExternalForm();

            ResourceHandler res = new ResourceHandler();
            res.setResourceBase(webDir);
            res.setDirAllowed(true);

            HandlerList hList = new HandlerList();
            hList.setHandlers(new Handler[]{
                    new MainHandler(this),
                    res
                    });
            server.setHandler(hList);
            server.start();
            // server.join();
        } catch (Exception e) {
            System.out.println("Error running server" );
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LocalSudokuServer().start();
    }
}
