package vn.qnam.servie.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.qnam.dto.reponse.PageResponse;
import vn.qnam.dto.reponse.UserDetailResponse;
import vn.qnam.dto.request.ScoreDTO;
import vn.qnam.dto.request.UserRequestDTO;
import vn.qnam.exception.ResourceNotFoundException;
import vn.qnam.model.Score;
import vn.qnam.model.User;
import vn.qnam.repository.FilterRepository;
import vn.qnam.repository.UserRepository;
import vn.qnam.servie.UserService;
import vn.qnam.util.Type;
import vn.qnam.util.UserStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final FilterRepository filterRepository;

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
                .password(userRequestDTO.getPassword())
                .status(userRequestDTO.getStatus())
                .type(Type.valueOf(userRequestDTO.getType().toUpperCase()))
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
        user.setPassword(requestDTO.getPassword());
        user.setStatus(requestDTO.getStatus());
        user.setType(Type.valueOf(requestDTO.getType().toUpperCase()));
        user.setScoresList(convertToScore(requestDTO.getScore()));
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

    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
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
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
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

    private List<Score> convertToScore(List<ScoreDTO> scoreDTOList) {
        List<Score> scoreList = new ArrayList<>();
        scoreDTOList.forEach(a ->
                scoreList.add(Score.builder()
                        .scoreValue(a.getScore())
                        .build())
        );
        return scoreList;
    }

    private User getUserById(long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with userId={" + userId + "} not exists"));
    }
}
