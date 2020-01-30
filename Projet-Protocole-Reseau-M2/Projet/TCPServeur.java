import java.net.*;
import java.io.*;
import java.util.HashMap;

public class TCPServeur{

  public static HashMap<String, Utilisateur> utilisateurs = new HashMap<String, Utilisateur>();;
  private static final String salt = "é*♫5Ma   ■";

  public static HashMap<String, Utilisateur> getMapUtilisateur(){
    return utilisateurs;
  }

  public static String getSalt(){
    return salt;
  }

  public TCPServeur(){
    try{
      ServerSocket server=new ServerSocket(1027);
      while(true){
        Socket socket=server.accept();
        TCPService serv = new TCPService(socket);
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
