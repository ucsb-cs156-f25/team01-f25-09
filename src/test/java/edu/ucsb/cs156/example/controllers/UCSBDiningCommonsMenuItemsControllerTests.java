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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemsController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemsControllerTests extends ControllerTestCase {
  @MockBean UCSBDiningCommonsMenuItemsRepository UCSBDiningCommonsMenuItemsRepository;
  @MockBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItems/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItems/all"))
        .andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/UCSBDiningCommonsMenuItems/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/UCSBDiningCommonsMenuItems/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItem =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("ortega")
            .name("pasta")
            .station("italian")
            .build();

    when(UCSBDiningCommonsMenuItemsRepository.findById(eq(7L)))
        .thenReturn(Optional.of(UCSBDiningCommonsMenuItem));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItems?id=7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(UCSBDiningCommonsMenuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(UCSBDiningCommonsMenuItemsRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItems?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItems with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_UCSBDiningCommonsMenuItemss() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItems1 =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("ortega")
            .name("pasta")
            .station("italian")
            .build();

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItems2 =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("dlg")
            .name("pizza")
            .station("american")
            .build();

    ArrayList<UCSBDiningCommonsMenuItems> expectedItems = new ArrayList<>();
    expectedItems.addAll(Arrays.asList(UCSBDiningCommonsMenuItems1, UCSBDiningCommonsMenuItems2));

    when(UCSBDiningCommonsMenuItemsRepository.findAll()).thenReturn(expectedItems);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItems/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_UCSBDiningCommonsMenuItems() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItems3 =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("dlg")
            .name("pizza")
            .station("american")
            .build();

    when(UCSBDiningCommonsMenuItemsRepository.save(eq(UCSBDiningCommonsMenuItems3)))
        .thenReturn(UCSBDiningCommonsMenuItems3);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBDiningCommonsMenuItems/post?diningCommonsCode=dlg&name=pizza&station=american")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).save(UCSBDiningCommonsMenuItems3);
    String expectedJson = mapper.writeValueAsString(UCSBDiningCommonsMenuItems3);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_UCSBDiningCommonsMenuItems() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItemsOrig =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("dlg")
            .name("pizza")
            .station("american")
            .build();

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItemsEdited =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("ortega")
            .name("pasta")
            .station("italian")
            .build();

    String requestBody = mapper.writeValueAsString(UCSBDiningCommonsMenuItemsEdited);

    when(UCSBDiningCommonsMenuItemsRepository.findById(eq(67L)))
        .thenReturn(Optional.of(UCSBDiningCommonsMenuItemsOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItems?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).findById(67L);
    verify(UCSBDiningCommonsMenuItemsRepository, times(1))
        .save(UCSBDiningCommonsMenuItemsEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_UCSBDiningCommonsMenuItems_that_does_not_exist() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuEditedItems =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("ortega")
            .name("pasta")
            .station("italian")
            .build();

    String requestBody = mapper.writeValueAsString(UCSBDiningCommonsMenuEditedItems);

    when(UCSBDiningCommonsMenuItemsRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItems?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItems with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItems1 =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("ortega")
            .name("pasta")
            .station("italian")
            .build();

    when(UCSBDiningCommonsMenuItemsRepository.findById(eq(15L)))
        .thenReturn(Optional.of(UCSBDiningCommonsMenuItems1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItems?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).findById(15L);
    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItems with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_MenuItems_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(UCSBDiningCommonsMenuItemsRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItems?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(UCSBDiningCommonsMenuItemsRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItems with id 15 not found", json.get("message"));
  }
}
