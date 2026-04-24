# Test Ban/Unban User Flow

## 🧪 Test Cases cho chức năng Ban/Unban

### **Test Case 1: Ban User (Temporary)**
```bash
# 1. Get user list to find a test user
curl -X GET "https://mapic-backend-ute.onrender.com/api/admin/users?page=0&size=5" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"

# 2. Ban user temporarily (7 days)
curl -X PUT "https://mapic-backend-ute.onrender.com/api/admin/users/{USER_ID}/status" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "SUSPENDED",
    "reason": "Vi phạm quy định cộng đồng",
    "note": "Khóa tạm thời 7 ngày",
    "expiresAt": "2024-12-31T23:59:59",
    "notifyUser": true
  }'

# 3. Verify user is banned
curl -X GET "https://mapic-backend-ute.onrender.com/api/admin/users/{USER_ID}" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### **Test Case 2: Ban User (Permanent)**
```bash
# Ban user permanently
curl -X PUT "https://mapic-backend-ute.onrender.com/api/admin/users/{USER_ID}/status" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "BANNED",
    "reason": "Vi phạm nghiêm trọng quy định",
    "note": "Khóa vĩnh viễn do spam",
    "notifyUser": true
  }'
```

### **Test Case 3: Unban User**
```bash
# Unban user (restore to active)
curl -X PUT "https://mapic-backend-ute.onrender.com/api/admin/users/{USER_ID}/status" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ACTIVE",
    "reason": "Mở khóa tài khoản sau khi xem xét",
    "note": "User đã cam kết tuân thủ quy định",
    "notifyUser": true
  }'

# Verify user is unbanned
curl -X GET "https://mapic-backend-ute.onrender.com/api/admin/users/{USER_ID}" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### **Test Case 4: Warn User**
```bash
# Give user a warning
curl -X PUT "https://mapic-backend-ute.onrender.com/api/admin/users/{USER_ID}/status" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ACTIVE",
    "reason": "Cảnh cáo vi phạm nhẹ",
    "note": "Lần đầu vi phạm, chỉ cảnh cáo",
    "notifyUser": true
  }'
```

## 📱 Frontend Test Flow

### **Admin App Test Steps:**

1. **Login to Admin App**
   - Open admin app
   - Login with admin credentials
   - Navigate to User Management

2. **Find Test User**
   - Search for a test user
   - Click on user to open detail page

3. **Test Ban Flow**
   - Click "Khóa TK" button
   - Select "Khóa tạm thời (7 ngày)"
   - Enter reason: "Test ban functionality"
   - Confirm action
   - Verify success message
   - Check user status shows as banned

4. **Test Unban Flow**
   - On same user detail page
   - Should now see "Mở khóa" button instead of "Khóa TK"
   - Click "Mở khóa" button
   - Confirm unban action
   - Verify success message
   - Check user status shows as active

5. **Test Warning Flow**
   - Click "Cảnh cáo" button
   - Enter warning reason
   - Confirm action
   - Verify success message

## 🔍 Expected Results

### **After Ban:**
- User status in database: `BLOCK`
- Admin UI shows: "Mở khóa" button
- User cannot login to main app
- Admin sees ban info in user profile

### **After Unban:**
- User status in database: `ACTIVE`
- Admin UI shows: "Khóa TK" and "Cảnh cáo" buttons
- User can login to main app normally
- Ban history preserved (if implemented)

### **After Warning:**
- User status remains: `ACTIVE`
- User can still login
- Warning recorded (if warning system implemented)

## 🚨 Common Issues to Check

1. **Status Mapping Issues**
   - Frontend sends: `BANNED`, `SUSPENDED`
   - Backend expects: `BLOCK`
   - Should be handled by mapping logic

2. **UI State Issues**
   - Button visibility based on current ban status
   - Proper refresh after action
   - Loading states during API calls

3. **API Response Issues**
   - Check for proper error handling
   - Verify success messages
   - Ensure data refresh after actions

4. **Permission Issues**
   - Admin authentication required
   - Proper role-based access control

## 🔧 Debug Commands

```bash
# Check user status directly in database
# (if you have database access)
SELECT id, username, status, created_at, updated_at 
FROM users 
WHERE id = {USER_ID};

# Check admin logs
tail -f /path/to/logs/application.log | grep "ADMIN-USER"

# Test admin authentication
curl -X GET "https://mapic-backend-ute.onrender.com/api/admin/dashboard/metrics" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

## ✅ Success Criteria

- [ ] Can ban user (temporary and permanent)
- [ ] Can unban user successfully
- [ ] UI updates correctly after each action
- [ ] User login behavior changes appropriately
- [ ] Admin logs show proper audit trail
- [ ] Error handling works for invalid operations
- [ ] Status mapping works correctly between frontend/backend