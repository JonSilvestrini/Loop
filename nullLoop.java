import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Loop {
    public static String Read() {
 BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 String input = "null";

 try {
  input = br.readLine();
 }
 catch (IOException e) {
  System.out.println("deu ruim");
 }
 return input;
 
}
public static void delay(int millis) {try {Thread.sleep(millis);} catch (Exception e) {System.out.println("erro ao pausar a programação");}}}