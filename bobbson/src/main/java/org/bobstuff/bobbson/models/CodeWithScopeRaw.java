package org.bobstuff.bobbson.models;

public class CodeWithScopeRaw {
  private String code;
  private byte[] scope;

  public CodeWithScopeRaw(String code, byte[] scope) {
    this.code = code;
    this.scope = scope;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public byte[] getScope() {
    return scope;
  }

  public void setScope(byte[] scope) {
    this.scope = scope;
  }
}
