package airi.ojt.backend.ojtrestapi.events;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import airi.ojt.backend.ojtrestapi.common.BaseControllerTest;
import airi.ojt.backend.ojtrestapi.common.TestDescription;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class EventControllerTests extends BaseControllerTest {

  @Autowired
  EventRepository eventRepository;

  @Test
  @TestDescription("정상적으로 이벤트 요청받은 테스트")
  public void createEvent() throws Exception {
    EventDto event = EventDto.builder()
        .name("Spring")
        .description("REST API Development with Spring")
        .beginEnrollmentDateTime(LocalDateTime.of(2021, 1, 30, 00, 00))
        .closeEnrollmentDateTime(LocalDateTime.of(2021, 2, 1, 00, 00))
        .beginEventDateTime(LocalDateTime.of(2021, 2, 1, 10, 30))
        .endEventDateTime(LocalDateTime.of(2021, 2, 17, 18, 00))
        .basePrice(100)
        .maxPrice(200)
        .limitOfEnrollment(40)
        .location("AIRI홀")
        .build();

    mockMvc.perform(post("/api/events")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaTypes.HAL_JSON)
        .content(objectMapper.writeValueAsString(event)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id").exists())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(jsonPath("free").value(false))
        .andExpect(jsonPath("offline").value(true))
        .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()));
  }

  @Test
  @TestDescription("입력 받을 수 없는 값을 사용한 경우트 테스")
  public void createEvent_Bad_Request() throws Exception {
    Event event = Event.builder()
        .id(100)
        .name("Spring")
        .description("REST API Development with Spring")
        .beginEnrollmentDateTime(LocalDateTime.of(2021, 1, 30, 00, 00))
        .closeEnrollmentDateTime(LocalDateTime.of(2021, 2, 1, 00, 00))
        .beginEventDateTime(LocalDateTime.of(2021, 2, 1, 10, 30))
        .endEventDateTime(LocalDateTime.of(2021, 2, 17, 18, 00))
        .basePrice(100)
        .maxPrice(200)
        .limitOfEnrollment(40)
        .location("AIRI홀")
        .free(true)
        .offline(false)
        .eventStatus(EventStatus.DRAFT)
        .build();

    mockMvc.perform(post("/api/events")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaTypes.HAL_JSON)
        .content(objectMapper.writeValueAsString(event)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @TestDescription("입력 값이 비어있을때 테스트")
  public void createEvent_Bad_Request_Empty_Input() throws Exception {
    EventDto eventDto = EventDto.builder().build();

    mockMvc.perform(post("/api/events")
        .contentType(MediaType.APPLICATION_JSON)
        .content(this.objectMapper.writeValueAsString(eventDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @TestDescription("입력 값이 잘못된 경우 테스트")
  public void createEvent_Bad_Request_Wrong_Input() throws Exception {
    EventDto eventDto = EventDto.builder()
        .name("Spring")
        .description("REST API Development with Spring")
        .beginEnrollmentDateTime(LocalDateTime.of(2021, 1, 30, 00, 00))
        .closeEnrollmentDateTime(LocalDateTime.of(2021, 2, 1, 00, 00))
        .beginEventDateTime(LocalDateTime.of(2021, 2, 1, 10, 30))
        .endEventDateTime(LocalDateTime.of(2021, 2, 1, 9, 00))
        .basePrice(10000)
        .maxPrice(200)
        .limitOfEnrollment(40)
        .location("AIRI홀")
        .build();

    mockMvc.perform(post("/api/events")
        .contentType(MediaType.APPLICATION_JSON)
        .content(this.objectMapper.writeValueAsString(eventDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$[0].objectName").exists())
        .andExpect(jsonPath("$[0].defaultMessage").exists())
        .andExpect(jsonPath("$[0].code").exists())
    ;
  }

  @Test
  @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
  public void queryEvents() throws Exception {
    //Given
    IntStream.range(0, 30).forEach(i -> {
      this.generateEvent(i);
    });

    //When
    this.mockMvc.perform(get("/api/events")
        .param("page", "1")
        .param("size", "10")
        .param("sort", "name,DESC")
    )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("page").exists())
        .andExpect(jsonPath("_embedded.eventList[0].self").exists()
        );
  }

  @Test
  @TestDescription("기존의 이벤트를 하나 조회하기")
  public void getEvent() throws Exception {
    //Given
    Event event = this.generateEvent(100);

    //When & then
    this.mockMvc.perform(get("/api/events/{id}", event.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("name").exists())
        .andExpect(jsonPath("id").exists());
  }

  @Test
  @TestDescription("없는 이벤트를 조회했을때 404 응답")
  public void getEvent404() throws Exception {
    //When & then
    this.mockMvc.perform(get("/api/events/99999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @TestDescription("이벤트 수정하기")
  public void updateEvent() throws Exception {
    //Given
    Event event = this.generateEvent(200);
    EventDto eventDto = this.modelMapper.map(event, EventDto.class);
    String eventName = "testest";
    eventDto.setName(eventName);

    //When & then
    this.mockMvc.perform(put("/api/events/{id}", event.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(this.objectMapper.writeValueAsString(eventDto)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("name").value(eventName));
  }

  @Test
  @TestDescription("입력값이 없눈 경우의 이벤트 수정하기 실패")
  public void updateEvent400_Empty() throws Exception {
    //Given
    Event event = this.generateEvent(200);
    EventDto eventDto = new EventDto();

    //When & then
    this.mockMvc.perform(put("/api/events/{id}", event.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(this.objectMapper.writeValueAsString(eventDto)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @TestDescription("입력값이 잘못된 경우의 이벤트 수정하기 실패")
  public void updateEvent400_Wrong() throws Exception {
    //Given
    Event event = this.generateEvent(200);
    EventDto eventDto = this.modelMapper.map(event, EventDto.class);
    eventDto.setBasePrice(20000);
    eventDto.setMaxPrice(1000);
    //When & then
    this.mockMvc.perform(put("/api/events/{id}", event.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(this.objectMapper.writeValueAsString(eventDto)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @TestDescription("존재하지 않는 이벤트 수정 실패")
  public void updateEvent404() throws Exception {
    //Given
    Event event = this.generateEvent(200);
    EventDto eventDto = this.modelMapper.map(event, EventDto.class);

    //When & then
    this.mockMvc.perform(put("/api/events/123123")
        .contentType(MediaType.APPLICATION_JSON)
        .content(this.objectMapper.writeValueAsString(eventDto)))
        .andDo(print())
        .andExpect(status().isNotFound());
  }


  private Event generateEvent(int index) {
    Event event = Event.builder()
        .name("Spring")
        .description("REST API Development with Spring")
        .beginEnrollmentDateTime(LocalDateTime.of(2021, 1, 30, 00, 00))
        .closeEnrollmentDateTime(LocalDateTime.of(2021, 2, 1, 00, 00))
        .beginEventDateTime(LocalDateTime.of(2021, 2, 1, 10, 30))
        .endEventDateTime(LocalDateTime.of(2021, 2, 17, 18, 00))
        .basePrice(100)
        .maxPrice(200)
        .limitOfEnrollment(40)
        .location("AIRI홀")
        .free(false)
        .offline(true)
        .eventStatus(EventStatus.DRAFT)
        .build();

    return this.eventRepository.save(event);
  }
}
