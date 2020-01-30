import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Utils {
	
	static String intToLE(String i, int bytes) {
		ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(Integer.parseInt(i));
        bb.flip();
        byte[] bytesArray = Arrays.copyOfRange(bb.array(), 0, bytes);
        return new String(bytesArray);
	}
	
	static String LEToInt(String le) {
		while(le.length()<4) le+=(char) 0;
		byte[] bytesArray = le.getBytes();
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(bytesArray);
		bb.flip();
		return Integer.toString(bb.getInt());
	}
	
	static String padLeft(String str, int n, char c) {
		while(str.length()<n) str=c+str;
		return str;
	}
	
	static String padRight(String str, int n, char c) {
		while(str.length()<n) str+=c;
		return str;
	}
	
	static String identity(String str) {
		return str;
	}

	static String unpadRight(String arg, char c) {
		while(arg.charAt(arg.length()-1) == c) {
			arg = arg.substring(0, arg.length()-1);
		}
		return arg;
	}
}
