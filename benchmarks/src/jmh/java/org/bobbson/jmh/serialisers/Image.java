package org.bobbson.jmh.serialisers;

import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class Image implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  public String uri;
  public String title; // Can be null
  public int width;
  public int height;
  public Size size;

  public Image() {}

  public Image(String uri, String title, int width, int height, Size size) {
    this.height = height;
    this.title = title;
    this.uri = uri;
    this.width = width;
    this.size = size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Image image = (Image) o;

    if (height != image.height) return false;
    if (width != image.width) return false;
    if (size != image.size) return false;
    if (title != null ? !title.equals(image.title) : image.title != null) return false;
    if (uri != null ? !uri.equals(image.uri) : image.uri != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uri != null ? uri.hashCode() : 0;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + width;
    result = 31 * result + height;
    result = 31 * result + (size != null ? size.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Image{"
        + "uri='"
        + uri
        + '\''
        + ", title='"
        + title
        + '\''
        + ", width="
        + width
        + ", height="
        + height
        + ", size="
        + size
        + '}';
  }

  //    @JsonAttribute(index = 1)
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  //    @JsonAttribute(index = 2)
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  //    @JsonAttribute(index = 3)
  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  //    @JsonAttribute(index = 4)
  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  //    @JsonAttribute(index = 5)
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @GenerateBobBsonConverter
  public enum Size {
    SMALL,
    LARGE
  }
}
