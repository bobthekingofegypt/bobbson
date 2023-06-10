package org.bobbson.jmh.serialisers;

import java.util.List;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@SuppressWarnings("serial")
@GenerateBobBsonConverter
public class Media implements java.io.Serializable {
  public String uri;
  public String title; // Can be unset.
  public int width;
  public int height;
  public String format;
  public long duration;
  public long size;
  public int bitrate; // Can be unset.
  public List<String> persons;
  public Player player;
  public String copyright; // Can be unset.

  public Media() {}

  public Media(
      String uri,
      String title,
      int width,
      int height,
      String format,
      long duration,
      long size,
      int bitrate,
      boolean hasBitrate,
      List<String> persons,
      Player player,
      String copyright) {
    this.uri = uri;
    this.title = title;
    this.width = width;
    this.height = height;
    this.format = format;
    this.duration = duration;
    this.size = size;
    this.bitrate = bitrate;
    //		this.hasBitrate = hasBitrate;
    this.persons = persons;
    this.player = player;
    this.copyright = copyright;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Media media = (Media) o;

    if (bitrate != media.bitrate) return false;
    if (duration != media.duration) return false;
    //		if (hasBitrate != media.hasBitrate) return false;
    if (height != media.height) return false;
    if (size != media.size) return false;
    if (width != media.width) return false;
    if (copyright != null ? !copyright.equals(media.copyright) : media.copyright != null)
      return false;
    if (format != null ? !format.equals(media.format) : media.format != null) return false;
    if (persons != null ? !persons.equals(media.persons) : media.persons != null) return false;
    if (player != media.player) return false;
    if (title != null ? !title.equals(media.title) : media.title != null) return false;
    if (uri != null ? !uri.equals(media.uri) : media.uri != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uri != null ? uri.hashCode() : 0;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + width;
    result = 31 * result + height;
    result = 31 * result + (format != null ? format.hashCode() : 0);
    result = 31 * result + (int) (duration ^ (duration >>> 32));
    result = 31 * result + (int) (size ^ (size >>> 32));
    result = 31 * result + bitrate;
    //		result = 31 * result + (hasBitrate ? 1 : 0);
    result = 31 * result + (persons != null ? persons.hashCode() : 0);
    result = 31 * result + (player != null ? player.hashCode() : 0);
    result = 31 * result + (copyright != null ? copyright.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Media{"
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
        + ", format='"
        + format
        + '\''
        + ", duration="
        + duration
        + ", size="
        + size
        + ", bitrate="
        + bitrate
        +
        //				", hasBitrate=" + hasBitrate +
        ", persons="
        + persons
        + ", player="
        + player
        + ", copyright='"
        + copyright
        + '\''
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
  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  //    @JsonAttribute(index = 6)
  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  //    @JsonAttribute(index = 7)
  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  //    @JsonAttribute(index = 8)
  public int getBitrate() {
    return bitrate;
  }

  public void setBitrate(int bitrate) {
    this.bitrate = bitrate;
    //        this.hasBitrate = true;
  }

  //    @JsonAttribute(index = 9)
  public List<String> getPersons() {
    return persons;
  }

  public void setPersons(List<String> persons) {
    this.persons = persons;
  }

  //    @JsonAttribute(index = 10)
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  //    @JsonAttribute(index = 11)
  public String getCopyright() {
    return copyright;
  }

  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }

  @GenerateBobBsonConverter
  public enum Player {
    JAVA,
    FLASH;

    public static Player find(String str) {
      if (str == "JAVA") return JAVA;
      if (str == "FLASH") return FLASH;
      if ("JAVA".equals(str)) return JAVA;
      if ("FLASH".equals(str)) return FLASH;
      String desc = (str == null) ? "NULL" : String.format("'%s'", str);
      throw new IllegalArgumentException("No Player value of " + desc);
    }
  }
}
