
public enum MessageArg {
	MESS(-2, Utils::identity, Utils::identity),
	ID(-1, Utils::identity, Utils::identity),
	NB(-1, Utils::identity, Utils::identity),
	PORT(-1, Utils::identity, Utils::identity),
	MDP(-1, Utils::identity, Utils::identity),
	PSEUDO(-1, Utils::identity, Utils::identity),
	PRENOM(-1, Utils::identity, Utils::identity),
	ADR(-1, Utils::identity, Utils::identity),
	NOM(-1, Utils::identity, Utils::identity),
	DOMAINE(-1, Utils::identity, Utils::identity),
	PRIX(-1, Utils::identity, Utils::identity),
	DESCRIPTION(-2, Utils::identity, Utils::identity),
	TITRE_NOSPLIT(-2, Utils::identity, Utils::identity),
	TITRE(-3, Utils::identity, Utils::identity);


	private final int argLength;
	private final ArgParser sender, receiver;

	MessageArg(int argLength, ArgParser sender, ArgParser receiver) {
		this.argLength = argLength;
		this.sender = sender;
		this.receiver = receiver;
	}

	public int getArgLength() {
		return argLength;
	}

	public ArgParser getSender() {
		return sender;
	}

	public ArgParser getReceiver() {
		return receiver;
	}
}
