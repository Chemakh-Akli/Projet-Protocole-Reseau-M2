public enum Domaine{
  VOITURE("VOITURE"),
  MOTO("MOTO"),
  MUSIQUE("MUSIQUE"),
  CONSOLE("CONSOLE"),
  ORDINATEUR("ORDINATEUR"),
  ELECTROMENAGER("ELECTROMENAGER"),
  TELEPHONE("TELEPHONE"),
  TELEVISION("TELEVISION"),
  MEUBLE("MEUBLE"),
  LIT("LIT"),
  AUTRE("AUTRE");

  private final String domaine;

	Domaine(String domaine) {
		this.domaine = domaine;
	}

	public String getDomaineToString() {
		return domaine;
	}
}
