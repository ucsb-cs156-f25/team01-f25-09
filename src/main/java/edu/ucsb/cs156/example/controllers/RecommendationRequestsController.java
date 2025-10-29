package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for RecommendationRequests */
@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all recommendation requests
   *
   * @return an iterable of recommendation requests
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> recommendationRequests =
        recommendationRequestRepository.findAll();
    return recommendationRequests;
  }

  /**
   * Look up a single RecommendationRequest by id.
   *
   * @param id the id of the RecommendationRequest to retrieve
   * @return the matching RecommendationRequest as JSON
   * @throws edu.ucsb.cs156.example.errors.EntityNotFoundException if no record with that id exists
   */
  @Operation(summary = "Get a single recommendationRequest")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return recommendationRequest;
  }

  /**
   * Create a new recommendation request.
   *
   * @param code a short code or identifier for the request (e.g. course/letter type)
   * @param requesterEmail the student's email requesting the recommendation
   * @param professorEmail the professor's email who is being asked
   * @param explanation context/reason for the request
   * @param dateRequested when the request was made (ISO 8601, e.g. 2025-01-10T12:00:00)
   * @param dateNeeded when the letter/recommendation is needed by (ISO 8601)
   * @param done whether the recommendation has already been completed
   * @return the saved RecommendationRequest, including any generated id
   * @throws com.fasterxml.jackson.core.JsonProcessingException if there is an issue processing JSON
   */
  @Operation(summary = "Create a new RecommendationRequest")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequests(
      @Parameter(name = "code") @RequestParam String code,
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "dateRequested", description = "ISO datetime e.g. 2025-10-28T14:30:00")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(name = "dateNeeded", description = "ISO datetime e.g. 2025-11-15T23:59:00")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam boolean done)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateRequested={}", dateRequested);
    log.info("dateNeeded={}", dateNeeded);

    RecommendationRequest rr = new RecommendationRequest();
    rr.setCode(code);
    rr.setRequesterEmail(requesterEmail);
    rr.setProfessorEmail(professorEmail);
    rr.setExplanation(explanation);
    rr.setDateRequested(dateRequested);
    rr.setDateNeeded(dateNeeded);
    rr.setDone(done);

    RecommendationRequest saved = recommendationRequestRepository.save(rr);

    return saved;
  }

  /**
   * Delete a RecommendationRequest
   *
   * @param id the id of the request to delete
   * @return a message indicating the request was deleted
   */
  @Operation(summary = "Delete a recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteRecommendationRequest(@Parameter(name = "id") @RequestParam Long id) {

    RecommendationRequest rr =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recommendationRequestRepository.delete(rr);
    return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
  }

  /**
   * Update an existing RecommendationRequest identified by id.
   *
   * @param id the id of the RecommendationRequest to update
   * @param incoming the new field values for this RecommendationRequest (code, requesterEmail,
   *     professorEmail, explanation, dateRequested, dateNeeded, done)
   * @return the updated RecommendationRequest, after saving
   * @throws edu.ucsb.cs156.example.errors.EntityNotFoundException if no RecommendationRequest with
   *     that id exists
   */
  @Operation(summary = "Update a single recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequest updateRecommendationRequest(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid RecommendationRequest incoming) {

    RecommendationRequest rr =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    rr.setCode(incoming.getCode());
    rr.setRequesterEmail(incoming.getRequesterEmail());
    rr.setProfessorEmail(incoming.getProfessorEmail());
    rr.setExplanation(incoming.getExplanation());
    rr.setDateRequested(incoming.getDateRequested());
    rr.setDateNeeded(incoming.getDateNeeded());
    rr.setDone(incoming.getDone());

    recommendationRequestRepository.save(rr);

    return rr;
  }
}
