package vn.qnam.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.qnam.dto.reponse.ResponseData;
import vn.qnam.dto.reponse.RoleResponse;
import vn.qnam.dto.request.RoleDTO;
import vn.qnam.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/role")
@Slf4j
@Tag(name = "Role Controller")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/")
    private ResponseData<RoleResponse> create(@RequestBody RoleDTO roleDTO) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Created permission successfully.",
                roleService.createRole(roleDTO));
    }

    @GetMapping("/get-all-roles")
    private ResponseData<List<RoleResponse>> getAll() {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Get all permissions successfully.",
                roleService.getAll());
    }

    @DeleteMapping("/{role}")
    private ResponseData<String> delete(@PathVariable String role) {
        return new ResponseData<>(HttpStatus.OK.value(),
                "Deleted role successfully.",
                roleService.delete(role));
    }
}
