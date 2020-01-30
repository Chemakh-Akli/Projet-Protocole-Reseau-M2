import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.* ;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class TCPRouter {

  private int nb_annonce;
  private Scanner scanner;

  private BufferedReader br;
  private PrintWriter pw;
  private static Socket socket;

  private static SSLSocketFactory socketFactory;
  private PrintWriter pw_client;
  private BufferedReader br_client;
  private SSLSocket socket_client;

  private static String  my_pseudo;
  private static String  adresse;
  private static Integer port;

  private ServeurPtoP serveurPTOP;


  public TCPRouter(Socket socket) {
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

      this.socketFactory = sc.getSocketFactory();
    }catch(Exception e){
      e.printStackTrace();
    }

    try{
      this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.scanner = new Scanner(System.in);
      this.socket = socket;
    }catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Integer getMyPort(){
    return port;
  }

  public static String getMyPseudo(){
    return my_pseudo;
  }

  public static String getMyAdress(){
    return adresse;
  }

  public static SSLSocketFactory getSSLSocketFactory(){
    return socketFactory;
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
      this.scanner.close();
      this.br.close();
      this.pw.close();
      this.socket.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void scanNext(){
    System.out.print("\n");
    String message = "";
    String messageAux = "";
    try {
      messageAux = this.scanner.nextLine();
      message = messageAux + "***";
      if(messageAux.trim().length() == 0){
        scanNext();
        return;
      }
      ParsedMessage parsedMessage = new ParsedMessage(message);
      if(parsedMessage.getType() == null || parsedMessage.getArgs() == null) {
        this.pw.write(message);
        this.pw.flush();
        return;
      }
      if(parsedMessage.getType() == MessageType.CALL_OPEN &&  this.my_pseudo != null){
        try{
          this.socket_client = (SSLSocket)this.getSSLSocketFactory().createSocket(parsedMessage.getArgs()[0], Integer.parseInt(parsedMessage.getArgs()[1]));
          this.socket_client.startHandshake();

          this.br_client = new BufferedReader(new InputStreamReader(this.socket_client.getInputStream()));
          this.pw_client = new PrintWriter(new OutputStreamWriter(this.socket_client.getOutputStream()));
          this.pw_client.println(parsedMessage.toString() + "***");
          this.pw_client.flush();

          if(pw_client.checkError()){System.out.println("SSLSocketClient:  java.io.PrintWriter error");}

          EcouteClient listen = new EcouteClient(socket_client, pw_client, br_client);
          Thread lis = new Thread(listen);
          lis.start();
          this.scanNext();
          return;
        }
        catch(Exception e){
          System.out.println("\nErreur de port et/ou d'adresse");
          scanNext();
          return;
        }
      }
      if( (parsedMessage.getType() == MessageType.CALL || parsedMessage.getType() == MessageType.CALL_CLOSE)  && this.my_pseudo != null ){
        try{
          if(ServeurPtoP.getMapContact().containsKey(parsedMessage.getArgs()[1])){
            ServeurPtoP.getMapContact().get(parsedMessage.getArgs()[1]).getPW().println(parsedMessage.toString() + "***");
            ServeurPtoP.getMapContact().get(parsedMessage.getArgs()[1]).getPW().flush();
            scanNext();
            return;
          }
          else{
            System.out.println("\nVous n'avez pas ouvert la communication avec cette personne");
            this.scanNext();
            return;
          }
        }
        catch(Exception e){
          e.printStackTrace();
          scanNext();
          return;
        }
      }
      String strParsedMessage = parsedMessage.toString();
      this.pw.println(strParsedMessage + "***");
      this.pw.flush();
    }
    catch(IndexOutOfBoundsException e){
      e.printStackTrace();
      return;
    }
  }

  public void callCloseAfterDisconnect(){
    Set set = ServeurPtoP.getMapContact().entrySet();
    Iterator iterator = set.iterator();
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      ServeurPtoP.getMapContact().get((String)mentry.getKey()).getPW().print("CALL_CLOSE " + getMyPseudo() + " " + (String)mentry.getKey() + "***");
      ServeurPtoP.getMapContact().get((String)mentry.getKey()).getPW().flush();
      System.out.println(getMyPseudo() + " " + (String)mentry.getKey() );
    }
  }

  boolean route(String message) {
    UnparsedMessage unparsedMessage = new UnparsedMessage(message);
    MessageType messageType = unparsedMessage.getType();
    String[] args = unparsedMessage.getArgs();
    if(messageType == null || args == null) {
      this.scanNext();
      return true;
    }
    switch(messageType) {
      case WELCOME:
      System.out.println(unparsedMessage.toString());
      this.scanNext();
      break;

      case LISTE_DOMAINE:
      System.out.println(unparsedMessage.toString());
      this.scanNext();
      break;

      case INVALID:
      System.out.println("\nCommande invalide");
      this.scanNext();
      break;

      case NEW_SUCCESS:
      System.out.println("\nInscription réussie");
      this.scanNext();
      break;

      case NEW_ERROR:
      System.out.println("\nEchec de l'inscription");
      this.scanNext();
      break;

      case CONNECT_SUCCESS:
      this.my_pseudo = args[0];
      this.adresse = args[1];
      this.port = Integer.parseInt(args[2]);
      this.serveurPTOP = new ServeurPtoP(this.port);
      Thread t=new Thread(serveurPTOP);
      t.start();
      System.out.println("\nConnexion réussie");
      this.scanNext();
      break;

      case CONNECT_ERROR:
      System.out.println("\nEchec de la connexion");
      this.scanNext();
      break;

      case DISCONNECT_SUCCESS:
      this.callCloseAfterDisconnect();
      this.my_pseudo = null;
      this.port = null;
      System.out.println("\nDéconnexion réussi");
      this.scanNext();
      break;

      case DISCONNECT_ERROR:
      System.out.println("\nEchec de la déconnexion");
      this.scanNext();
      break;

      case DELETE_SUCCESS:
      System.out.println("\nSuppression réussie");
      this.scanNext();
      break;

      case DELETE_ERROR:
      System.out.println("\nEchec de la suppression");
      this.scanNext();
      break;

      case ADD_SUCCESS:
      System.out.println("\nInsertion réussie");
      this.scanNext();
      break;

      case ADD_ERROR:
      System.out.println("\nEchec de l'insertion");
      this.scanNext();
      break;

      case ANNONCES:
      System.out.println(unparsedMessage.toString());
      this.nb_annonce = Integer.parseInt(args[0]);
      if(this.nb_annonce == 0) this.scanNext();
      break;

      case ANNONCE:
      System.out.println(unparsedMessage.toString());
      if(this.nb_annonce > 1){
        this.nb_annonce--;
      }
      else{
        this.scanNext();
      }
      break;

      case DOMAINE_NO_EXIST:
      System.out.println("\nCe domaine n'existe pas");
      this.scanNext();
      break;

      case CLIENT_NO_EXIST:
      System.out.println("\nCe client n'existe pas");
      this.scanNext();
      break;

      case PRICE_ERROR:
      System.out.println("\nCeci n'est pas un prix");
      this.scanNext();
      break;

      case DESCRIPTION_VIDE:
      System.out.println("\nLa description de cette annonce est vide");
      this.scanNext();
      break;

      case ANNONCE_NOT_EXIST:
      System.out.println("\nCette annonce n'existe pas");
      this.scanNext();
      break;

      case DESCRIPTION:
      System.out.println("\n" + args[0]);
      this.scanNext();
      break;

      case IT_IS:
      System.out.println("\nAdresse : " + args[0] + "\nPort : " + args[1]);
      this.scanNext();
      break;

      case NOPE:
      System.out.println("\nVous n'avez pas le statut necessaire pour cette commande");
      this.scanNext();
      break;

      case BYE:
      System.out.println("\nAu revoir !");
      this.callCloseAfterDisconnect();
      return false;

      default:
      this.scanNext();
      break;
    }
    return true;
  }
}
