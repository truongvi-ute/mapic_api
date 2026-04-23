package com.mapic.backend.config;

import com.mapic.backend.entity.*;
import com.mapic.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
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
    private final UserStatusRepository userStatusRepository;
    private final NotificationRepository notificationRepository;
    private final AdminRepository adminRepository;
    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Seed admin first
        seedAdmin();
        
        // Clean up mock avatars from previous seeds
        log.info("Cleaning up mock avatar URLs...");
        List<UserProfile> profiles = userProfileRepository.findAll();
        for (UserProfile profile : profiles) {
            if (profile.getAvatarUrl() != null && profile.getAvatarUrl().contains("pravatar")) {
                log.info("Removing mock avatar for user: {}", profile.getUser().getUsername());
                profile.setAvatarUrl(null);
                userProfileRepository.save(profile);
            }
        }
        
        if (userRepository.count() >= 5) {
            log.info("Database already seeded with enough users. Skipping user data seed.");
            
            // But still seed reports if needed
            if (reportRepository.count() == 0) {
                log.info("No reports found. Seeding sample reports...");
                List<User> users = userRepository.findAll();
                List<Moment> moments = momentRepository.findAll();
                createSampleReports(users, moments);
            } else {
                log.info("Reports already exist ({} reports). Skipping report seed.", reportRepository.count());
            }
            
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
        // Base GPS coordinates (around Ho Chi Minh City)
        double[] baseLats = {10.7361, 10.7365, 10.7370, 10.7355, 10.7380};
        double[] baseLngs = {106.9487, 106.9490, 106.9485, 106.9495, 106.9480};
        
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

            // Create UserProfile with avatar (only if not exists)
            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .avatarUrl(null) // Don't set mock avatar, let users upload their own
                    .bio("Xin chào, tôi là " + fullNames[i] + ". Rất vui được làm quen với mọi người trên MAPIC!")
                    .gender(genders[i])
                    .dateOfBirth(LocalDate.now().minusYears(20 + i))
                    .location(provinces.get(random.nextInt(provinces.size())).getName())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userProfileRepository.save(profile);
            
            // Create UserStatus with GPS coordinates
            UserStatus status = UserStatus.builder()
                    .user(user)
                    .lastLat(baseLats[i])
                    .lastLng(baseLngs[i])
                    .lastSeenAt(LocalDateTime.now())
                    .isSharingLocation(true)
                    .batteryLevel(80 + random.nextInt(20))
                    .build();
            userStatusRepository.save(status);
            
            log.info("Created user: {} with GPS: {}, {}", user.getUsername(), baseLats[i], baseLngs[i]);
        }

        // 2. Create Friendships and Friend Requests
        // user1 is friends with all other users (for map testing)
        log.info("Creating friendships for map feature...");
        createFriendship(users.get(0), users.get(1)); // user1 <-> user2
        createFriendship(users.get(0), users.get(2)); // user1 <-> user3
        createFriendship(users.get(0), users.get(3)); // user1 <-> user4
        createFriendship(users.get(0), users.get(4)); // user1 <-> user5
        
        // Also create some friendships between other users
        createFriendship(users.get(1), users.get(2)); // user2 <-> user3
        createFriendship(users.get(2), users.get(3)); // user3 <-> user4

        // Pending requests
        createFriendRequest(users.get(4), users.get(3)); // user5 -> user4
        
        log.info("Created {} friendships", friendshipRepository.count() / 2);

        // 3. Create Moments
        String[] categories = {"LANDSCAPE", "FOOD", "OTHER", "ARCHITECTURE", "PEOPLE"};
        
        List<String> seedImagesList = new ArrayList<>();
        File uploadDir = new File("uploads/moments");
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().startsWith(".")) {
                        seedImagesList.add(file.getName());
                    }
                }
            }
        }
        
        if (seedImagesList.isEmpty()) {
            log.warn("No images found in uploads/moments. Using default dummy names.");
            seedImagesList.add("default.jpg");
        }
        
        String[] seedImages = seedImagesList.toArray(new String[0]);

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

        // 5. Create Sample Notifications
        createSampleNotifications(users, allMoments);
        
        // 6. Create Sample Reports
        createSampleReports(users, allMoments);

        log.info("Data seeding completed successfully!");
    }

    private void createSampleNotifications(List<User> users, List<Moment> moments) {
        if (users.size() < 5 || moments.isEmpty()) return;
        
        User mainUser = users.get(0); // user1
        
        // 1. Friend request
        createNotification(users.get(3), mainUser, NotificationType.FRIEND_REQUEST, "FRIENDSHIP", 1L);
        
        // 2. Moment reaction
        createNotification(users.get(1), mainUser, NotificationType.MOMENT_REACTION, "MOMENT", moments.get(0).getId());
        
        // 3. Moment comment
        createNotification(users.get(2), mainUser, NotificationType.MOMENT_COMMENT, "MOMENT", moments.get(0).getId());
        
        // 4. New message
        createNotification(users.get(4), mainUser, NotificationType.NEW_MESSAGE, "MESSAGE", 1L);
        
        log.info("Seeded 4 mock notifications for user: {}", mainUser.getUsername());
    }

    private void createNotification(User actor, User recipient, NotificationType type, String targetType, Long targetId) {
        Notification notification = Notification.builder()
                .actor(actor)
                .recipient(recipient)
                .type(type)
                .targetType(targetType)
                .targetId(targetId)
                .isRead(false)
                .createdAt(LocalDateTime.now().minusMinutes(new Random().nextInt(60)))
                .build();
        notificationRepository.save(notification);
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

    private void seedAdmin() {
        // Check if admin already exists
        if (adminRepository.existsByUsername("admin")) {
            log.info("Admin user already exists. Skipping admin seed.");
            return;
        }

        log.info("Creating default admin user...");
        
        Admin admin = Admin.builder()
                .name("Super Administrator")
                .email("admin@mapic.com")
                .role(AdminRole.SUPER_ADMIN)
                .isActive(true)
                .build();
        
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setStatus(AccountStatus.ACTIVE);
        
        adminRepository.save(admin);
        
        log.info("✅ Admin user created successfully!");
        log.info("   Username: admin");
        log.info("   Password: 123456");
        log.info("   Role: SUPER_ADMIN");
    }
    
    private void createSampleReports(List<User> users, List<Moment> moments) {
        if (users.size() < 3 || moments.isEmpty()) {
            log.warn("Not enough users or moments to create sample reports");
            return;
        }
        
        // Clear existing reports to avoid duplicates
        reportRepository.deleteAll();
        log.info("Cleared existing reports");
        
        List<Comment> comments = commentRepository.findAll();
        if (comments.isEmpty()) {
            log.warn("No comments found to create reports");
            return;
        }
        
        // Report 1: Nội dung sai lệch (FAKE_NEWS) - Moment
        if (moments.size() > 0) {
            Report report1 = Report.builder()
                    .reporter(users.get(1))
                    .targetId(moments.get(0).getId())
                    .targetType(ReportTargetType.MOMENT)
                    .reason("Bài viết chứa thông tin sai lệch về địa điểm")
                    .reasonCategory(ReportReasonCategory.FAKE_NEWS)
                    .status(ReportStatus.PENDING)
                    .build();
            reportRepository.save(report1);
            log.info("Created FAKE_NEWS report for moment {}", moments.get(0).getId());
        }
        
        // Report 2: Vi phạm tiêu chuẩn cộng đồng (INAPPROPRIATE) - Comment
        if (comments.size() > 0) {
            Report report2 = Report.builder()
                    .reporter(users.get(2))
                    .targetId(comments.get(0).getId())
                    .targetType(ReportTargetType.COMMENT)
                    .reason("Bình luận có nội dung không phù hợp với cộng đồng")
                    .reasonCategory(ReportReasonCategory.INAPPROPRIATE)
                    .status(ReportStatus.PENDING)
                    .build();
            reportRepository.save(report2);
            log.info("Created INAPPROPRIATE report for comment {}", comments.get(0).getId());
        }
        
        // Report 3: Ngôn từ thù ghét (HATE_SPEECH) - Moment
        if (moments.size() > 1) {
            Report report3 = Report.builder()
                    .reporter(users.get(0))
                    .targetId(moments.get(1).getId())
                    .targetType(ReportTargetType.MOMENT)
                    .reason("Bài viết có ngôn từ phân biệt đối xử")
                    .reasonCategory(ReportReasonCategory.HATE_SPEECH)
                    .status(ReportStatus.PENDING)
                    .build();
            reportRepository.save(report3);
            log.info("Created HATE_SPEECH report for moment {}", moments.get(1).getId());
        }
        
        // Report 4: Khác (OTHER) - Comment
        if (comments.size() > 1) {
            Report report4 = Report.builder()
                    .reporter(users.get(1))
                    .targetId(comments.get(1).getId())
                    .targetType(ReportTargetType.COMMENT)
                    .reason("Nội dung không phù hợp, cần xem xét")
                    .reasonCategory(ReportReasonCategory.OTHER)
                    .status(ReportStatus.RESOLVED)
                    .build();
            reportRepository.save(report4);
            log.info("Created OTHER report for comment {}", comments.get(1).getId());
        }
        
        log.info("✅ Created 4 sample reports (FAKE_NEWS, INAPPROPRIATE, HATE_SPEECH, OTHER)");
    }
}
