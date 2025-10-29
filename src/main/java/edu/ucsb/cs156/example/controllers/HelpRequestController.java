package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for HelpRequest */
@Tag(name = "HelpRequest")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired HelpRequestRepository helpRequestRepository;

  /**
   * List all help requests
   *
   * @return an iterable of HelpRequest
   */
  @Operation(summary = "List all help requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    Iterable<HelpRequest> helpRequests = helpRequestRepository.findAll();
    return helpRequests;
  }

  /**
   * Get a single date by id
   *
   * @param id the id of the request
   * @return a HelpRequest
   */
  @Operation(summary = "Get a single request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    HelpRequest helpRequest =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    return helpRequest;
  }

  /**
   * Create a new help request
   *
   * @param requesterEmail
   * @param teamId
   * @param tableOrBreakoutRoom
   * @param requestTime
   * @param explanation
   * @param solved
   * @return
   */
  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved,
      @Parameter(
              name = "requestTime",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("requestTime")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime requestTime)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("requestTime={}", requestTime);

    HelpRequest helpRequest = new HelpRequest();
    helpRequest.setRequesterEmail(requesterEmail);
    helpRequest.setTeamId(teamId);
    helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
    helpRequest.setRequestTime(requestTime);
    helpRequest.setExplanation(explanation);
    helpRequest.setSolved(solved);

    HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

    return savedHelpRequest;
  }

  //   /**
  //    * Delete a UCSBDate
  //    *
  //    * @param id the id of the date to delete
  //    * @return a message indicating the date was deleted
  //    */
  //   @Operation(summary = "Delete a UCSBDate")
  //   @PreAuthorize("hasRole('ROLE_ADMIN')")
  //   @DeleteMapping("")
  //   public Object deleteUCSBDate(@Parameter(name = "id") @RequestParam Long id) {
  //     UCSBDate ucsbDate =
  //         ucsbDateRepository
  //             .findById(id)
  //             .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

  //     ucsbDateRepository.delete(ucsbDate);
  //     return genericMessage("UCSBDate with id %s deleted".formatted(id));
  //   }

  //   /**
  //    * Update a single date
  //    *
  //    * @param id id of the date to update
  //    * @param incoming the new date
  //    * @return the updated date object
  //    */
  //   @Operation(summary = "Update a single date")
  //   @PreAuthorize("hasRole('ROLE_ADMIN')")
  //   @PutMapping("")
  //   public UCSBDate updateUCSBDate(
  //       @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid UCSBDate incoming) {

  //     UCSBDate ucsbDate =
  //         ucsbDateRepository
  //             .findById(id)
  //             .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

  //     ucsbDate.setQuarterYYYYQ(incoming.getQuarterYYYYQ());
  //     ucsbDate.setName(incoming.getName());
  //     ucsbDate.setLocalDateTime(incoming.getLocalDateTime());

  //     ucsbDateRepository.save(ucsbDate);

  //     return ucsbDate;
  //   }
}
