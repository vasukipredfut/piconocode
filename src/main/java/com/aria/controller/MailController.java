package com.aria.controller;

import com.aria.config.ResponseMessage;
import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.MailService;
import com.aria.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MailController {


    @Autowired
    MailService mailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MailUtil mailUtil;


    @PostMapping("/sentMail")
    public ResponseEntity<ResponseMessage> sentMail(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("type") String type,
                                                    @RequestParam("typeId") Long typeId,
                                                    HttpServletRequest request, Authentication authentication) {


        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        String message = "";

        if (file == null || file.isEmpty()) {
            message = "No file uploaded or file is empty!";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
        }

        if (mailUtil.hasHTMLFormat(file)) {

            try {
                return mailService.sentMail(file.getInputStream(), type, typeId, request);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        message = "Please upload a HTML file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }


}
