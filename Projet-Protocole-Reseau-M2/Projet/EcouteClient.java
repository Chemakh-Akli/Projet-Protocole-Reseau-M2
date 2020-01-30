import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.* ;
import java.util.HashMap;


public class EcouteClient implements Runnable{

  private SSLSocket socket;
  private PrintWriter pw;
  private BufferedReader br;

  public EcouteClient( SSLSocket s, PrintWriter pw, BufferedReader br){
    try{
      this.socket = s;
      this.pw = pw;
      this.br = br;
    }catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void run(){
    try {
      boolean looping = true;
      while(looping) {
        int delimiters = 0;
        String message = "";
        while(delimiters < 3){
          int charCode = this.br.read();
          if(charCode == 42){
            delimiters++;
          }
          else{
            delimiters = 0;
          }
          message += ((char)charCode) + "";
        }
        looping = route(message);
      }
      this.br.close();
      this.pw.close();
      this.socket.close();
    }
    catch (IOException e) {
      System.out.println("\nEXCEPTION DANS ECOUTECLIENT");
    }
  }

  boolean route(String message) {
    UnparsedMessage unparsedMessage = new UnparsedMessage(message);
    MessageType messageType = unparsedMessage.getType();
    String[] args = unparsedMessage.getArgs();
    if(messageType == null || args == null) {
      return true;
    }
    switch(messageType) {

      case CALL_OPEN_SUCCESS:
      System.out.println("\nCommunication possible avec: " + args[0] +"\n");
      ServeurPtoP.getMapContact().put(args[0], new Contact(socket));
      break;

      case CALL_OPEN_ERROR:
      System.out.println("\nImpossible d'initialiser la communication");
      break;

      case SENT:
      break;

      case CALL_CLOSE_OK:
      System.out.println("\nCommunication fermée");
      ServeurPtoP.getMapContact().remove(args[0]);
      System.out.println("\n");
      return false;

      default:
      break;
    }
    return true;
  }
}
