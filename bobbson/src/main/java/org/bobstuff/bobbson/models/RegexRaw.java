package org.bobstuff.bobbson.models;

public class RegexRaw {
  private String regex;
  private String options;

  public RegexRaw(String regex, String options) {
    this.regex = regex;
    this.options = options;
  }

  public String getRegex() {
    return regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }
}
