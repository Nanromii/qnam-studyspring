package vn.qnam.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.qnam.dto.reponse.PermissionResponse;
import vn.qnam.dto.reponse.ResponseData;
import vn.qnam.dto.request.PermissionDTO;
import vn.qnam.service.PermissionService;

import java.util.List;

@RestController
@RequestMapping("/permission")
@Slf4j
@Tag(name = "Permission Controller")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping("/")
    private ResponseData<PermissionResponse> create(@RequestBody PermissionDTO permissionDTO) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Created permission successfully.",
                permissionService.createPermission(permissionDTO));
    }

    @GetMapping("/get-all-permissions")
    private ResponseData<List<PermissionResponse>> getAll() {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Get all permissions successfully.",
                permissionService.getAll());
    }

    @DeleteMapping("/{permission}")
    private ResponseData<String> delete(@PathVariable String permission) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Deleted permission successfully.",
                permissionService.delete(permission));
    }
}
