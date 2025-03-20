package vn.qnam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.qnam.configuration.Translator;
import vn.qnam.dto.reponse.PageResponse;
import vn.qnam.dto.reponse.ResponseData;
import vn.qnam.dto.reponse.ResponseError;
import vn.qnam.dto.reponse.UserDetailResponse;
import vn.qnam.dto.request.UserRequestDTO;
import vn.qnam.exception.ResourceNotFoundException;
import vn.qnam.service.UserService;
import vn.qnam.util.UserStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@Tag(name = "User Controller")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //@PostMapping("/")
    @Operation(method = "POST", summary = "Add User", description = "API create new user")
    @RequestMapping(method = RequestMethod.POST, path = "/")
    public ResponseData<Long> addUser(@Valid @RequestBody UserRequestDTO userDTO) {
        log.info("Request add user, {} {}", userDTO.getFirstName(), userDTO.getLastName());
        try {
            long userId = userService.addUser(userDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), userId);
        } catch (UnsupportedEncodingException | MessagingException e) {
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        } catch (Exception e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "User added fail");
        }
    }

    @Operation(summary = "Update User", description = "API update user")
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser(@Min(value = 1, message = "userId must be greater than 0") @PathVariable int userId, @Valid @RequestBody UserRequestDTO userDTO) {
        userService.updateUser(userId, userDTO);
        return new ResponseData<>(
                HttpStatus.ACCEPTED.value(),
                Translator.toLocale("user.update.success"));
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Change user status", description = "API change user status")
    public ResponseData<?> changeStatus(@Min(value = 1, message = "userId must be greater than 0") @PathVariable int userId, @RequestParam UserStatus status) {
        userService.changeUser(userId, status);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.changeStatus.success"));
    }

    @Operation(summary = "Delete user", description = "API delete user")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<?> deleteUser(@Min(value = 1, message = "userId must be greater than 0") @PathVariable int userId) {
        userService.deleteUser(userId);
        return new ResponseData<>(HttpStatus.OK.value(), Translator.toLocale("user.del.success"));
    }

    @GetMapping("/confirm/{userId}")
    @Operation(summary = "Confirm account", description = "API confirm user account")
    public ResponseData<?> confirmUser(@Min(value = 1, message = "userId must be greater than 0") @PathVariable int userId, @RequestParam String secretCode, HttpServletResponse response) throws IOException {
        log.info("Confirm user with userId={}, secretCode={}", userId, secretCode);
        try {
            userService.confirmUser(userId, secretCode);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.changeStatus.success"));
        } catch (Exception e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Confirmation was failure.");
        } finally {
            //chuyen huong toi log in
            //tam thoi se chuyen huong tam ra day
            response.sendRedirect("https://leetcode.com/u/_vnqnammm/");
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user", description = "API get user")
    //@PostAuthorize("hasRole('ADMIN')") //Sau khi vào method, lấy data, nếu kiểm tra mà role không thỏa mãn thì sẽ bị chặn và không trả về data

    /*Nếu userName của người lấy data chính là userName của data thì cho lấy, tức là chỉ được phép lấy data của chính mình
    ADMIN có thể lấy data của bất cứ user nào*/
    @PostAuthorize("returnObject.data.userName == authentication.name or hasRole('ADMIN')")
    public ResponseData<UserDetailResponse> getUser(@Min(value = 1, message = "userId must be greater than 0") @PathVariable int userId) {
        log.info("Request get user with userId={}", userId);
        try {
            UserDetailResponse userDetailResponse = userService.getUser(userId);
            return new ResponseData<>(HttpStatus.OK.value(),
                    Translator.toLocale("user.getUser.success"),
                    userDetailResponse);
        } catch (Exception e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Get user fail. " + e.getMessage());
        }
    }

    @GetMapping("/myinfo")
    public ResponseData<UserDetailResponse> getMyInfo() {
        log.info("Request get my info");
        try {
            UserDetailResponse userDetailResponse = userService.getMyInfo();
            return new ResponseData<>(HttpStatus.OK.value(),
                    Translator.toLocale("user.getUser.success"),
                    userDetailResponse);
        } catch (ResourceNotFoundException e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Get user fail. " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "Get list of user", description = "API get list user")
    @PreAuthorize("hasRole('ADMIN')") //Nếu không phải role ID thì user bị chặn trước khi vào method
    /*
    * Sau khi chinh sua thi hasRole('APPROVE POST') se khong goi duoc API getAllUSers boi vi
    * ban chat hasRole('APPROVE POST') = ROLE_APPROVE POST nhung trong Scope cua Jwt chi co APPROVE POST
    * muon su dung cac permission giong nhu APPROVE POST de truy cap API thi viec chung ta can lam la
    * them @PreAuthorize("hasAuthority('APPROVE POST')") vi doi khi nhieu Role khac nhau co the co cung 1 permission
    * vi du ADMIN co CREATE POST va USER cung co thi ta su dung hasAuthority se linh hoat hon la dung
    * hasRole('ADMIN') or hasRole('USER')
    */
    public ResponseData<PageResponse<?>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String... sorts) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("userName={}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
        log.info("Get all users from {} to {}", pageNo, pageSize);

        try {
            return new ResponseData<>(HttpStatus.OK.value(), Translator.toLocale("user.getListUser.success"),
                    userService.getAllUser(pageNo, pageSize, sorts));
        } catch (ResourceNotFoundException e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Get all users fail. " + e.getMessage());
        }
    }

    @GetMapping("/get-list-user-by-filtering")
    @Operation(summary = "Get users by filtering", description = "API get users by filtering")
    public ResponseData<PageResponse<?>> getUsersByFiltering(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy) {

        log.info("Get users by filtering");
        try {
            return new ResponseData<>(HttpStatus.OK.value(), Translator.toLocale("user.getListUser.success"), userService.getUsersByFiltering(pageNo, pageSize, search, sortBy));
        } catch (ResourceNotFoundException e) {
            log.info("error message={}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Get users by filtering fail: " + e.getMessage());
        }
    }

    @GetMapping("/get-list-user-use-advance-search-by-criteria")
    @Operation(summary = "Get users by filtering", description = "API get users use advance search by criteria")
    public ResponseData<PageResponse<?>> advanceSearchByCriteria(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(defaultValue = "", required = false) String sortBy,
            @RequestParam(defaultValue = "", required = false) String score,
            @RequestParam(defaultValue = "", required = false) String... search) {

        log.info("Get users use advance search by criteria");
        try {
            return new ResponseData<>(HttpStatus.OK.value(), Translator.toLocale("user.getListUser.success"),
                    userService.advanceSearchByCriteria(pageNo, pageSize, sortBy, score, search));
        } catch (ResourceNotFoundException e) {
            log.info("error message={}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Get users by filtering fail: " + e.getMessage());
        }
    }

    @GetMapping("/get-list-user-with-specification")
    @Operation(summary = "Get users with specification", description = "API get users with specification")
    public ResponseData<PageResponse<?>> searchWithSpecification(
            Pageable pageable,
            @RequestParam(required = false) String[] user,
            @RequestParam(required = false) String[] score){

        log.info("Get users with specification");
        return new ResponseData<>(HttpStatus.OK.value(), Translator.toLocale("user.getListUser.success"),
                userService.searchWithSpecification(pageable, user, score));
    }
}
