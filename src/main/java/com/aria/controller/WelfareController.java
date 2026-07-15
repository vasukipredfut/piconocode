
package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.WelfareContributionDto;
import com.aria.dto.WelfareItemsDto;
import com.aria.model.User;
import com.aria.service.WelfareService;

import org.springframework.beans.factory.annotation.Autowired;
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
public class WelfareController {
    
    @Autowired
    WelfareService welfareService;

    

    @Autowired
    UserRepository userRepository;

    //welfare contribution

    @PostMapping("/saveWelfareContribution")
    public ResponseEntity<?> saveWelfareContribution(@RequestBody WelfareContributionDto welfareContributionDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return welfareService.saveWelfareContribution(welfareContributionDto, user.get());
    }

    @PutMapping("/updateWelfareContribution")
    public ResponseEntity<?> updateWelfareContribution(@RequestBody WelfareContributionDto welfareContributionDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return welfareService.updateWelfareContribution(welfareContributionDto, user.get());
    }

    @GetMapping("/getWelfareContributionList")
    public ResponseEntity<?> getWelfareContributionList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
       if (user.get() != null) {
            return welfareService.listAllWelfareContribution();
        } else {
            return new ResponseEntity<List<WelfareContributionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getWelfareContributionDetails/{id}")
    public ResponseEntity<?> getWelfareContributionDetails(@PathVariable String id, Authentication authentication) {
        return welfareService.getWelfareContributionDetails(id);
    }

    @DeleteMapping("/deleteWelfareContributionDetails/{id}")
    public ResponseEntity<?> deleteWelfareContributionDetails(@PathVariable String id, Authentication authentication) {
        return welfareService.deleteWelfareContribution(id);
    }
    
    
    @GetMapping("/getWelfareAmountList")
	public ResponseEntity<?> getWelfareAmount(Authentication authentication,
			@RequestParam("fromDate") String fromDate,@RequestParam("toDate") String toDate) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		//if (user.get().getRole().getName().equalsIgnoreCase("Admin")) {
			return welfareService.listAllWelfareAmount(fromDate, toDate);
	//	} else {
	//		return new ResponseEntity<List<WelfareContributionDto>>(new ArrayList<>(), HttpStatus.OK);
	//	}
	}
    
//welfare amount
    
/*    @PostMapping("/saveWelfareAmount")
    public ResponseEntity<?> saveWelfareAmount(@RequestBody WelfareAmountDto welfareAmountDto,Authentication authentication) {

  	UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

  	Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

  	return welfareService.saveWelfareAmount(welfareAmountDto, user.get());
    }

    @PutMapping("/updateWelfareAmount")
    public ResponseEntity<?> updateWelfareAmount(@RequestBody WelfareAmountDto welfareAmountDto,Authentication authentication) {

  	UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

  	Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

  	return welfareService.updateWelfareAmount(welfareAmountDto, user.get());
     }

      @GetMapping("/getWelfareAmountList")
     public ResponseEntity<?> getWelfareAmount(Authentication authentication) {

  	UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
  	Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
  	if (user.get().getRole().getName().equalsIgnoreCase("Admin")) {
  		return welfareService.listAllWelfareAmount();
  	} else {
  		return new ResponseEntity<List<WelfareAmountDto>>(new ArrayList<>(), HttpStatus.OK);
  	}
    }

     @GetMapping("/getWelfareAmountDetails/{id}")
     public ResponseEntity<?> getWelfareAmount(@PathVariable String id, Authentication authentication) {
  	return welfareService.getWelfareAmountDetails(id);
      }


      @DeleteMapping("/deleteWelfareAmount/{id}")
     public ResponseEntity<?> deleteWelfareAmount(@PathVariable String id, Authentication authentication) {
  	return welfareService.deleteWelfareAmountDetails(id);
    }
*/

//welfare items
  @PostMapping("/saveWelfareItems")
  public ResponseEntity<?> saveWelfareItems(@RequestBody WelfareItemsDto welfareItemsDto,Authentication authentication) {

	UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

	Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

	return welfareService.saveWelfareItems(welfareItemsDto, user.get());
  }

  @PutMapping("/updateWelfareItems")
  public ResponseEntity<?> updateWelfareItems(@RequestBody WelfareItemsDto welfareItemsDto,Authentication authentication) {

	UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

	Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

	return welfareService.updateWelfareItems(welfareItemsDto, user.get());
   }

    @GetMapping("/getWelfareItemsList")
   public ResponseEntity<?> getWelfareItems(Authentication authentication) {

	UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
//	if (user.get().getRole().getName().equalsIgnoreCase("Admin")) {
		return welfareService.listAllWelfareItems();
	//} else {
//		return new ResponseEntity<List<WelfareItemsDto>>(new ArrayList<>(), HttpStatus.OK);
	//}
  }

   @GetMapping("/getWelfareItemsDetails/{id}")
   public ResponseEntity<?> getWelfareItems(@PathVariable String id, Authentication authentication) {
	return welfareService.getWelfareItemsDetails(id);
    }


    @DeleteMapping("/deleteWelfareItems/{id}")
   public ResponseEntity<?> deleteWelfareItems(@PathVariable String id, Authentication authentication) {
	return welfareService.deleteWelfareItemsDetails(id);
  }
 }
