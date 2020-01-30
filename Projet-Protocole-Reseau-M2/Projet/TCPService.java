import java.net.*;
import java.io.*;
import java.lang.*;
import java.security.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class TCPService implements Runnable{

  public Socket socket;
  public PrintWriter pw;
  public BufferedReader br;
  public Utilisateur utilisateur;

  public TCPService(Socket s){
    try {
      this.socket = s;
      this.utilisateur = null;
      br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    }
    catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }



  public void run(){
    try{
      sendControl();
      awaitLoop();
      br.close();
      pw.close();
      socket.close();
    }
    catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  void sendControl(){
    sendMessage(
    "WELCOME \nPour poster une annonce: ADD_ANNONCE domaine prix description" + "\n" +
    "Pour supprimer une annonce: DELETE id" + "\n" +
    "Pour voir toutes les annonces disponibles: CHECK_ALL_ANNONCES" +"\n" +
    "Pour voir toutes les annonces d'un autre client: CHECK_ANNONCES_CLIENT id" + "\n" +
    "Pour voir toutes les annonces d'un domaine particulier: CHECK_ANNONCES_CLIENT domaine" + "\n" +
    "Pour voir toutes les annonces à un prix inférieur ou égal à un nombre donné: CHECK_ANNONCES_PRICE prix" + "\n" +
    "Pour réafficher ce message: HELP" +"\n" +
    "Pour voir la liste des domaines disponibles: INFO_DOMAINE" + "\n" +
    "Pour quitter le serveur: QUIT" +"\n" +
    "Avant d'utiliser une de ses commandes, vous devez vous inscrire ou vous connecter a l'aide d'une des la commande suivante" + "\n" +
    "Pour vous inscrire: NEW nom prenom pseudo MotDePasse" + "\n" +
    "Pour vous connecter: CONNECT pseudo MotDePasse\n"
    );
  }

  void sendListeDomaine(){
    sendMessage(
    "LISTE_DOMAINE \nVoici la liste des domaines avec lesquels vous DEVEZ ajouter vos annonces" + "\n" +
    "MOTO" + "\n" +
    "VOITURE" +"\n" +
    "ORDINATEUR" + "\n" +
    "CONSOLE" +"\n" +
    "ELECTROMENAGER" +"\n" +
    "MUSIQUE" + "\n" +
    "MEUBLE" + "\n" +
    "LIT" + "\n" +
    "TELEPHONE" + "\n" +
    "TELEVISION" + "\n" +
    "AUTRE\n"
    );
  }


  void sendMessage(String message) {
    ParsedMessage pm = new ParsedMessage(message);
    pw.print(pm.toString() + "***");
    pw.flush();
  }

  void awaitLoop(){
    try {
      while (true) {
        int delimiters = 0;
        String message = "";
        while (delimiters < 3) {
          int charCode = br.read();
          if (charCode == 42){
            delimiters++;
          }
          else{
            delimiters = 0;
          }
          message += ((char)charCode)+ "";
        }
        routeServer(message);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  void routeServer(String message) {
    //System.out.println("<<"+ message +">>");
    if(message.charAt(0) == '\n'){message = message.substring(1);}
    UnparsedMessage pm = new UnparsedMessage(message);
    MessageType messageType = pm.getType();
    String[] args = pm.getArgs();
    if(messageType == null || args == null) {
      System.out.println("Commande invalide détectée\n");
      sendMessage("INVALID");
      return;
    }
    String pseudo;
    Utilisateur user;
    String description;
    switch(messageType){

      case HELP:
      sendControl();
      break;

      case INFO_DOMAINE:
      sendListeDomaine();
      break;

      case NEW:
      if(this.utilisateur != null){
        System.out.println("Deja connecté\n");
        sendMessage("NEW_ERROR");
      }
      else{
        if(this.pseudoNonExistant(args[0])){
          TCPServeur.getMapUtilisateur().put(args[0], new Utilisateur(args[0],cryptedSaltMD5(args[1])));
          System.out.println("Inscription réussie : " +
          args[0] + " " + args[1] + "\n");
          sendMessage("NEW_SUCCESS");
        }
        else{
          sendMessage("NEW_ERROR");
        }
      }
      break;

      case CONNECT:
      if(this.utilisateur != null){
        System.out.println("Deja connecté\n");
        sendMessage("CONNECT_ERROR");
      }
      else{
        user = checkPassword(args[0],args[1]);
        if(user != null && portLibre(args[2])){
          this.utilisateur = user;
          this.utilisateur.setIP(this.socket.getRemoteSocketAddress().toString().split(":")[0].substring(1));
          this.utilisateur.setPort(Integer.parseInt(args[2]));
          System.out.println("Connexion : " + this.utilisateur.getPseudo() + "\n");
          sendMessage("CONNECT_SUCCESS " + this.utilisateur.getPseudo() + " "
          + this.utilisateur.getIP() + " "
          + String.valueOf(this.utilisateur.getPort())
          );
        }
        else{
          sendMessage("CONNECT_ERROR");
        }
      }
      break;

      case DISCONNECT:
      if(this.utilisateur == null){
        System.out.println("Echec de la deconnexion (Pas connecté)\n");
        sendMessage("DISCONNECT_ERROR");
      }
      else{
        System.out.println("Deconnexion\n");
        this.utilisateur.setIP(null);
        this.utilisateur.setPort(null);
        this.utilisateur = null;
        sendMessage("DISCONNECT_SUCCESS");
      }
      break;

      case ADD_ANNONCE:
      if(this.utilisateur != null){
        if(annonceValide( args[0], args[1], args[2], args[3]) ){
          System.out.println("L'insertion de la nouvelle annonce a réussie\n");
          sendMessage("ADD_SUCCESS");
        }
        else{
          System.out.println("L'insertion de la nouvelle annonce a échoué (Annonce invalide)\n");
          sendMessage("ADD_ERROR");
        }
      }
      else{
        System.out.println("L'insertion de la nouvelle annonce a échoué (Non connecté)\n");
        sendMessage("NOPE");
      }
      break;

      case DELETE_ANNONCE:
      if(this.utilisateur != null){
        if(containAnnonce(args[0])){
          System.out.println("La suppression de l'annonce a réussie\n");
          sendMessage("DELETE_SUCCESS");
        }
        else{
          System.out.println("La suppression de l'annonce a échoué (Annonce introuvable)\n");
          sendMessage("ANNONCE_NOT_EXIST");
        }
      }
      else{
        System.out.println("La suppression de l'annonce a échoué (Pas connecté)\n");
        sendMessage("NOPE");
      }
      break;

      case CHECK_ALL_ANNONCES:
      if(this.utilisateur != null){
        System.out.println("Demande de toutes les annonces\n");
        sendAllAnnonces();
      }else{
        System.out.println("Pas connecté\n");
        sendMessage("NOPE");
      }
      break;

      case CHECK_ANNONCES_CLIENT:
      if(this.utilisateur != null){
        if(!pseudoNonExistant(args[0])){
          System.out.println("Demande de toutes les annonces de " + args[0] + "\n");
          sendAnnoncesClient(args[0]);
        }
        else{
          sendMessage("CLIENT_NO_EXIST");
        }
      }else{
        System.out.println("Pas connecté\n");
        sendMessage("NOPE");
      }
      break;

      case CHECK_ANNONCES_DOMAINE:
      if(this.utilisateur != null){
        if(domaineValide(args[0])){
          System.out.println("Demande de toutes les annonces du domaine " + args[0] + "\n");
          sendAnnoncesDomaine(args[0]);
        }
        else{
          sendMessage("DOMAINE_NO_EXIST");
        }
      }else{
        sendMessage("NOPE");
      }
      break;

      case CHECK_ANNONCES_PRICE:
      if(this.utilisateur != null){
        if(prixValide(args[0])){
          System.out.println("Demande de toutes les annonces a un prix inférieur ou egal a " + args[0] + "\n");
          double d = Double.parseDouble(args[0]);
          sendAnnoncesPrix(d);
        }
        else{
          sendMessage("PRICE_ERROR");
        }
      }else{
        sendMessage("NOPE");
      }
      break;

      case CHECK_DESCRIPTION:
      if(this.utilisateur != null){
        description = getDescriptionAnnonceById(args[0]);
        if(description != null){
          System.out.println("Demande d'affichage de la description d'identifiant " + args[0] + "\n");
          if(description.length() > 0){
            sendMessage("DESCRIPTION " + description);
          }
          else{
            sendMessage("DESCRIPTION_VIDE");
          }
        }
        else{
          System.out.println("Annonce introuvable\n");
          sendMessage("ANNONCE_NOT_EXIST");
        }
      }
      else{
        System.out.println("Pas connecté\n");
        sendMessage("NOPE");
      }
      break;

      case WHOIS:
      if(this.utilisateur != null){
        if(!pseudoNonExistant(args[0])){
          System.out.println("Demande d'adresse:port detectée\n");
          sendMessage("IT_IS " + TCPServeur.getMapUtilisateur().get(args[0]).getIP() + " "
          + String.valueOf(TCPServeur.getMapUtilisateur().get(args[0]).getPort()));
        }
        else{
          sendMessage("CLIENT_NO_EXIST");
        }
      }
      else{
        sendMessage("NOPE");
      }
      break;

      case QUIT:
      if(this.utilisateur != null){
        this.utilisateur.setIP(null);
        this.utilisateur.setPort(null);
      }
      sendMessage("BYE");
      break;
    }
  }


  public void sendAllAnnonces(){
    sendMessage("ANNONCES " + String.valueOf(this.nbAnnoncesTotal()));
    Set set = TCPServeur.getMapUtilisateur().entrySet();
    Iterator iterator = set.iterator();
    Utilisateur a;
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      a = (Utilisateur)mentry.getValue();
      for(int i = 0 ; i < a.getAnnonces().size() ; i++){
        sendMessage("ANNONCE " + String.valueOf(a.getAnnonces().get(i).getId()) + " " +
        a.getAnnonces().get(i).getDomaine().getDomaineToString() + " " +
        String.valueOf(a.getAnnonces().get(i).getPrix()) + " " +
        a.getAnnonces().get(i).getTitre() + " " + a.getPseudo());
      }
    }
  }

  public static String cryptedSaltMD5(String passwordToHash){
    StringBuffer stringBuffer = new StringBuffer();
    try{
      String machin=passwordToHash+TCPServeur.getSalt();
      MessageDigest messageDigest;
      messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(machin.getBytes());
      byte[] messageDigestMD5 = messageDigest.digest();
      for (byte bytes : messageDigestMD5) {
        stringBuffer.append(String.format("%02x", bytes & 0xff));
      }
    } catch (NoSuchAlgorithmException  e) {
      e.printStackTrace();
    }
    return stringBuffer.toString();
  }


  public void sendAnnoncesClient(String pseudo){
    Utilisateur a = TCPServeur.getMapUtilisateur().get(pseudo);
    sendMessage("ANNONCES " + String.valueOf(a.getAnnonces().size()));
    for(int i = 0; i < a.getAnnonces().size() ; i++){
      sendMessage("ANNONCE " + String.valueOf(a.getAnnonces().get(i).getId()) + " " +
      a.getAnnonces().get(i).getDomaine().getDomaineToString() + " " +
      String.valueOf(a.getAnnonces().get(i).getPrix()) + " " +
      a.getAnnonces().get(i).getDescription() + " " + pseudo);
    }
  }

  public void sendAnnoncesDomaine(String domaine){
    sendMessage("ANNONCES " + String.valueOf(this.nbAnnoncesTotalDomaine(domaine)));
    Set set = TCPServeur.getMapUtilisateur().entrySet();
    Iterator iterator = set.iterator();
    Utilisateur a;
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      a = (Utilisateur)mentry.getValue();
      for(int i = 0 ; i < a.getAnnonces().size() ; i++){
        if(a.getAnnonces().get(i).getDomaine().getDomaineToString().equals(domaine)) {
          sendMessage("ANNONCE " + String.valueOf(a.getAnnonces().get(i).getId()) + " " +
          a.getAnnonces().get(i).getDomaine().getDomaineToString() + " " +
          String.valueOf(a.getAnnonces().get(i).getPrix()) + " " +
          a.getAnnonces().get(i).getDescription() + " " + a.getPseudo());
        }
      }
    }
  }

  public void sendAnnoncesPrix(double prix){
    sendMessage("ANNONCES " + String.valueOf(this.nbAnnoncesTotalPrix(prix)));
    Set set = TCPServeur.getMapUtilisateur().entrySet();
    Iterator iterator = set.iterator();
    Utilisateur a;
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      a = (Utilisateur)mentry.getValue();
      for(int i = 0 ; i < a.getAnnonces().size() ; i++){
        if(a.getAnnonces().get(i).getPrix() <= prix) {
          sendMessage("ANNONCE " + String.valueOf(a.getAnnonces().get(i).getId()) + " " +
          a.getAnnonces().get(i).getDomaine().getDomaineToString() + " " +
          String.valueOf(a.getAnnonces().get(i).getPrix()) + " " +
          a.getAnnonces().get(i).getDescription() + " " + a.getPseudo());
        }
      }
    }
  }

  public boolean pseudoNonExistant(String strpseudo){
    return (!TCPServeur.getMapUtilisateur().containsKey(strpseudo));
  }


  public Utilisateur checkPassword(String strpseudo, String strmdp){
    if(pseudoNonExistant(strpseudo)){
      System.out.println("Pseudo null");
      return null;
    }
    else{
      if(TCPServeur.getMapUtilisateur().get(strpseudo).getMotDePasse().equals(cryptedSaltMD5(strmdp))){
        return TCPServeur.getMapUtilisateur().get(strpseudo);
      }
      else{
        return null;
      }
    }
  }


  public boolean portLibre(String p) {
    Integer port;
    try{
      port = Integer.parseInt(p);
    } catch (Exception e) {
      return false;
    }
    try{
      Socket s = new Socket(this.socket.getRemoteSocketAddress().toString().split(":")[0].substring(1) , port );
      s.close();
    } catch (Exception e) {
      return true;
    }
    return false;
  }


  public boolean prixValide(String prix){
    double p;
    try{
      p = Double.parseDouble(prix);
    }catch(Exception e){
      return false;
    }
    return true;
  }

  public boolean domaineValide(String domaine){
    for(Domaine d2: Domaine.values()) {
      if(d2.getDomaineToString().equals(domaine)) {
        return true;
      }
    }
    return false;
  }

  public boolean titreValide(String titre){
    return (titre.length() >= 4 && titre.length() <= 30);
  }

  public boolean annonceValide(String titre, String domaine, String prix, String description){
    if(titreValide(titre) && domaineValide(domaine) && prixValide(prix)){
      TCPServeur.getMapUtilisateur().get(this.utilisateur.getPseudo()).getAnnonces().add
      (new Annonce(titre, Double.parseDouble(prix), description, stringToDomaine(domaine)));
      return true;
    }
    else{
      return false;
    }
  }

  public Domaine stringToDomaine(String domaine){
    for(Domaine d2: Domaine.values()) {
      if(d2.getDomaineToString().equals(domaine)) {
        return d2;
      }
    }
    return null;
  }

  public boolean containAnnonce(String strId){
    int id;
    try{
      id = Integer.parseInt(strId);
    }catch(Exception e){
      return false;
    }
    for(int i = 0; i < this.utilisateur.getAnnonces().size(); i++){
      if(this.utilisateur.getAnnonces().get(i).getId() == id){
        this.utilisateur.getAnnonces().remove(i);
        return true;
      }
    }
    return false;
  }

  public String getDescriptionAnnonceById(String strId){
    int id;
    try{
      id = Integer.parseInt(strId);
    }catch(Exception e){
      return null;
    }
    Set set = TCPServeur.getMapUtilisateur().entrySet();
    Iterator iterator = set.iterator();
    Utilisateur a;
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      a = (Utilisateur)mentry.getValue();
      for(int i = 0 ; i < a.getAnnonces().size() ; i++){
        if(a.getAnnonces().get(i).getId() == id){
          return a.getAnnonces().get(i).getDescription();
        }
      }
    }
    return null;
  }

  public int nbAnnoncesTotal(){
    Set set = TCPServeur.getMapUtilisateur().entrySet();
    Iterator iterator = set.iterator();
    Utilisateur a;
    int compteur = 0;
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      a = (Utilisateur)mentry.getValue();
      compteur += a.getAnnonces().size();
    }
    return compteur;
  }

  public int nbAnnoncesTotalDomaine(String domaine){
    Set set = TCPServeur.getMapUtilisateur().entrySet();
    Iterator iterator = set.iterator();
    int compteur = 0;
    Utilisateur a;
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      a = (Utilisateur)mentry.getValue();
      for(int i = 0; i < a.getAnnonces().size() ; i++){
        if(a.getAnnonces().get(i).getDomaine().getDomaineToString().equals(domaine)){
          compteur++;
        }
      }
    }
    return compteur;
  }

  public int nbAnnoncesTotalPrix(double prix){
    Set set = TCPServeur.getMapUtilisateur().entrySet();
    Iterator iterator = set.iterator();
    int compteur = 0;
    Utilisateur a;
    while(iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry)iterator.next();
      a = (Utilisateur)mentry.getValue();
      for(int i = 0; i < a.getAnnonces().size() ; i++){
        if(a.getAnnonces().get(i).getPrix() <= prix ){
          compteur++;
        }
      }
    }
    return compteur;
  }

}
