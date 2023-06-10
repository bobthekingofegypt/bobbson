package org.bobstuff.bobbson.models;

public class DbPointerRaw {
  private String namespace;
  private byte[] objectId;

  public DbPointerRaw(String namespace, byte[] objectId) {
    this.namespace = namespace;
    this.objectId = objectId;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public byte[] getObjectId() {
    return objectId;
  }

  public void setObjectId(byte[] objectId) {
    this.objectId = objectId;
  }
}
