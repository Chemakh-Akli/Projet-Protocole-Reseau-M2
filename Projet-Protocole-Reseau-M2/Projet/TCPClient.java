import java.io.*;
import java.net.*;

public class TCPClient {

  Socket socket;

  public TCPClient(String adr, String port_s){
    try {
      int port_serveur = Integer.parseInt(port_s);
      this.socket = new Socket(adr, port_serveur);
      new TCPRouter(this.socket).run();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
