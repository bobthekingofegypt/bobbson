package org.bobstuff.bobbson;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class Person {
  private String name;
  private String occupation;

  @BsonConverter(target = CustomConverter.class)
  private String country;

  private int age;
  private double weight;
  private List<String> places;

  @BsonConverter(target = CustomListConverter.class)
  private List<String> aliases;

  @BsonConverter(target = CustomListToStringConverter.class)
  private String scores;

  private Set<String> vacationSpots;
  private Map<String, Integer> counts;
  private Map<String, Qualification> qualifications;

  public Map<String, Integer> getCounts() {
    return counts;
  }

  public void setCounts(Map<String, Integer> counts) {
    this.counts = counts;
  }

  public Map<String, Qualification> getQualifications() {
    return qualifications;
  }

  public void setQualifications(Map<String, Qualification> qualifications) {
    this.qualifications = qualifications;
  }

  public Set<String> getVacationSpots() {
    return vacationSpots;
  }

  public void setVacationSpots(Set<String> vacationSpots) {
    this.vacationSpots = vacationSpots;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOccupation() {
    return occupation;
  }

  public void setOccupation(String occupation) {
    this.occupation = occupation;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public int getAge() {
    return age;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public List<String> getPlaces() {
    return places;
  }

  public void setPlaces(List<String> places) {
    this.places = places;
  }

  public String getScores() {
    return scores;
  }

  public void setScores(String scores) {
    this.scores = scores;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person person = (Person) o;
    return age == person.age
        && Double.compare(person.weight, weight) == 0
        && Objects.equals(name, person.name)
        && Objects.equals(occupation, person.occupation)
        && Objects.equals(country, person.country)
        && Objects.equals(places, person.places)
        && Objects.equals(aliases, person.aliases)
        && Objects.equals(scores, person.scores)
        && Objects.equals(vacationSpots, person.vacationSpots)
        && Objects.equals(counts, person.counts)
        && Objects.equals(qualifications, person.qualifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        occupation,
        country,
        age,
        weight,
        places,
        aliases,
        scores,
        vacationSpots,
        counts,
        qualifications);
  }

  @Override
  public String toString() {
    return "Person{"
        + "name='"
        + name
        + '\''
        + ", occupation='"
        + occupation
        + '\''
        + ", country='"
        + country
        + '\''
        + ", age="
        + age
        + ", weight="
        + weight
        + ", places="
        + places
        + ", aliases="
        + aliases
        + ", scores='"
        + scores
        + '\''
        + ", vacationSpots="
        + vacationSpots
        + ", counts="
        + counts
        + ", qualifications="
        + qualifications
        + '}';
  }
}
