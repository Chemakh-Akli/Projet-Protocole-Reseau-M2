import java.util.LinkedList;

public class Utilisateur{

  private String pseudo;
  private String mdp;
  private LinkedList<Annonce> annonces;
  private Integer port;
  private String adrIP;

  public Utilisateur( String pseudo, String mdp){
    this.pseudo = pseudo;
    this.mdp = mdp;
    this.annonces = new LinkedList<Annonce>();
    this.port = null;
    this.adrIP = null;
  }

  public void setPseudo(String pseudo){
      this.pseudo = pseudo;
  }

  public void setMotDePasse(String mdp){
      this.mdp = mdp;
  }

  public void setIP(String adrIP){
      this.adrIP = adrIP;
  }

  public void setPort(Integer port){
      this.port = port;
  }

  public String getPseudo(){
    return this.pseudo;

  }
  public String getMotDePasse(){
    return this.mdp;
  }

  public Integer getPort(){
    return this.port;
  }

  public String getIP(){
    return this.adrIP;
  }

  public LinkedList<Annonce> getAnnonces(){
    return this.annonces;
  }
}
