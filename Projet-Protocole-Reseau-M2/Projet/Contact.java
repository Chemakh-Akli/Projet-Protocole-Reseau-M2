import java.net.*;
import java.io.*;
import java.lang.*;
import java.security.* ;
import javax.net.ssl.*;

public class Contact{

  private Integer port;
  private SSLSocket sock;
  private PrintWriter pw;
  private BufferedReader br;

  public Contact( SSLSocket s){
    try{
      this.sock = s;
      this.port = this.sock.getPort();
      this.pw = new PrintWriter(new OutputStreamWriter(this.sock.getOutputStream()));
      this.br = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public SSLSocket getSocket(){
    return sock;
  }

  public PrintWriter getPW(){
    return pw;
  }

  public BufferedReader getBR(){
    return br;
  }

  public Integer getPort(){
    return port;
  }
}
