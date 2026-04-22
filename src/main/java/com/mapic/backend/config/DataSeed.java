package com.mapic.backend.config;

import com.mapic.backend.entity.*;
import com.mapic.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeed implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MomentRepository momentRepository;
    private final MomentMediaRepository momentMediaRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final ProvinceRepository provinceRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() >= 5) {
            log.info("Database already seeded with enough users. Skipping data seed.");
            return;
        }

        log.info("Starting data seeding...");

        List<Province> provinces = provinceRepository.findAll();
        if (provinces.isEmpty()) {
            log.warn("No provinces found in database. Please seed provinces first.");
            return;
        }

        // 1. Create 5 Users and Profiles
        String[] fullNames = {
                "Nguyễn Văn Nam", 
                "Trần Thị Lan", 
                "Lê Hoàng Anh", 
                "Phạm Minh Đức", 
                "Hoàng Thu Thủy"
        };
        Gender[] genders = {Gender.MALE, Gender.FEMALE, Gender.MALE, Gender.MALE, Gender.FEMALE};
        String password = passwordEncoder.encode("123456");

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int userNum = i + 1;
            User user = User.builder()
                    .name(fullNames[i])
                    .email("user" + userNum + "@mapic.com")
                    .phone("090000000" + userNum)
                    .failedLoginAttempts(0)
                    .build();
            user.setUsername("user" + userNum);
            user.setPassword(password);
            user.setStatus(AccountStatus.ACTIVE);
            
            user = userRepository.save(user);
            users.add(user);

            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .bio("Xin chào, tôi là " + fullNames[i] + ". Rất vui được làm quen với mọi người trên MAPIC!")
                    .gender(genders[i])
                    .dateOfBirth(LocalDate.now().minusYears(20 + i))
                    .location(provinces.get(random.nextInt(provinces.size())).getName())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userProfileRepository.save(profile);
        }

        // 2. Create Friendships and Friend Requests
        // user1 is friends with user2 and user3
        createFriendship(users.get(0), users.get(1));
        createFriendship(users.get(0), users.get(2));

        // Pending requests
        createFriendRequest(users.get(3), users.get(0)); // user4 -> user1
        createFriendRequest(users.get(4), users.get(1)); // user5 -> user2

        // 3. Create Moments
        String[] categories = {"LANDSCAPE", "FOOD", "OTHER", "ARCHITECTURE", "PEOPLE"};
        String[] seedImages = {
                "seed-VN-HN-nature-002.jpg", "seed-VN-HN-nature-004.jpg", "seed-VN-SG-architecture-005.jpg",
                "seed-VN-SG-architecture-006.jpg", "seed-VN-DN-landscape-010.jpg", "seed-VN-DN-nature-011.jpg",
                "seed-VN-HP-food-015.jpg", "seed-VN-HP-nature-013.jpg", "seed-VN-LD-urban-081.jpg",
                "seed-VN-LD-nature-084.jpg", "seed-VN-HA-landscape-062.jpg", "seed-VN-HA-nature-063.jpg",
                "seed-VN-BTH-nature-039.jpg", "seed-VN-BTH-food-040.jpg", "seed-VN-CT-landscape-016.jpg"
        };

        List<Moment> allMoments = new ArrayList<>();
        int imgIdx = 0;
        for (User author : users) {
            for (int j = 0; j < 3; j++) {
                Province province = provinces.get(random.nextInt(provinces.size()));
                
                // Create Location
                Location location = Location.builder()
                        .latitude(10.0 + random.nextDouble() * 10.0) // Sample VN coords
                        .longitude(105.0 + random.nextDouble() * 3.0)
                        .address("Địa điểm tại " + province.getName())
                        .name(province.getName())
                        .build();
                location = locationRepository.save(location);

                Moment moment = Moment.builder()
                        .author(author)
                        .content("Khoảnh khắc tuyệt vời của " + author.getName() + " tại " + province.getName() + ". #MAPIC #Vietnam")
                        .location(location)
                        .province(province)
                        .category(categories[random.nextInt(categories.length)])
                        .isPublic(true)
                        .status("ACTIVE")
                        .build();
                moment = momentRepository.save(moment);
                allMoments.add(moment);

                // Add 1-3 images
                int numImgs = random.nextInt(3) + 1;
                for (int m = 0; m < numImgs; m++) {
                    String imgFile = seedImages[imgIdx % seedImages.length];
                    imgIdx++;
                    MomentMedia media = MomentMedia.builder()
                            .moment(moment)
                            .mediaUrl(imgFile)
                            .mediaType(MediaType.IMAGE)
                            .sortOrder(m)
                            .build();
                    momentMediaRepository.save(media);
                }
            }
        }

        // 4. Create Reactions and Comments
        for (Moment m : allMoments) {
            // Each moment gets 1-3 reactions
            int numReactions = random.nextInt(3) + 1;
            for (int r = 0; r < numReactions; r++) {
                User reactor = users.get(random.nextInt(users.size()));
                if (!reactionRepository.existsByUserAndMoment(reactor, m)) {
                    Reaction reaction = Reaction.builder()
                            .user(reactor)
                            .moment(m)
                            .type(r % 2 == 0 ? ReactionType.HEART : ReactionType.LIKE)
                            .createdAt(LocalDateTime.now())
                            .build();
                    reactionRepository.save(reaction);
                }
            }

            // Each moment gets 1-2 comments
            int numComments = random.nextInt(2) + 1;
            String[] commentTexts = {"Tuyệt quá!", "Đẹp thật đấy ❤️", "Nhìn thích thế!", "Chỗ này ở đâu vậy bạn?", "Wow!"};
            for (int c = 0; c < numComments; c++) {
                User commenter = users.get(random.nextInt(users.size()));
                Comment comment = Comment.builder()
                        .author(commenter)
                        .moment(m)
                        .content(commentTexts[random.nextInt(commentTexts.length)])
                        .createdAt(LocalDateTime.now())
                        .build();
                commentRepository.save(comment);
            }
        }

        log.info("Data seeding completed successfully!");
    }

    private void createFriendship(User u1, User u2) {
        Friendship f1 = Friendship.builder()
                .user1(u1)
                .user2(u2)
                .build();
        Friendship f2 = Friendship.builder()
                .user1(u2)
                .user2(u1)
                .build();
        friendshipRepository.save(f1);
        friendshipRepository.save(f2);
    }

    private void createFriendRequest(User sender, User receiver) {
        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        friendRequestRepository.save(request);
    }
}
