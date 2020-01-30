import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client{
  public static void main(String[] args){
    new TCPClient(args[0], args[1]);
  }
}
