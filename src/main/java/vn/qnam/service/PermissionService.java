package vn.qnam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.qnam.dto.reponse.PermissionResponse;
import vn.qnam.dto.request.PermissionDTO;
import vn.qnam.exception.ResourceNotFoundException;
import vn.qnam.model.Permission;
import vn.qnam.repository.PermissionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionResponse createPermission(PermissionDTO request) {
        Permission permission = convertToPermission(request);
        permissionRepository.save(permission);
        return convertToResponse(permission);
    }

    public PermissionResponse convertToResponse(Permission permission) {
        return new PermissionResponse(permission.getName(), permission.getDescription());
    }

    private Permission convertToPermission(PermissionDTO request) {
        return new Permission(request.getName(), request.getDescription());
    }

    public List<PermissionResponse> getAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream().map(this::convertToResponse).toList();
    }

    public String delete(String permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new ResourceNotFoundException("Permission not found: " + permissionId);
        }
        permissionRepository.deleteById(permissionId);
        return "Deleted permission successfully.";
    }

}
