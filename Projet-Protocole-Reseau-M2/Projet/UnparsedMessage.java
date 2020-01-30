public class UnparsedMessage {

	private MessageType type;
	private String[] args;

	public UnparsedMessage(String message) {
		String relevantMessage = message.split("(\\*\\*\\*|\\+\\+\\+)")[0];
		String strType = relevantMessage.split(" ")[0];
		MessageType mt = null;
		for(MessageType mt2: MessageType.values()) {
			if(mt2.getType().equals(strType)) {
				mt = mt2;
				break;
			}
		}
		if(mt == null) {
			type = null;
			args = null;
			return;
		}

		type = mt;
		MessageArg[] messageArgsNames = mt.getArgsName();
		args = new String[messageArgsNames.length];
		if(args.length == 0){		//Nothing to parse
			return;
		}
		int delimiters = 0;
		int i = mt.getType().length();
		for(int j=0;j<args.length;j++) {
			i++;
			args[j] = "";
			if(messageArgsNames[j].getArgLength() == -1) {
				while(i<relevantMessage.length() && relevantMessage.charAt(i) != ' ') {
					args[j] += relevantMessage.charAt(i++);
				}
			} else if(messageArgsNames[j].getArgLength() == -2) {
				while(i<relevantMessage.length()) {
					args[j] += relevantMessage.charAt(i++);
				}
			} else if(messageArgsNames[j].getArgLength() == -3) {
				while(i<relevantMessage.length() &&  delimiters < 2) {
					int charcode = relevantMessage.charAt(i);
					if(charcode == 124){
						delimiters++;
					}
					else{
						delimiters = 0;
					}
					args[j] += relevantMessage.charAt(i++);
				}
				args[j] = args[j].substring(0,args[j].length() - 2);
				i--;
			}
			else{
				for(int k=0;k<messageArgsNames[j].getArgLength();k++) {
					args[j] += relevantMessage.charAt(i++);
				}
			}
		}
		for(i=0;i<args.length;i++) {
			args[i] = mt.getArgsName()[i].getReceiver().parse(args[i]);
		}
	}

	public MessageType getType() {
		return type;
	}
	public String[] getArgs() {
		return args;
	}

	public String toString() {
		String argsStr = String.join(" ", args),
		messageStr = type.getType() + " " + argsStr;
		return messageStr;
	}
}
