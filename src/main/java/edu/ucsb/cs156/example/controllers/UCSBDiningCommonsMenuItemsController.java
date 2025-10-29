package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDiningCommonsMenuItems */
@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/UCSBDiningCommonsMenuItems")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemsController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemsRepository UCSBDiningCommonsMenuItemsRepository;

  /**
   * List all menu items
   *
   * @return an iterable of UCSBDiningCommonsMenuItems
   */
  @Operation(summary = "List all items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItems> allItems() {
    Iterable<UCSBDiningCommonsMenuItems> items = UCSBDiningCommonsMenuItemsRepository.findAll();
    return items;
  }

  /**
   * Get a single item by id
   *
   * @param id the id of the item
   * @return a item
   */
  @Operation(summary = "Get a single item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItems getById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItems UCSBDiningCommonsMenuItems =
        UCSBDiningCommonsMenuItemsRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItems.class, id));

    return UCSBDiningCommonsMenuItems;
  }

  /**
   * Create a new menu item
   *
   * @param diningCommonsCode the dining hall name
   * @param name the name of the menu item
   * @param station where food is located
   * @return the saved menu item
   */
  @Operation(summary = "Create a new item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItems postItems(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station) {

    UCSBDiningCommonsMenuItems items = new UCSBDiningCommonsMenuItems();
    items.setDiningCommonsCode(diningCommonsCode);
    items.setName(name);
    items.setStation(station);

    UCSBDiningCommonsMenuItems savedItems = UCSBDiningCommonsMenuItemsRepository.save(items);

    return savedItems;
  }
}
