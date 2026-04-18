1. Hệ thống Tài khoản & Định danh (Identity)

Account (Abstract): id, username, password, status (AccountStatus), createdAt, updatedAt

User (Extends Account): name, email, phone, failedLoginAttempts, lockoutUntil

Admin (Extends Account): (Kế thừa từ Account)

Moderator (Extends Account): (Kế thừa từ Account)

UserProfile: id, user (OneToOne), bio, avatarUrl, coverImageUrl, gender, dateOfBirth, location, updatedAt

UserStatus: id, user (OneToOne), lastLat, lastLng, lastSeenAt, batteryLevel, isSharingLocation, statusMessage

2. Nội dung & Tương tác (Content & Interaction)

Moment: id, content, author (User), location (Location), province (Province), status, createdAt, updatedAt

MomentMedia: id, moment (ManyToOne), mediaUrl, publicId (Cloudinary), mediaType (IMAGE/VIDEO), sortOrder

Album: id, title, description, coverImageUrl, author (User), isPrivate, createdAt

AlbumItem: id, album (ManyToOne), moment (ManyToOne), sortOrder (Phục vụ điều hướng), addedAt

Comment: id, content, author (User), moment (ManyToOne), parentComment (ManyToOne), createdAt

Reaction: id, user (User), moment (ManyToOne), type (ReactionType)

Hashtag: id, name, createdAt

MomentHashtag: id, moment (ManyToOne), hashtag (ManyToOne) (Bảng trung gian để tối ưu truy vấn)

3. Mạng xã hội & Địa lý (Social & Geo)

Friendship: id, user (User), friend (User), status (FriendshipStatus), requestedAt, updatedBy (User - để biết ai là người block)

Province: id, name, code, region

Location: id, latitude, longitude, address, name

4. Tin nhắn & Hội thoại (Communication)

Conversation: id, title, isGroup, creator (User), lastMessage (Message), createdAt

Participant: id, conversation (ManyToOne), user (User), role, joinedAt

Message (Abstract): id, conversation (ManyToOne), sender (User), type (MessageType), createdAt

TextMessage (Extends Message): content

AttachmentMessage (Extends Message): attachmentUrl, publicId (Cloudinary), attachmentType (IMAGE/VIDEO/FILE)

CallMessage (Extends Message): callStatus, duration

ShareMessage (Extends Message): targetId, shareType (MOMENT/ALBUM)

MessageReaction: id, message (ManyToOne), user (User), emoji

5. Thông báo & An toàn (Notification & Safety)

Notification: id, actor (User), recipient (User), targetId, targetType, type (NotificationType), isRead, createdAt

SOSAlert: id, sender (User), latitude, longitude, status, createdAt

SOSRecipient: id, sosAlert (ManyToOne), recipient (User), isNotified, notifiedAt

Report: id, reporter (User), targetId, targetType (MOMENT/COMMENT/USER), reason, status (PENDING/RESOLVED), createdAt

6. Các lớp hỗ trợ (Enums & Constants)

AccountStatus: ACTIVE, BLOCK

Gender: MALE, FEMALE, OTHER

MediaType: IMAGE, VIDEO

ReactionType: LIKE, HEART, HAHA, SAD, ANGRY

FriendshipStatus: PENDING, ACCEPTED, BLOCKED

MessageType: TEXT, ATTACHMENT, CALL, SHARE

NotificationType: NEW_MESSAGE, FRIEND_REQUEST, MOMENT_REACTION, MOMENT_TAG, SOS_ALERT

sử dụng @MappedSuperclass (không lưu bảng cha, đẩy thuộc tính xuống bảng con)