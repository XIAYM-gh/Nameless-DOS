package cn.xiaym.utils;

import java.util.*;

public class RandomIDGenerator {
  private static String idStr = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
  private static ArrayList<String> idList = new ArrayList<>();

  public static String generate() {
    return generate(8);
  }

  public static String generate(int len) {
    if(len > 256) len = 256;

    StringBuilder target = new StringBuilder();
    idList = new ArrayList<>();
    int i;

    for(i=0; i <= idStr.length() - 1; i++) {
      idList.add(String.valueOf(idStr.charAt(i)));
    }

    Random r = new Random();

    for(i=0; i < len; i++) {
      target.append(idList.get(r.nextInt(idList.size())));
    }

    return target.toString();
  }
}
