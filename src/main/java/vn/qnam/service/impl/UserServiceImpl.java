package vn.qnam.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.qnam.dto.reponse.PageResponse;
import vn.qnam.dto.reponse.PermissionResponse;
import vn.qnam.dto.reponse.RoleResponse;
import vn.qnam.dto.reponse.UserDetailResponse;
import vn.qnam.dto.request.ScoreDTO;
import vn.qnam.dto.request.UserRequestDTO;
import vn.qnam.exception.ResourceNotFoundException;
import vn.qnam.model.Role;
import vn.qnam.model.Score;
import vn.qnam.model.User;
import vn.qnam.repository.FilterRepository;
import vn.qnam.repository.UserRepository;
import vn.qnam.repository.specification.UserSpecificationBuilder;
import vn.qnam.service.UserService;
import vn.qnam.util.UserStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final FilterRepository filterRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public long addUser(UserRequestDTO userRequestDTO) {
        User user = User.builder()
                .firstName(userRequestDTO.getFirstName())
                .lastName(userRequestDTO.getLastName())
                .dateOfBirth(userRequestDTO.getDateOfBirth())
                .phone(userRequestDTO.getPhone())
                .email(userRequestDTO.getEmail())
                .gender(userRequestDTO.getGender())
                .userName(userRequestDTO.getUsername())
                .password(passwordEncoder.encode(userRequestDTO.getPassword()))
                .status(userRequestDTO.getStatus())
                .role(Role.builder().name(userRequestDTO.getRole()).build())
                .build();
        userRequestDTO.getScore().forEach(a ->
                user.saveScore(Score.builder().scoreValue(a.getScore()).build()));
        userRepository.save(user);
        log.info("User has saved!");
        return user.getId();
    }


    @Override
    public void updateUser(long userId, UserRequestDTO requestDTO) {
        User user = getUserById(userId);
        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        user.setDateOfBirth(requestDTO.getDateOfBirth());
        user.setPhone(requestDTO.getPhone());
        user.setEmail(requestDTO.getEmail());
        user.setGender(requestDTO.getGender());
        user.setUserName(requestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setStatus(requestDTO.getStatus());
        user.setRole(Role.builder().name(requestDTO.getRole()).build());
        user.setScoresList(convertToScore(requestDTO.getScore(), user));
        userRepository.save(user);
        log.info("User has updated successfully, userId={}", userId);
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
        log.info("User deleted successfully, userId={}", userId);
    }

    @Override
    public void changeUser(long userId, UserStatus userStatus) {
        User user = getUserById(userId);
        user.setStatus(userStatus);
        userRepository.save(user);
        log.info("User status has changed successfully, userId={}", userId);
    }

    @Transactional
    @Override
    public UserDetailResponse getMyInfo() {
        SecurityContext context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findUserByUserName(name)
                .orElseThrow(() -> new ResourceNotFoundException("User with userName ={" + name + "} not exists"));
        return UserDetailResponse.builder()
                .userName(user.getUserName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(RoleResponse.builder()
                        .name(user.getRole().getName())
                        .description(user.getRole().getDescription())
                        .permissions(user.getRole().getPermissions().stream()
                                        .map(a -> PermissionResponse.builder()
                                                .name(a.getName())
                                                .description(a.getDescription())
                                                .build())
                                        .collect(Collectors.toSet()))
                        .build())
                .build();
    }

    @Transactional
    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .userName(user.getUserName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(RoleResponse.builder()
                        .name(user.getRole().getName())
                        .description(user.getRole().getDescription())
                        .permissions(user.getRole().getPermissions().stream()
                                .map(a -> PermissionResponse.builder()
                                        .name(a.getName())
                                        .description(a.getDescription())
                                        .build())
                                .collect(Collectors.toSet()))
                        .build())
                .build();
    }

    @Override
    public PageResponse<?> getAllUser(int pageNo, int pageSize, String... sorts) {
        if (pageNo > 0) --pageNo;
        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sorts) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }
                if (matcher.group(3).equalsIgnoreCase("desc")) {
                    orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(orders));
        Page<User> allUsers = userRepository.findAll(pageable);
        List<UserDetailResponse> userDetailResponseList = allUsers.stream().map(user -> UserDetailResponse.builder()
                .userName(user.getUserName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(RoleResponse.builder()
                        .name(user.getRole().getName())
                        .description(user.getRole().getDescription())
                        .permissions(user.getRole().getPermissions().stream()
                                .map(a -> PermissionResponse.builder()
                                        .name(a.getName())
                                        .description(a.getDescription())
                                        .build())
                                .collect(Collectors.toSet()))
                        .build())
                .build()).toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(allUsers.getTotalPages())
                .items(userDetailResponseList)
                .build();
    }

    @Override
    public PageResponse<?> getUsersByFiltering(int pageNo, int pageSize, String search, String sortBy) {
        return filterRepository.getUsersByFiltering(pageNo, pageSize, search, sortBy);
    }

    @Override
    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String score, String... search) {
        return filterRepository.getUsersByCriteria(pageNo, pageSize, sortBy, score, search);
    }

    @Override
    public PageResponse<?> searchWithSpecification(Pageable pageable, String[] user, String[] score) {
        if (user != null && score != null) {
            return filterRepository.getUsersJoinedScores(pageable, user, score);
        } else if (user != null) {
            UserSpecificationBuilder builder = new UserSpecificationBuilder();
            for (String u : user) {
                Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
                Matcher matcher = pattern.matcher(u);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            return PageResponse.builder()
                    .pageNo(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalPage(0)
                    .items(userRepository.findAll(builder.build()))
                    .build();
        }
        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(0)
                .items(userRepository.findAll(pageable).stream().toList())
                .build();
    }

    private List<Score> convertToScore(List<ScoreDTO> scoreDTOList, User user) {
        List<Score> scoreList = new ArrayList<>();
        scoreDTOList.forEach(a -> {
            Score score = Score.builder()
                    .scoreValue(a.getScore())
                    .build();
            score.setUser(user);
            scoreList.add(score);
        });
        return scoreList;
    }


    private User getUserById(long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with userId={" + userId + "} not exists"));
    }
}
