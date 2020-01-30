import java.net.*;
import java.io.*;
import java.lang.*;
import java.security.* ;
import javax.net.ssl.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ServicePtoP implements Runnable{

  public SSLSocket socket;
  public PrintWriter pw;
  public BufferedReader br;

  public ServicePtoP(SSLSocket s){
    try {
      this.socket = s;
      this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void run(){
    try{
      awaitLoop();
      this.br.close();
      this.pw.close();
      this.socket.close();
    }
    catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  void sendMessage(String message) {
    ParsedMessage pm = new ParsedMessage(message);
    this.pw.write(pm.toString() + "***");
    this.pw.flush();

  }

  void awaitLoop(){
    try {
      boolean looping = true;
      while(looping) {
        int delimiters = 0;
        String message = "";
        while(delimiters < 3) {
          int charCode = this.br.read();
          if(charCode == 42){
            delimiters++;
          }
          else{
            delimiters = 0;
          }
          message += ((char)charCode)+ "";
        }
        looping = route(message);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  boolean route(String message) {
    if(message.charAt(0) == '\n'){message = message.substring(1);}
    UnparsedMessage pm = new UnparsedMessage(message);
    MessageType messageType = pm.getType();
    String[] args = pm.getArgs();
    if(messageType == null || args == null) {
      System.out.println("Commande invalide détectée\n");
      sendMessage("INVALID");
      return true;
    }
    Contact contact;
    switch(messageType){
      case CALL_OPEN:
      if(!ServeurPtoP.getMapContact().containsKey(args[2]) ||
       (!ServeurPtoP.getMapContact().get(args[2]).getSocket().getRemoteSocketAddress().toString().split(":")[0].substring(1).equals(args[3]) ||
        ServeurPtoP.getMapContact().get(args[2]).getSocket().getPort() != Integer.parseInt(args[4]))) {
        System.out.println("L'utilisateur " + args[2] + " souhaite vous parler\n");
          try{
            SSLSocket soc = (SSLSocket) TCPRouter.getSSLSocketFactory().createSocket(args[3], Integer.parseInt(args[4]));
            contact = new Contact(soc);
            soc.startHandshake();
            ServeurPtoP.getMapContact().put(args[2], contact);
            EcouteClient listen = new EcouteClient(soc, contact.getPW(), contact.getBR());
            Thread lis = new Thread(listen);
            lis.start();
          }
          catch(Exception e){
            e.printStackTrace();
          }
          this.sendMessage("CALL_OPEN_SUCCESS " + TCPRouter.getMyPseudo());
        }

      else{
        this.sendMessage("CALL_OPEN_ERROR ");
      }
      break;

      case CALL:
      System.out.println("\nMessage de " + args[0] + " : " + args[2]);
      sendMessage("SENT");
      break;

      case CALL_CLOSE:
      try{
        ServeurPtoP.getMapContact().get(args[0]).getPW().print("CALL_CLOSE " + args[1] + " " + args[0]+ "***");
        ServeurPtoP.getMapContact().get(args[0]).getPW().flush();
      }catch(Exception e){}
      sendMessage("CALL_CLOSE_OK " + args[1]);
      return false;
    }
    return true;
  }
}
