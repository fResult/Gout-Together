package dev.fResult.goutTogether.common.utils;

public class StringUtil {
  public static String pluralize(String word) {
    if (word == null || word.isEmpty()) {
      return word;
    }
    if (word.endsWith("y")) {
      return word.substring(0, word.length() - 1) + "ies";
    } else if (word.endsWith("s")
        || word.endsWith("sh")
        || word.endsWith("ch")
        || word.endsWith("x")
        || word.endsWith("z")) {
      return word + "es";
    } else {
      return word + "s";
    }
  }
}
