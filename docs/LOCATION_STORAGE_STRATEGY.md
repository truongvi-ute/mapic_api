# Chiến lược lưu trữ Location

## Vấn đề

Ban đầu, Moment entity có 4 foreign keys:
- `location_id` → bảng `locations` (latitude, longitude, address)
- `province_id` → bảng `provinces`
- `district_id` → bảng `districts`  
- `commune_id` → bảng `communes`

**Vấn đề**: Bảng provinces/districts/communes chưa có dữ liệu → luôn NULL

## Giải pháp hiện tại

### Cách 1: Lưu đơn giản (Đang dùng) ✅

Chỉ lưu vào bảng `locations`:
- `latitude`: Tọa độ vĩ độ
- `longitude`: Tọa độ kinh độ
- `address`: Địa chỉ đầy đủ (text)
- `name`: Tên location

**Ưu điểm**:
- Đơn giản, không cần seed data
- Linh hoạt, không bị ràng buộc với cấu trúc hành chính
- Đủ để hiển thị trên map và tìm kiếm

**Nhược điểm**:
- Không thể query theo tỉnh/huyện/xã
- Không chuẩn hóa dữ liệu

### Cách 2: Seed dữ liệu đầy đủ (Tương lai)

Nếu cần query theo địa giới hành chính:

1. **Import dữ liệu 63 tỉnh/thành phố**
```sql
INSERT INTO provinces (id, code, name, name_with_type) VALUES
(1, '01', 'Hà Nội', 'Thành phố Hà Nội'),
(79, '79', 'Hồ Chí Minh', 'Thành phố Hồ Chí Minh'),
...
```

2. **Import quận/huyện**
```sql
INSERT INTO districts (id, code, name, name_with_type, province_id) VALUES
(1, '001', 'Ba Đình', 'Quận Ba Đình', 1),
...
```

3. **Import phường/xã**
```sql
INSERT INTO communes (id, code, name, name_with_type, district_id) VALUES
(1, '00001', 'Phúc Xá', 'Phường Phúc Xá', 1),
...
```

4. **Sử dụng OpenCageService** để map tên → ID

**Ưu điểm**:
- Query theo địa giới: "Tất cả moments ở Hà Nội"
- Chuẩn hóa dữ liệu
- Hỗ trợ thống kê theo vùng

**Nhược điểm**:
- Phức tạp, cần seed ~11,000 records
- Tên có thể không khớp (VD: "TP.HCM" vs "Thành phố Hồ Chí Minh")
- Cần maintain khi có thay đổi hành chính

## Cấu trúc hiện tại

### Bảng: locations

```sql
CREATE TABLE locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    address VARCHAR(500),  -- Địa chỉ đầy đủ text
    name VARCHAR(255)
);
```

### Bảng: moments

```sql
CREATE TABLE moments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT,
    author_id BIGINT NOT NULL,
    location_id BIGINT,        -- Foreign key to locations
    province_id INT,           -- NULL (chưa dùng)
    district_id INT,           -- NULL (chưa dùng)
    commune_id INT,            -- NULL (chưa dùng)
    category VARCHAR(50),
    is_public BOOLEAN,
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);
```

## Dữ liệu mẫu

### Moment với GPS (Chụp nhanh)

```json
{
  "id": 123,
  "content": "Đi chơi",
  "location": {
    "id": 456,
    "latitude": 10.8418712,
    "longitude": 106.798116,
    "address": "Phường Tăng Nhơn Phú A, Thành phố Thủ Đức, Thành phố Hồ Chí Minh",
    "name": "User Location"
  },
  "province": null,
  "district": null,
  "commune": null
}
```

### Moment với Location Picker (Thư viện)

```json
{
  "id": 124,
  "content": "Phong cảnh đẹp",
  "location": {
    "id": 457,
    "latitude": 21.0352,
    "longitude": 105.8190,
    "address": "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội",
    "name": "User Location"
  },
  "province": null,
  "district": null,
  "commune": null
}
```

## Query Examples

### Tìm moments gần vị trí

```sql
-- Tìm moments trong bán kính 5km
SELECT m.*, l.latitude, l.longitude, l.address
FROM moments m
JOIN locations l ON m.location_id = l.id
WHERE (
    6371 * acos(
        cos(radians(21.0278)) * cos(radians(l.latitude)) * 
        cos(radians(l.longitude) - radians(105.8342)) + 
        sin(radians(21.0278)) * sin(radians(l.latitude))
    )
) < 5;
```

### Tìm moments theo địa chỉ text

```sql
-- Tìm moments ở Hà Nội
SELECT m.*, l.address
FROM moments m
JOIN locations l ON m.location_id = l.id
WHERE l.address LIKE '%Hà Nội%';
```

### Tìm moments theo tọa độ

```sql
-- Tìm moments trong khu vực (bounding box)
SELECT m.*, l.latitude, l.longitude
FROM moments m
JOIN locations l ON m.location_id = l.id
WHERE l.latitude BETWEEN 20.9 AND 21.1
  AND l.longitude BETWEEN 105.7 AND 105.9;
```

## Kế hoạch tương lai

### Phase 1: Hiện tại ✅
- Lưu location đơn giản (latitude, longitude, address text)
- Đủ để hiển thị map và tìm kiếm cơ bản

### Phase 2: Nếu cần (Tùy chọn)
- Seed dữ liệu 63 tỉnh/thành phố
- Implement OpenCageService để map tên → ID
- Cho phép query theo địa giới hành chính

### Phase 3: Tối ưu (Tương lai xa)
- Thêm spatial index cho location
- Sử dụng PostGIS cho query địa lý phức tạp
- Cache kết quả geocoding

## Kết luận

**Hiện tại**: Dùng cách đơn giản, lưu address dưới dạng text. Đủ cho MVP và dễ maintain.

**Tương lai**: Nếu cần query phức tạp theo địa giới, mới seed data và dùng foreign keys.
