import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class SpecialChar {
  public static void main(String[] args) {
 
      Pattern pattern = Pattern.compile("[ a-zA-Z0-9]*");
 
      String str = "abc d";
      Matcher matcher = pattern.matcher(str);
 
      if (!matcher.matches()) {
           System.out.println("string '"+str + "' contains special character");
      } else {
           System.out.println("string '"+str + "' doesn't contains special character");
      }
 
  }
}
