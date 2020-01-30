public class Annonce{

  private String titre;
  private Domaine domaine;
  private double prix;
  private String description;
  private int id;
  public static int compteur = 1;

  public Annonce(String titre, double prix, String description, Domaine domaine){
    this.prix = prix;
    this.titre = titre;
    this.description = description;
    this.domaine = domaine;
    this.id = compteur;
    compteur++;
  }

  public void setPrix(double prix){
      this.prix = prix;
  }

  public void setDescription(String description){
    this.description = description;
  }

  public void setTitre(String titre){
    this.titre = titre;
  }

  public void setDomaine(Domaine domaine){
    this.domaine = domaine;
  }

  public double getPrix(){
    return this.prix;
  }

  public String getDescription(){
    return this.description;
  }

  public String getTitre(){
    return this.titre;
  }

  public Domaine getDomaine(){
    return this.domaine;
  }

  public int getId(){
    return this.id;
  }
}
