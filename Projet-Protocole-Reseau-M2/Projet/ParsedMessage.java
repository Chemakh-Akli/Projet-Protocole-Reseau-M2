import java.util.Arrays;

public class ParsedMessage {

	private MessageType type;
	private String[] args;

	public ParsedMessage(String message){
		String relevantMessage = message.split("\\*\\*\\*")[0];
		String[] messageParts = relevantMessage.split(" ");
		String strType = messageParts[0];
		MessageType mt = null;
		for(MessageType mt2: MessageType.values()) {
			if(mt2.getType().equals(strType)) {
				mt = mt2;
				break;
			}
		}

		if(mt == MessageType.CALL_OPEN){
			relevantMessage = relevantMessage + " " + TCPRouter.getMyPseudo() + " "
			+ TCPRouter.getMyAdress() + " " + String.valueOf(TCPRouter.getMyPort());
			messageParts = relevantMessage.split(" ");
			args = Arrays.copyOfRange(messageParts, 1, messageParts.length);
		}
		else{
			if(mt == MessageType.CALL_CLOSE){
				relevantMessage = strType + " " + TCPRouter.getMyPseudo() + " " + messageParts[1];
				messageParts = relevantMessage.split(" ");
				args = Arrays.copyOfRange(messageParts, 1, messageParts.length);
			}
			else{
				if(mt == MessageType.CALL){
					args = new String[3];
					relevantMessage = strType + " " + TCPRouter.getMyPseudo() + " " + String.join(" ", Arrays.copyOfRange(messageParts, 1, messageParts.length));
					messageParts = relevantMessage.split(" ");
					args[0] = messageParts[1];
					args[1] = messageParts[2];
					args[2] = String.join(" ", Arrays.copyOfRange(messageParts, 3, messageParts.length));
				}
				else{
					if(mt == MessageType.WELCOME || mt == MessageType.LISTE_DOMAINE || mt == MessageType.DESCRIPTION){
						String arg = String.join(" ", Arrays.copyOfRange(messageParts, 1, messageParts.length));
						args = new String[] {arg};
					}
					else{
						if(mt == MessageType.ADD_ANNONCE){
							String[] separation = relevantMessage.split("\\|\\|");
							String[] type_et_titre = separation[0].split(" ");
							String[] autres_champs =  separation[1].split(" ");
							args = new String[4];
							args[0] = String.join(" ", Arrays.copyOfRange(type_et_titre, 1, type_et_titre.length));
							args[1] = autres_champs[0];
							args[2] = autres_champs[1];
							if(autres_champs.length >= 3){
								args[3] = String.join(" ", Arrays.copyOfRange(autres_champs, 2, autres_champs.length));
							}
							else{
								if(autres_champs.length == 2 ){
									args[3] = "";
								}
							}
						}
						else{
							if(mt == MessageType.ANNONCE){
								args = new String[5];
								args[0] = messageParts[1];
								args[1] = messageParts[2];
								args[2] = messageParts[3];
								args[3] = String.join(" ", Arrays.copyOfRange(messageParts, 4, messageParts.length - 1)) ;
								args[4] = messageParts[messageParts.length-1];
							}
							else{
								args = Arrays.copyOfRange(messageParts, 1, messageParts.length);
							}
						}
					}
				}
			}
		}

		if(mt == null || args.length != mt.getArgsName().length){
			type = null;
			args = null;
			return;
		}

		type = mt;
		for(int i=0;i<args.length;i++) {
			args[i] = mt.getArgsName()[i].getSender().parse(args[i]);
		}
	}


	public MessageType getType() {
		return type;
	}

	public String[] getArgs() {
		return args;
	}

	public String toString() {
		if(args.length == 0){
			return type.getType();
		}
		String argsStr = "";
		if(type.getType().equals("ADD_ANNONCE")){
			argsStr = args[0] + "||" + args[1] + " " + args[2] + " " + args[3];
		}else{
			argsStr = String.join(" ", args);
		}
		String messageStr = type.getType() + " " + argsStr;
		return messageStr;
	}
}
