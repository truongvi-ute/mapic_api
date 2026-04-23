package com.mapic.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email Service Implementation using Brevo (Sendinblue) SMTP
 * Handles sending OTP and other transactional emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    
    @Value("${mail.from.email}")
    private String fromEmail;
    
    @Value("${mail.from.name}")
    private String fromName;

    @Override
    @Async
    public void sendOtpEmail(String toEmail, String otp, String userName) {
        log.info("Sending OTP email to: {}", toEmail);
        
        String subject = "Xác thực tài khoản MAPIC - Mã OTP của bạn";
        String body = buildOtpEmailBody(otp, userName, "đăng ký");
        
        try {
            sendHtmlEmail(toEmail, subject, body);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetOtp(String toEmail, String otp, String userName) {
        log.info("Sending password reset OTP email to: {}", toEmail);
        
        String subject = "Đặt lại mật khẩu MAPIC - Mã OTP của bạn";
        String body = buildOtpEmailBody(otp, userName, "đặt lại mật khẩu");
        
        try {
            sendHtmlEmail(toEmail, subject, body);
            log.info("Password reset OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset OTP email", e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        log.info("Sending welcome email to: {}", toEmail);
        
        String subject = "Chào mừng bạn đến với MAPIC! 🎉";
        String body = buildWelcomeEmailBody(userName);
        
        try {
            sendHtmlEmail(toEmail, subject, body);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw exception for welcome email - it's not critical
        }
    }

    @Override
    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        log.info("Sending email to: {} with subject: {}", toEmail, subject);
        
        try {
            sendHtmlEmail(toEmail, subject, body);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Internal method to send HTML email using JavaMailSender
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content
            
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException | java.io.UnsupportedEncodingException e) {
            throw new MessagingException("Failed to send email", e);
        }
    }

    /**
     * Build HTML email body for OTP
     */
    private String buildOtpEmailBody(String otp, String userName, String purpose) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        border-radius: 10px;
                        padding: 40px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .content {
                        background: white;
                        border-radius: 8px;
                        padding: 30px;
                    }
                    h1 {
                        color: #667eea;
                        margin-top: 0;
                        font-size: 24px;
                    }
                    .otp-box {
                        background: #f8f9fa;
                        border: 2px dashed #667eea;
                        border-radius: 8px;
                        padding: 20px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .otp-code {
                        font-size: 32px;
                        font-weight: bold;
                        color: #667eea;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .warning {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 12px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e9ecef;
                        color: #6c757d;
                        font-size: 14px;
                    }
                    .logo {
                        text-align: center;
                        margin-bottom: 20px;
                    }
                    .logo-text {
                        font-size: 28px;
                        font-weight: bold;
                        color: white;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="logo">
                        <div class="logo-text">📍 MAPIC</div>
                    </div>
                    <div class="content">
                        <h1>Xin chào %s! 👋</h1>
                        <p>Cảm ơn bạn đã sử dụng MAPIC. Đây là mã OTP để %s tài khoản của bạn:</p>
                        
                        <div class="otp-box">
                            <div style="color: #6c757d; font-size: 14px; margin-bottom: 10px;">MÃ OTP CỦA BẠN</div>
                            <div class="otp-code">%s</div>
                            <div style="color: #6c757d; font-size: 12px; margin-top: 10px;">Mã có hiệu lực trong 5 phút</div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Lưu ý bảo mật:</strong>
                            <ul style="margin: 10px 0 0 0; padding-left: 20px;">
                                <li>Không chia sẻ mã OTP này với bất kỳ ai</li>
                                <li>MAPIC sẽ không bao giờ yêu cầu mã OTP qua điện thoại hoặc email</li>
                                <li>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email</li>
                            </ul>
                        </div>
                        
                        <p>Nếu bạn cần hỗ trợ, vui lòng liên hệ với chúng tôi qua email: <a href="mailto:support@mapic.com">support@mapic.com</a></p>
                        
                        <div class="footer">
                            <p><strong>MAPIC Team</strong></p>
                            <p>Chia sẻ khoảnh khắc, kết nối địa điểm</p>
                            <p style="font-size: 12px; color: #adb5bd;">Email này được gửi tự động, vui lòng không trả lời.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, userName, purpose, otp);
    }

    /**
     * Build HTML email body for welcome message
     */
    private String buildWelcomeEmailBody(String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        border-radius: 10px;
                        padding: 40px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .content {
                        background: white;
                        border-radius: 8px;
                        padding: 30px;
                    }
                    h1 {
                        color: #667eea;
                        margin-top: 0;
                        font-size: 28px;
                        text-align: center;
                    }
                    .welcome-icon {
                        text-align: center;
                        font-size: 64px;
                        margin: 20px 0;
                    }
                    .feature-box {
                        background: #f8f9fa;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 15px 0;
                    }
                    .feature-title {
                        font-weight: bold;
                        color: #667eea;
                        margin-bottom: 5px;
                    }
                    .cta-button {
                        display: inline-block;
                        background: #667eea;
                        color: white;
                        padding: 12px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e9ecef;
                        color: #6c757d;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="content">
                        <div class="welcome-icon">🎉</div>
                        <h1>Chào mừng đến với MAPIC!</h1>
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chúc mừng bạn đã đăng ký thành công tài khoản MAPIC! Chúng tôi rất vui mừng được chào đón bạn vào cộng đồng của chúng tôi.</p>
                        
                        <div class="feature-box">
                            <div class="feature-title">📸 Chia sẻ khoảnh khắc</div>
                            <p>Ghi lại và chia sẻ những khoảnh khắc đáng nhớ của bạn với bạn bè</p>
                        </div>
                        
                        <div class="feature-box">
                            <div class="feature-title">📍 Khám phá địa điểm</div>
                            <p>Tìm kiếm và khám phá những địa điểm thú vị xung quanh bạn</p>
                        </div>
                        
                        <div class="feature-box">
                            <div class="feature-title">👥 Kết nối bạn bè</div>
                            <p>Kết nối với bạn bè và xem họ đang ở đâu, làm gì</p>
                        </div>
                        
                        <p style="text-align: center;">
                            <a href="#" class="cta-button">Bắt đầu khám phá</a>
                        </p>
                        
                        <p>Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi!</p>
                        
                        <div class="footer">
                            <p><strong>MAPIC Team</strong></p>
                            <p>Chia sẻ khoảnh khắc, kết nối địa điểm</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, userName);
    }
}
