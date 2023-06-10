package org.bobbson.jmh.serialisers;

import java.util.List;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@SuppressWarnings("serial")
@GenerateBobBsonConverter
public class MediaContent implements java.io.Serializable {
  public Media media;
  public List<Image> images;

  public MediaContent() {}

  public MediaContent(Media media, List<Image> images) {
    this.media = media;
    this.images = images;
  }

  @Override
  public String toString() {
    return "MediaContent{" + "media=" + media + ", images=" + images + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MediaContent that = (MediaContent) o;

    if (images != null ? !images.equals(that.images) : that.images != null) return false;
    if (media != null ? !media.equals(that.media) : that.media != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = media != null ? media.hashCode() : 0;
    result = 31 * result + (images != null ? images.hashCode() : 0);
    return result;
  }

  public void setMedia(Media media) {
    this.media = media;
  }

  public void setImages(List<Image> images) {
    this.images = images;
  }

  //    @JsonAttribute(index = 1)
  public Media getMedia() {
    return media;
  }

  //    @JsonAttribute(index = 2)
  public List<Image> getImages() {
    return images;
  }
}
