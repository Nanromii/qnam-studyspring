package vn.qnam.service;

import jakarta.mail.MessagingException;
import org.springframework.data.domain.Pageable;
import vn.qnam.dto.reponse.PageResponse;
import vn.qnam.dto.reponse.UserDetailResponse;
import vn.qnam.dto.request.UserRequestDTO;
import vn.qnam.util.UserStatus;

import java.io.UnsupportedEncodingException;

public interface UserService {
    long addUser(UserRequestDTO userRequestDTO) throws MessagingException, UnsupportedEncodingException;
    void updateUser(long userId, UserRequestDTO requestDTO);
    void deleteUser(long userId);
    void changeUser(long userId, UserStatus userStatus);
    UserDetailResponse getMyInfo();
    UserDetailResponse getUser(long userId);
    PageResponse<?> getAllUser(int pageNo, int pageSize, String... sorts);
    PageResponse<?> getUsersByFiltering(int pageNo, int pageSize, String search, String sortBy);
    PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String score, String... search);
    PageResponse<?> searchWithSpecification(Pageable pageable, String[] user, String[] score);
    void confirmUser(int userId, String secretCode);
}
