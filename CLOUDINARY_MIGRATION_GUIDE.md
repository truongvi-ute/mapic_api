# Hướng dẫn Migration Cloudinary

## Vấn đề hiện tại
Ảnh đã được upload lên Cloudinary nhưng không hiển thị trên môi trường deploy vì:
1. Cấu hình Cloudinary chưa được set trên Render
2. Database vẫn chứa local filenames thay vì Cloudinary URLs

## Bước 1: Cấu hình Cloudinary trên Render

### 1.1 Truy cập Render Dashboard
- Đăng nhập vào https://render.com
- Chọn service `mapic-backend-ute`
- Vào tab **Environment**

### 1.2 Thêm các biến môi trường Cloudinary
Thêm các biến sau:

```
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key  
CLOUDINARY_API_SECRET=your_api_secret
```

**Lưu ý**: Thay `your_cloud_name`, `your_api_key`, `your_api_secret` bằng thông tin thực tế từ Cloudinary Dashboard của bạn.

### 1.3 Deploy lại service
Sau khi thêm biến môi trường, Render sẽ tự động deploy lại.

## Bước 2: Kiểm tra cấu hình

### 2.1 Kiểm tra Cloudinary config
```bash
curl https://your-app-url.onrender.com/api/admin/migration/check-cloudinary-config
```

### 2.2 Kiểm tra local files trong database
```bash
curl https://your-app-url.onrender.com/api/admin/migration/check-local-files
```

## Bước 3: Migration URLs

### 3.1 Dry run (kiểm tra trước)
```bash
curl -X POST "https://your-app-url.onrender.com/api/admin/migration/update-urls-to-cloudinary?dryRun=true"
```

### 3.2 Thực hiện migration thực tế
```bash
curl -X POST "https://your-app-url.onrender.com/api/admin/migration/update-urls-to-cloudinary?dryRun=false"
```

## Bước 4: Xác minh

### 4.1 Kiểm tra lại local files
```bash
curl https://your-app-url.onrender.com/api/admin/migration/check-local-files
```

Sau migration, số lượng local files phải là 0.

### 4.2 Test hiển thị ảnh
- Kiểm tra avatar user
- Kiểm tra cover image
- Kiểm tra ảnh trong moments

## Cấu trúc URL Cloudinary

Sau migration, URLs sẽ có dạng:
```
https://res.cloudinary.com/{cloud_name}/image/upload/mapic/{folder}/{uuid}
```

Ví dụ:
- Avatar: `https://res.cloudinary.com/your-cloud/image/upload/mapic/avatars/21191601-5f89-44c7-a372-7308632d65b0`
- Cover: `https://res.cloudinary.com/your-cloud/image/upload/mapic/covers/1c512dfb-ad32-4125-895a-a3f706f04549`
- Moment: `https://res.cloudinary.com/your-cloud/image/upload/mapic/moments/96664c9e-c8e6-4cad-893d-7694d409dce9`

## Troubleshooting

### Lỗi "Cloudinary cloud name not configured"
- Kiểm tra biến môi trường `CLOUDINARY_CLOUD_NAME` trên Render
- Đảm bảo service đã được deploy lại sau khi thêm biến

### Ảnh vẫn không hiển thị
1. Kiểm tra URLs trong database đã được update chưa
2. Kiểm tra ảnh có tồn tại trên Cloudinary không
3. Kiểm tra public_id trên Cloudinary có đúng format `mapic/{folder}/{uuid}` không

### Rollback nếu cần
Nếu có vấn đề, có thể rollback bằng cách:
1. Restore database từ backup
2. Hoặc chạy script reverse migration (cần tạo thêm)