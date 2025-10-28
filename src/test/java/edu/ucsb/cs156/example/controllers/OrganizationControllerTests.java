package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class OrganizationControllerTests extends ControllerTestCase {

  @MockBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganization/admin/all
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/ucsborganization/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganization/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganization/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganization() throws Exception {

    // arrange

    UCSBOrganization ab =
        UCSBOrganization.builder()
            .orgTranslation("Alpha Beta")
            .orgCode("AB")
            .orgTranslationShort("Alpha Beta")
            .inactive(false)
            .build();

    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgTranslation("Zeta Phi Rho")
            .orgCode("zpr")
            .orgTranslationShort("Zeta Phi Rho")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganization> expectedOrganization = new ArrayList<>();
    expectedOrganization.addAll(Arrays.asList(ab, zpr));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganization);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization krc =
        UCSBOrganization.builder()
            .orgTranslation("Korean_Radio_Club")
            .orgCode("krc")
            .orgTranslationShort("Korean_Radio_Cl")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(krc))).thenReturn(krc);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post?orgTranslation=Korean_Radio_Club&orgCode=krc&orgTranslationShort=Korean_Radio_Cl&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(krc);
    String expectedJson = mapper.writeValueAsString(krc);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);

    // Check inactive
    UCSBOrganization save =
        mapper.readValue(response.getResponse().getContentAsString(), UCSBOrganization.class);
    assertEquals(true, save.getInactive());
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization?orgCode=krc"))
        .andExpect(status().is(403)); // logged out users can't get by id (orgCode)
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBOrganization organization =
        UCSBOrganization.builder()
            .orgTranslation("Alpha Beta")
            .orgCode("AB")
            .orgTranslationShort("Alpha Beta")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("AB"))).thenReturn(Optional.of(organization));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=AB"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("AB"));
    String expectedJson = mapper.writeValueAsString(organization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("ASB"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=ASB"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("ASB"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id ASB not found", json.get("message"));
  }

  //   @WithMockUser(roles = {"ADMIN", "USER"})
  //   @Test
  //   public void admin_can_edit_an_existing_commons() throws Exception {
  //     // arrange

  //     UCSBDiningCommons carrilloOrig =
  //         UCSBDiningCommons.builder()
  //             .name("Carrillo")
  //             .code("carrillo")
  //             .hasSackMeal(false)
  //             .hasTakeOutMeal(false)
  //             .hasDiningCam(true)
  //             .latitude(34.409953)
  //             .longitude(-119.85277)
  //             .build();

  //     UCSBDiningCommons carrilloEdited =
  //         UCSBDiningCommons.builder()
  //             .name("Carrillo Dining Hall")
  //             .code("carrillo")
  //             .hasSackMeal(true)
  //             .hasTakeOutMeal(true)
  //             .hasDiningCam(false)
  //             .latitude(34.409954)
  //             .longitude(-119.85278)
  //             .build();

  //     String requestBody = mapper.writeValueAsString(carrilloEdited);

  //     when(ucsbDiningCommonsRepository.findById(eq("carrillo")))
  //         .thenReturn(Optional.of(carrilloOrig));

  //     // act
  //     MvcResult response =
  //         mockMvc
  //             .perform(
  //                 put("/api/ucsbdiningcommons?code=carrillo")
  //                     .contentType(MediaType.APPLICATION_JSON)
  //                     .characterEncoding("utf-8")
  //                     .content(requestBody)
  //                     .with(csrf()))
  //             .andExpect(status().isOk())
  //             .andReturn();

  //     // assert
  //     verify(ucsbDiningCommonsRepository, times(1)).findById("carrillo");
  //     verify(ucsbDiningCommonsRepository, times(1))
  //         .save(carrilloEdited); // should be saved with updated info
  //     String responseString = response.getResponse().getContentAsString();
  //     assertEquals(requestBody, responseString);
  //   }

  //   @WithMockUser(roles = {"ADMIN", "USER"})
  //   @Test
  //   public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
  //     // arrange

  //     UCSBDiningCommons editedCommons =
  //         UCSBDiningCommons.builder()
  //             .name("Munger Hall")
  //             .code("munger-hall")
  //             .hasSackMeal(false)
  //             .hasTakeOutMeal(false)
  //             .hasDiningCam(true)
  //             .latitude(34.420799)
  //             .longitude(-119.852617)
  //             .build();

  //     String requestBody = mapper.writeValueAsString(editedCommons);

  //     when(ucsbDiningCommonsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

  //     // act
  //     MvcResult response =
  //         mockMvc
  //             .perform(
  //                 put("/api/ucsbdiningcommons?code=munger-hall")
  //                     .contentType(MediaType.APPLICATION_JSON)
  //                     .characterEncoding("utf-8")
  //                     .content(requestBody)
  //                     .with(csrf()))
  //             .andExpect(status().isNotFound())
  //             .andReturn();

  //     // assert
  //     verify(ucsbDiningCommonsRepository, times(1)).findById("munger-hall");
  //     Map<String, Object> json = responseToJson(response);
  //     assertEquals("UCSBDiningCommons with id munger-hall not found", json.get("message"));
  //   }

}
