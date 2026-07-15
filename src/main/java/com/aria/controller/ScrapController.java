package com.aria.controller;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ScrapController {

	@Autowired
	ScrapService scrapService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/saveScrap")
	public ResponseEntity<?> saveScrap(@RequestBody ScrapDto scrapDto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return scrapService.saveScrap(scrapDto, user.get());
	}

	@PutMapping("/updateScrap")
	public ResponseEntity<?> updateScrap(@RequestBody ScrapDto scrapDto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return scrapService.updateScrap(scrapDto, user.get());
	}

	@GetMapping("/getScrapList")
	public ResponseEntity<?> getScrap(Pageable pageable, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return scrapService.listAllScrap(pageable);
		} else {
			return new ResponseEntity<List<ScrapDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getScrapDetails/{id}")
	public ResponseEntity<?> getScrap(@PathVariable String id, Authentication authentication) {
		return scrapService.getScrap(id);
	}
	
	@GetMapping("/getScrapFilterList")
	public ResponseEntity<?> scrapList(
			@RequestParam(value = "scrapItem", required = false) String scrapItem,
			@RequestParam(value = "departmentId", required = false) Long departmentId,
			@RequestParam(value = "fromProcess", required = false) String fromProcess,
			@RequestParam(value = "selectStorageBin", required = false) String selectStorageBin,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
	     	@RequestParam(value = "dateTo", required = false) String dateTo,Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return scrapService.scrapfilterList(scrapItem,departmentId,fromProcess,selectStorageBin,dateFrom,dateTo);
		} else {
			return new ResponseEntity<List<ScrapDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	} 

	@DeleteMapping("/deleteScrapDetails/{id}")
	public ResponseEntity<?> deleteScrap(@PathVariable String id, Authentication authentication) {
		return scrapService.deleteScrap(id);
	}
	
	

}
