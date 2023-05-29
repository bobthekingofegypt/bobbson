package org.bobstuff.bobbson.writer;

public class ArrayIntegerKeyUtils {
  static final byte[] DigitOnes =
      new byte[] {
        48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50,
        51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53,
        54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56,
        57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49,
        50, 51, 52, 53, 54, 55, 56, 57
      };
  static final byte[] DigitTens =
      new byte[] {
        48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 50, 50, 50,
        50, 50, 50, 50, 50, 50, 50, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52, 52, 52, 52, 52,
        52, 52, 52, 52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 54, 54, 54, 54,
        54, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57,
        57, 57, 57, 57, 57, 57, 57, 57
      };

  public static int getChars(int i, int index, byte[] buf) {
    int charPos = index;
    boolean negative = i < 0;
    if (!negative) {
      i = -i;
    }

    int q;
    int r;
    while (i <= -100) {
      q = i / 100;
      r = q * 100 - i;
      i = q;
      --charPos;
      buf[charPos] = DigitOnes[r];
      --charPos;
      buf[charPos] = DigitTens[r];
    }

    q = i / 10;
    r = q * 10 - i;
    --charPos;
    buf[charPos] = (byte) (48 + r);
    if (q < 0) {
      --charPos;
      buf[charPos] = (byte) (48 - q);
    }

    if (negative) {
      --charPos;
      buf[charPos] = 45;
    }

    return charPos;
  }

  public static int stringSize(int x) {
    int d = 1;
    if (x >= 0) {
      d = 0;
      x = -x;
    }

    int p = -10;

    for (int i = 1; i < 10; ++i) {
      if (x > p) {
        return i + d;
      }

      p = 10 * p;
    }

    return 10 + d;
  }
}
