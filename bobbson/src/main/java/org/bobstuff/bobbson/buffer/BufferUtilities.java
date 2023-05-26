package org.bobstuff.bobbson.buffer;

public class BufferUtilities {
  /**
   * Write a utf8 encoded string to the given byte array.
   *
   * <p>Using string.getBytes() causes an allocation which we don't want.
   *
   * <p>This function does no bounds checking, it will cause an array index out of bounds if you try
   * to write a string larger than the buffer.
   *
   * @param value string to be writen to byte array
   * @param buf byte array to place the results
   * @param tail position in buf array to start writing data
   * @return index tail adjusted for number of bytes writen
   */
  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public static int writeStringToByteArray(final String value, final byte[] buf, int tail) {
    var i = tail;
    var length = value.length();

    for (int sIndex = 0, sLength = length; sIndex < sLength; sIndex++) {
      char c = value.charAt(sIndex);
      if (c < '\u0080') {
        buf[i++] = (byte) c;
      } else if (c < '\u0800') {
        buf[i++] = (byte) (192 | c >>> 6);
        buf[i++] = (byte) (128 | c & 63);
      } else if (c < '\ud800' || c > '\udfff') {
        buf[i++] = (byte) (224 | c >>> 12);
        buf[i++] = (byte) (128 | c >>> 6 & 63);
        buf[i++] = (byte) (128 | c & 63);
      } else {
        int cp = 0;
        sIndex += 1;
        if (sIndex < sLength) cp = Character.toCodePoint(c, value.charAt(sIndex));
        if ((cp >= 1 << 16) && (cp < 1 << 21)) {
          buf[i++] = (byte) (240 | cp >>> 18);
          buf[i++] = (byte) (128 | cp >>> 12 & 63);
          buf[i++] = (byte) (128 | cp >>> 6 & 63);
          buf[i++] = (byte) (128 | cp & 63);
        } else {
          buf[i++] = (byte) '?';
        }
      }
    }
    return i;
  }
}
