package org.bobstuff.bobbson.jmhlargefail;

import com.google.common.base.Strings;
import org.bobstuff.bobbson.LargeObject;
import org.bobstuff.bobbson.MediumObject;
import org.bobstuff.bobbson.SmallObject;

import java.util.ArrayList;

public class Generator {
    public static SmallObject newSmallObject() {
        var obj = new SmallObject();
        obj.setName("John Smith");
        obj.setDescription("a small textual description using words");
        obj.setNumber1(1);
        obj.setNumber2(4.2);
        obj.setNumber3(3999999);
        obj.setOption(false);

        return obj;
    }

    public static MediumObject newMediumObject() {
        var smallObject = newSmallObject();

        var obj = new MediumObject();
        obj.setSmallObject(smallObject);
        obj.setLotsOfText(Strings.repeat("words of very little importance repeated a fair few times", 10));
        obj.setMoreText(Strings.repeat("another text field that is super exciting", 10));
        obj.setNumber1(2);
        obj.setNumber2(4);
        obj.setNumber3(6);
        obj.setNumber4(8);
        obj.setOption1(true);
        obj.setOption2(true);
        obj.setOption3(false);
        obj.setOption4(false);
        obj.setString1("a string 1");
        obj.setString2("a string 2");
        obj.setString3("a string 3");
        obj.setString4("a string 4");

        return obj;
    }

    public static LargeObject newLargeObject() {
        var smallObject = new SmallObject();
        var mediumObject = new MediumObject();

        var mediumObjects = new ArrayList<MediumObject>();
        for (var i = 0; i < 20; i += 1) {
            mediumObjects.add(mediumObject);
        }

        var obj = new LargeObject();
        obj.setMediumObjects(mediumObjects);
        obj.setSmallObject(smallObject);
        obj.setDouble1(1.2);
        obj.setDouble2(1.2);
        obj.setDouble3(1.2);
        obj.setDouble4(1.2);
        obj.setDouble5(1.2);
        obj.setDouble6(1.2);
        obj.setDouble7(1.2);
        obj.setDouble8(1.2);
        obj.setDouble9(1.2);

        obj.setString1(Strings.repeat("a smaller string", 10));
        obj.setString2(Strings.repeat("a smaller string", 10));
        obj.setString3(Strings.repeat("a smaller string", 10));
        obj.setString4(Strings.repeat("a smaller string", 10));
        obj.setString5(Strings.repeat("a smaller string", 10));
        obj.setString6(Strings.repeat("a smaller string", 10));
        obj.setString7(Strings.repeat("a smaller string", 10));
        obj.setString8(Strings.repeat("a smaller string", 10));
        obj.setString9(Strings.repeat("a smaller string", 10));

        return obj;
    }
}
