# 🚀 Quick Fix: Cloudinary Images Not Showing

## Vấn đề
Ảnh đã upload lên Cloudinary nhưng không hiển thị trên production.

## Giải pháp nhanh

### 1. Cấu hình Cloudinary trên Render
Vào Render Dashboard → Service → Environment Variables, thêm:
```
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### 2. Chạy migration script
```bash
# Trên local
./cloudinary-migration.sh https://your-app.onrender.com

# Hoặc test thủ công
curl https://your-app.onrender.com/api/admin/migration/check-cloudinary-config
curl -X POST "https://your-app.onrender.com/api/admin/migration/update-urls-to-cloudinary?dryRun=false"
```

### 3. Kiểm tra kết quả
- Test hiển thị avatar, cover image, moment photos
- Kiểm tra console logs không có lỗi 404

## Endpoints hữu ích
- `GET /api/admin/migration/check-storage-service` - Kiểm tra service đang dùng
- `GET /api/admin/migration/check-cloudinary-config` - Kiểm tra cấu hình Cloudinary  
- `GET /api/admin/migration/check-local-files` - Kiểm tra files local trong DB
- `POST /api/admin/migration/update-urls-to-cloudinary` - Migration URLs

## Lưu ý
- Backup database trước khi migration
- Test với `dryRun=true` trước
- Cloudinary URLs có format: `https://res.cloudinary.com/{cloud}/image/upload/mapic/{folder}/{uuid}`