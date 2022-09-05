package org.bobstuff.bobbson.processor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BobMessager {
  private Messager messager;
  private boolean appendFile;
  private @Nullable OutputStream fos;

  public BobMessager(Messager messager, boolean appendFile) {
    this.messager = messager;
    this.appendFile = appendFile;
    if (appendFile) {
      try {
        this.fos = new FileOutputStream(Paths.get("/tmp/annotation_processor.log").toFile());
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  public void error(Throwable t) {
      try {
        for (var s : t.getStackTrace()) {
          if (fos != null) {
            fos.write(s.toString().getBytes(StandardCharsets.UTF_8));
            fos.write("\r\n".getBytes(StandardCharsets.UTF_8));
            fos.flush();
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public void error(String text) {
    if (fos != null) {
      try {
        fos.write(text.getBytes(StandardCharsets.UTF_8));
        fos.write("\r\n".getBytes(StandardCharsets.UTF_8));
        fos.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    messager.printMessage(Diagnostic.Kind.ERROR, text);
  }

  public void debug(String text) {
    if (fos != null) {
      try {
        fos.write(text.getBytes(StandardCharsets.UTF_8));
        fos.write("\r\n".getBytes(StandardCharsets.UTF_8));
        fos.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
