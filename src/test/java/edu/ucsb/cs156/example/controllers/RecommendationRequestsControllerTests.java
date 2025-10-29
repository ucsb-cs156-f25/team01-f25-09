package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase {

  @MockBean RecommendationRequestRepository recommendationRequestRepository;

  @MockBean UserRepository userRepository;

  //
  // Tests for GET /api/recommendationrequests/all
  //

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(403)); // 403 Forbidden when not logged in
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    // arrange
    LocalDateTime reqDate1 = LocalDateTime.parse("2025-01-10T12:00:00");
    LocalDateTime needDate1 = LocalDateTime.parse("2025-02-01T23:59:00");

    RecommendationRequest rr1 =
        RecommendationRequest.builder()
            .code("CMPSC156-GradLetter")
            .requesterEmail("student@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Applying to grad school, need letter.")
            .dateRequested(reqDate1)
            .dateNeeded(needDate1)
            .done(false)
            .build();

    LocalDateTime reqDate2 = LocalDateTime.parse("2025-03-05T09:30:00");
    LocalDateTime needDate2 = LocalDateTime.parse("2025-03-20T23:59:00");

    RecommendationRequest rr2 =
        RecommendationRequest.builder()
            .code("InternshipRec")
            .requesterEmail("intern@ucsb.edu")
            .professorEmail("mentor@ucsb.edu")
            .explanation("Need reference for internship at ACME Robotics.")
            .dateRequested(reqDate2)
            .dateNeeded(needDate2)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(rr1, rr2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  //
  // Tests for POST /api/recommendationrequests/post
  //

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/recommendationrequests/post"))
        .andExpect(status().is(403)); // 403 because not logged in
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/recommendationrequests/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_user_can_post_new_recommendation_request() throws Exception {
    // arrange
    LocalDateTime reqDate = LocalDateTime.parse("2025-01-10T12:00:00");
    LocalDateTime needDate = LocalDateTime.parse("2025-02-01T23:59:00");

    String expectedCode = "CMPSC156-GradLetter";
    String expectedRequester = "student@ucsb.edu";
    String expectedProfessor = "prof@ucsb.edu";
    String expectedExplanation = "Applying to grad school, need letter.";
    boolean expectedDone = true;

    RecommendationRequest rrReturned =
        RecommendationRequest.builder()
            .code(expectedCode)
            .requesterEmail(expectedRequester)
            .professorEmail(expectedProfessor)
            .explanation(expectedExplanation)
            .dateRequested(reqDate)
            .dateNeeded(needDate)
            .done(expectedDone)
            .build();

    when(recommendationRequestRepository.save(any(RecommendationRequest.class)))
        .thenAnswer(
            invocation -> {
              RecommendationRequest arg = invocation.getArgument(0, RecommendationRequest.class);

              assertEquals(expectedCode, arg.getCode(), "code mismatch");
              assertEquals(expectedRequester, arg.getRequesterEmail(), "requesterEmail mismatch");
              assertEquals(expectedProfessor, arg.getProfessorEmail(), "professorEmail mismatch");
              assertEquals(expectedExplanation, arg.getExplanation(), "explanation mismatch");
              assertEquals(reqDate, arg.getDateRequested(), "dateRequested mismatch");
              assertEquals(needDate, arg.getDateNeeded(), "dateNeeded mismatch");

              assertEquals(expectedDone, arg.getDone(), "done mismatch");

              return rrReturned;
            });

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("code", expectedCode)
                    .param("requesterEmail", expectedRequester)
                    .param("professorEmail", expectedProfessor)
                    .param("explanation", expectedExplanation)
                    .param("dateRequested", "2025-01-10T12:00:00")
                    .param("dateNeeded", "2025-02-01T23:59:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    String expectedJson = mapper.writeValueAsString(rrReturned);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  //
  // Tests for GET /api/recommendationrequests?id=...
  //

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests?id=7"))
        .andExpect(status().is(403)); // not logged in -> forbidden
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime reqDate = LocalDateTime.parse("2025-01-10T12:00:00");
    LocalDateTime needDate = LocalDateTime.parse("2025-02-01T23:59:00");

    RecommendationRequest rr =
        RecommendationRequest.builder()
            .code("CMPSC156-GradLetter")
            .requesterEmail("student@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Applying to grad school, need letter.")
            .dateRequested(reqDate)
            .dateNeeded(needDate)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(rr));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests?id=7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(rr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_gets_404_when_id_does_not_exist() throws Exception {

    // arrange
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(eq(7L));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  //
  // Tests for DELETE /api/recommendationrequests?id=...
  //

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_recommendationrequest() throws Exception {
    // arrange
    LocalDateTime reqDate = LocalDateTime.parse("2025-01-10T12:00:00");
    LocalDateTime needDate = LocalDateTime.parse("2025-02-01T23:59:00");

    RecommendationRequest rr =
        RecommendationRequest.builder()
            .code("CMPSC156-GradLetter")
            .requesterEmail("student@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Applying to grad school, need letter.")
            .dateRequested(reqDate)
            .dateNeeded(needDate)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(rr));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existent_recommendationrequest_and_gets_404()
      throws Exception {

    // arrange
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }

  //
  // Tests for PUT /api/recommendationrequests?id=...
  //

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
    // arrange: original in DB
    LocalDateTime oldReqDate = LocalDateTime.parse("2025-01-10T12:00:00");
    LocalDateTime oldNeedDate = LocalDateTime.parse("2025-02-01T23:59:00");

    RecommendationRequest rrOriginal =
        RecommendationRequest.builder()
            .code("CMPSC156-GradLetter")
            .requesterEmail("oldstudent@ucsb.edu")
            .professorEmail("oldprof@ucsb.edu")
            .explanation("Old explanation")
            .dateRequested(oldReqDate)
            .dateNeeded(oldNeedDate)
            .done(false)
            .build();

    // incoming edited values from client
    LocalDateTime newReqDate = LocalDateTime.parse("2025-01-15T09:00:00");
    LocalDateTime newNeedDate = LocalDateTime.parse("2025-02-10T17:00:00");

    RecommendationRequest rrEditedIncoming =
        RecommendationRequest.builder()
            .code("CMPSC156-UpdatedLetter")
            .requesterEmail("student@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Updated info and deadlines.")
            .dateRequested(newReqDate)
            .dateNeeded(newNeedDate)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(rrEditedIncoming);

    // this is what the repo returns AFTER save
    // (often you just return the 'incoming' back anyway, so this is okay)
    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(rrOriginal));

    when(recommendationRequestRepository.save(any(RecommendationRequest.class)))
        .thenReturn(rrEditedIncoming);

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert:
    // capture what controller actually tried to save
    ArgumentCaptor<RecommendationRequest> captor =
        ArgumentCaptor.forClass(RecommendationRequest.class);
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1)).save(captor.capture());

    RecommendationRequest savedArg = captor.getValue();

    // Now verify ALL fields were copied into what was saved
    // (This is where we kill the mutants that removed setRequesterEmail / setProfessorEmail)
    assertEquals("CMPSC156-UpdatedLetter", savedArg.getCode());
    assertEquals(
        "student@ucsb.edu", savedArg.getRequesterEmail()); // kills setRequesterEmail mutant
    assertEquals("prof@ucsb.edu", savedArg.getProfessorEmail()); // kills setProfessorEmail mutant
    assertEquals("Updated info and deadlines.", savedArg.getExplanation());
    assertEquals(newReqDate, savedArg.getDateRequested());
    assertEquals(newNeedDate, savedArg.getDateNeeded());
    assertEquals(true, savedArg.getDone());

    // and response should echo what repo returned
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
    // arrange
    LocalDateTime reqDate = LocalDateTime.parse("2025-01-10T12:00:00");
    LocalDateTime needDate = LocalDateTime.parse("2025-02-01T23:59:00");

    RecommendationRequest rrEdited =
        RecommendationRequest.builder()
            .code("CMPSC156-GradLetter")
            .requesterEmail("student@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("Applying to grad school, need letter.")
            .dateRequested(reqDate)
            .dateNeeded(needDate)
            .done(false)
            .build();

    String requestBody = mapper.writeValueAsString(rrEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }
}
