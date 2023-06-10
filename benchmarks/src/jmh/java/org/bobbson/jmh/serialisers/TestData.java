package org.bobbson.jmh.serialisers;

import java.util.ArrayList;
import java.util.List;

public class TestData {
  public static MediaContent standard() {
    var media = new Media();
    media.uri = "http://javaone.com/keynote.mpg";
    media.title = "Javaone Keynote";
    media.width = 640;
    media.height = 480;
    media.format = "video/mpg4";
    media.duration = 18000000;
    media.size = 58982400;
    media.bitrate = 262144; // 256k

    var persons = new ArrayList<String>();
    persons.add("Bill Gates");
    persons.add("Steve Jobs스");

    media.persons = persons;

    media.player = Media.Player.JAVA;
    media.copyright = null;

    var image = new Image();
    image.uri = "http://javaone.com/keynote_large.jpg";
    image.title = "Javaone Keynote";
    image.width = 1024;
    image.height = 768;
    image.size = Image.Size.LARGE;

    var image2 = new Image();
    image2.uri = "http://javaone.com/keynote_small.jpg";
    image2.title = "Javaone Keynote";
    image2.width = 320;
    image2.height = 240;
    image2.size = Image.Size.SMALL;

    var mc = new MediaContent(media, List.of(image, image2));
    return mc;
  }

  public static MediaContent custom() {
    var media = new Media();
    media.bitrate = 345;
    media.copyright = "jesus christ스";
    media.uri = "http://asimpleurlforalltosee";

    var persons = new ArrayList<String>();
    persons.add("bob");
    persons.add("Fred");
    persons.add("Jesus");
    persons.add("Steve Jobs스");
    persons.add("Fraser");
    persons.add("Stuart");
    persons.add("Christopher");
    persons.add("Charles");
    media.persons = persons;
    media.format = "amediaformat";
    media.duration = 3566;
    //        media.hasBitrate = true;
    media.height = 134;
    media.player = Media.Player.JAVA;
    media.size = 3454522;
    media.title = "A media title that no one would ever have used before";
    media.width = 3456643;

    var image =
        new Image(
            "http://www.images.com/2326464353",
            "a brilliant picture of pure awesomeness",
            123455,
            548783,
            Image.Size.SMALL);
    var images = new ArrayList<Image>();
    images.add(image);
    images.add(image);
    images.add(image);
    images.add(image);
    images.add(image);
    images.add(image);
    images.add(image);

    var mediaContent = new MediaContent();
    mediaContent.media = media;
    mediaContent.images = images;

    return mediaContent;
  }
}
