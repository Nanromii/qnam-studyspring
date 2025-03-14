package vn.qnam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.qnam.dto.reponse.PermissionResponse;
import vn.qnam.dto.reponse.RoleResponse;
import vn.qnam.dto.request.RoleDTO;
import vn.qnam.exception.ResourceNotFoundException;
import vn.qnam.model.Permission;
import vn.qnam.model.Role;
import vn.qnam.repository.PermissionRepository;
import vn.qnam.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleResponse createRole(RoleDTO request) {
        Role role = convertToRole(request);
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);
        return convertToResponse(role);
    }

    private RoleResponse convertToResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions()
                .stream()
                .map(permission -> new PermissionResponse(permission.getName(), permission.getDescription()))
                .collect(Collectors.toSet());
        return new RoleResponse(role.getName(), role.getDescription(), permissionResponses);
    }


    private Role convertToRole(RoleDTO request) {
        Set<Permission> permissions = request.getPermissions()
                .stream()
                .map(permissionDTO -> new Permission(permissionDTO, "DEFAULT DESCRIPTION."))
                .collect(Collectors.toSet());

        return new Role(request.getName(), request.getDescription(), permissions);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream().map(this::convertToResponse).toList();
    }

    public String delete(String roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role not found: " + roleId);
        }
        roleRepository.deleteById(roleId);
        return "Deleted role successfully.";
    }

}
