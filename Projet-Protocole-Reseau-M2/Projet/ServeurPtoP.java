import java.net.*;
import java.io.*;
import java.security.* ;
import javax.net.ssl.*;
import java.util.HashMap;


public class ServeurPtoP implements Runnable{

  private SSLServerSocket server;
  private static SSLServerSocketFactory socketFactory;
  private static HashMap<String, Contact > contactables = new HashMap<String, Contact>();

  public static HashMap<String, Contact> getMapContact(){
    return contactables;
  }

  public ServeurPtoP(int port_client){
    try{
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(new FileInputStream("server.jks"), "password".toCharArray());

      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
      kmf.init(ks, "password".toCharArray());

      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
      tmf.init(ks);

      SSLContext sc = SSLContext.getInstance("TLS");
      TrustManager[] trustManagers = tmf.getTrustManagers();
      sc.init(kmf.getKeyManagers(), trustManagers, null);

      this.socketFactory = sc.getServerSocketFactory();
      
      this.server= (SSLServerSocket) this.socketFactory.createServerSocket(port_client);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void run(){
    try{
      while(true){
        SSLSocket socket = (SSLSocket) server.accept();
        ServicePtoP serv = new ServicePtoP(socket);
        Thread t=new Thread(serv);
        t.start();
      }
    }
    catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
