package airi.ojt.backend.ojtrestapi.events;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class EventTest {

  @Test
  public void builder() {
    Event event = Event.builder()
        .name("Spring REST API")
        .description("REST API development with Spring")
        .build();
    assertThat(event).isNotNull();
  }

  @Test
  public void javaBean() {
    //Given
    String name = "Event";
    String description = "Spring";

    //When
    Event event = new Event();
    event.setName(name);
    event.setDescription(description);

    //Then
    assertThat(event.getName()).isEqualTo(name);
    assertThat(event.getDescription()).isEqualTo(description);

  }

  @Test
  @Parameters
  public void testFree(int basePrice, int maxPrice, boolean isFree) {
    //Given
    Event event = Event.builder()
        .basePrice(basePrice)
        .maxPrice(maxPrice)
        .build();

    //When
    event.update();

    //Then
    Assertions.assertThat(event.isFree()).isEqualTo(isFree);
  }

  //parametersFor가 prefix!!
  //없으면 Test에서 @Parameters(method = "parametersForTestFree") 해줘야
  private Object[] parametersForTestFree() {
    return new Object[]{
        new Object[]{0, 0, true},
        new Object[]{100, 0, false},
        new Object[]{0, 100, false},
        new Object[]{100, 200, false}
    };
  }

  @Test
  @Parameters
  public void testOffline(String location, Boolean isOffline) {
    //Given
    Event event = Event.builder()
        .location(location)
        .build();

    //When
    event.update();

    //Then
    Assertions.assertThat(event.isOffline()).isEqualTo(isOffline);
  }

  private Object[] parametersForTestOffline() {
    return new Object[]{
        new Object[]{"강남", true},
        new Object[]{"   ", false},
        new Object[]{null, false},
    };
  }
}