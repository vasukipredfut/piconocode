package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.ExistingPageDto;
import com.aria.dto.FieldsInExistingPageDto;
import com.aria.dto.PageDto;
import com.aria.model.User;
import com.aria.service.PageService;
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
public class PageController {


    @Autowired
    PageService pageService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/savePage")
    public ResponseEntity<?> savePage(@RequestBody PageDto pageDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return pageService.savePage(pageDto, user.get());
    }

    @PutMapping("/updatePage")
    public ResponseEntity<?> updatePage(@RequestBody PageDto pageDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return pageService.updatePage(pageDto, user.get());
    }

    @GetMapping("/getPageList")
    public ResponseEntity<?> listPage(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return pageService.listAllPage(pageable);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getPageDetails/{id}")
    public ResponseEntity<?> getPageDetails(@PathVariable String id, Authentication authentication) {

        return pageService.getPageDetails(id);
    }

    @DeleteMapping("/deletePageDetails/{id}")
    public ResponseEntity<?> deleteComponentDetails(@PathVariable String id, Authentication authentication) {

        return pageService.deletePage(id);
    }
    
    
    //add fields for existing page
    
    @GetMapping("/pages")
    public ResponseEntity<?> getAllPages(@RequestParam(value = "pageName", required = false) String pageName){
    	return pageService.getAllPages(pageName);
    }
    
    @GetMapping("/fields/{pageName}")
    public ResponseEntity<?> getAllFieldsByPage(@PathVariable String pageName) throws ClassNotFoundException{
    	return pageService.getAllFieldsByPage(pageName);
    }
    
    @GetMapping("/tables")
    public ResponseEntity<?> getAllTables(@RequestParam(value = "table", required = false) String table){
    	return pageService.getAllTables(table);
    }
    
    @PostMapping("/fields")
    public ResponseEntity<?> saveFieldsInExistingPage(@RequestBody ExistingPageDto pageDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return pageService.saveFieldsInExistingPage(pageDto, user.get());
    }
    
    @GetMapping("/fields")
    public ResponseEntity<?> getAllFieldsTables(@RequestParam(value = "table", required = false) String table){
    	return pageService.getAllFieldsTables(table);
    }
    
    @GetMapping("/screens")
    public ResponseEntity<?> getAllScreens(@RequestParam(value = "screenName", required = false) String screenName){
    	return pageService.getAllScreens(screenName);
    }
    
    
    


}
