package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.OrderDto;
import com.aria.dto.PageDto;
import com.aria.model.User;
import com.aria.service.OrderService;

@RestController
@CrossOrigin("*")
@RequestMapping("/api")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/order/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return orderService.createOrder(orderDto, user.get());
    }

    @GetMapping("/pieceCount")
    public ResponseEntity<?> getPieceCount(@RequestParam(value = "productId") Long productId,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return orderService.getPieceQtyList(productId);
        } else {
            return new ResponseEntity<List<Object>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/order/getAllOrdersByStatus")
    public ResponseEntity<?> getAllOrdersByStatus(@RequestParam(value = "firmId", required = false) Long firmId,
                                                  @RequestParam(value = "branchId", required = false) Long branchId,
                                                  @RequestParam(value = "orderNumber", required = false) Long orderNumber,
                                                  @RequestParam(value = "billDate", required = false) String billDate,
                                                  @RequestParam(value = "vendorName", required = false) String vendorName,
                                                  @RequestParam(value = "billNo", required = false) String billNo,
                                                  @RequestParam("status") String status,
                                                  @RequestParam("type") Long type, Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return orderService.getAllOrdersByStatus(firmId, branchId, orderNumber, billNo, billDate, vendorName, status, type, pageable);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/order/changeStatusOfOrder")
    public ResponseEntity<?> changeStatusOfOrder(@RequestParam Long orderId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        try {
            return orderService.changeStatusOfOrder(orderId);
        } catch (Exception e) {
            return new ResponseEntity<Object>("Something Went Wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/order/getAllAgainstOrder")
    public ResponseEntity<?> getAllNewOrder(@RequestParam(value = "firmId", required = false) Long firmId,
                                            @RequestParam(value = "branchId", required = false) Long branchId,
                                            @RequestParam(value = "orderNumber", required = false) Long orderNumber,
                                            @RequestParam(value = "billDate", required = false) String billDate,
                                            @RequestParam(value = "vendorName", required = false) String vendorName,
                                            @RequestParam(value = "status", required = false) String status,
                                            Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return orderService.getAllAgainstOrder(firmId, branchId, orderNumber, billDate, vendorName, status, pageable);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @GetMapping("/order/getAllorderAgainstBill")
    public ResponseEntity<?> getAllorderAgainstBill(@RequestParam(value = "firmId", required = false) Long firmId,
                                                    @RequestParam(value = "branchId", required = false) Long branchId,
                                                    @RequestParam(value = "billNumber", required = false) String billNumber,
                                                    @RequestParam(value = "billDate", required = false) String billDate,
                                                    @RequestParam(value = "vendorName", required = false) String vendorName,
                                                    @RequestParam(value = "status", required = false) String status, Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (user.get() != null) {
            return orderService.getAllorderAgainstBill(firmId, branchId, billNumber, billDate, vendorName, status, pageable);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @GetMapping("/order/getOrderDetailsById")
    public ResponseEntity<?> getOrderDetails(@RequestParam("orderId") Long orderId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return orderService.getOrderDetailsById(orderId);
    }

    @DeleteMapping("/order/deleteOrderDetails")
    public ResponseEntity<?> deleteOrderDetails(@RequestParam("orderId") Long orderId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return orderService.deleteOrderDetails(orderId);
    }

    @GetMapping("/order/getAllProductsAgainstOrder")
    public ResponseEntity<?> getAllProductsAgainstOrder(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return orderService.getAllProductsAgainstOrder();
    }

    @GetMapping("/order/getAllProductsAgainstBill")
    public ResponseEntity<?> getAllProductsAgainstBill(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return orderService.getAllProductsAgainstBill();
    }

}
