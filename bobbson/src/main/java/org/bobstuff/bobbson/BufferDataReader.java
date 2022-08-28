package org.bobstuff.bobbson;

import java.io.OutputStream;

public interface BufferDataReader {
  void reset();

  FieldName getFieldName();

  int getInt();

  byte get();

  long getLong();

  double getDouble();

  int readSizeValue(OutputStream stream);

  int readUntil(byte value);

  byte[] getBytes(int size);

  String readString(int size);

  void skip(int size);
}
