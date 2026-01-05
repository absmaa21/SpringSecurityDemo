# Two-Factor Authentication (2FA) with OTP

This Spring Boot application implements two-factor authentication using OTP (One-Time Password) sent via email.

## Features

- User signup with email verification
- Two-factor authentication with OTP
- MFA token-based workflow
- OTP expiration (5 minutes)
- Email delivery of OTP codes
- Comprehensive security filters

## API Endpoints

### 1. User Signup
**Endpoint:** `POST /api/auth/signup`

**Request Body:**
```json
{
  "username": "user@example.com",
  "password": "securepassword"
}
```

**Response:**
```
200 OK
"Successfully registered! Check Email for verification."
```

**Notes:**
- No JWT token is returned on signup
- A verification email is sent to the user's email address
- User must verify email before they can sign in

---

### 2. Email Verification
**Endpoint:** `GET /api/auth/verify-email?token={TOKEN}`

**Response:**
```
200 OK
"Successfully verified!"
```

---

### 3. SignIn with MFA
**Endpoint:** `POST /api/auth/signin`

**Request Body:**
```json
{
  "username": "user@example.com",
  "password": "securepassword"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Notes:**
- Returns an MFA token (not a regular JWT)
- The MFA token contains a claim: `"type": "mfa"`
- A 6-digit OTP is generated and sent to the user's email
- The OTP is valid for 5 minutes

---

### 4. OTP Verification
**Endpoint:** `GET /api/auth/otp-signin?otp={OTP}`

**Headers:**
```
Authorization: Bearer {MFA_TOKEN}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Notes:**
- Validates the MFA token and OTP
- Returns a final JWT (without `"type": "mfa"` claim)
- This JWT can be used to access protected resources

---

### 5. Legacy Login (without MFA)
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "user@example.com",
  "password": "securepassword"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Notes:**
- Returns a regular JWT directly (no MFA)
- Kept for backward compatibility
- Consider deprecating in favor of MFA workflow

---

## Workflow

### Complete MFA Authentication Flow

```
1. User Registration
   POST /api/auth/signup
   → Server creates user account
   → Server sends verification email
   → User receives email and clicks verification link
   
2. Email Verification
   GET /api/auth/verify-email?token={TOKEN}
   → Server enables user account

3. SignIn Request
   POST /api/auth/signin
   → Server validates credentials
   → Server generates MFA token
   → Server generates 6-digit OTP
   → Server sends OTP via email
   → Client receives MFA token
   
4. OTP Verification
   GET /api/auth/otp-signin?otp={OTP}
   Authorization: Bearer {MFA_TOKEN}
   → Server validates MFA token
   → Server validates OTP
   → Server generates final JWT
   → Client receives final JWT
   
5. Access Protected Resources
   GET /api/private/user
   Authorization: Bearer {FINAL_JWT}
   → Server validates JWT
   → Server grants access
```

---

## Security Features

### OTP Service
- **Generation:** Random 6-digit integer (100000-999999)
- **Storage:** In-memory ConcurrentHashMap (username → OTP + expiry)
- **Expiration:** 5 minutes
- **Validation:** One-time use, cleared after successful validation

### MFA Token
- **Type Claim:** Contains `"type": "mfa"` claim
- **Expiration:** 10 minutes
- **Purpose:** Temporary token for OTP verification only
- **Cannot be used:** For accessing protected resources

### Final JWT
- **Type Claim:** No MFA claim
- **Expiration:** 24 hours (configurable)
- **Purpose:** Access protected resources
- **Scope:** Full application access based on user role

### Security Filters (in order)
1. **RateLimitingFilter:** Prevents brute force attacks on OTP endpoint (10 req/5min per IP)
2. **MfaAuthenticationFilter:** Validates MFA tokens for `/api/auth/otp-signin`
3. **JwtAuthenticationFilter:** Validates regular JWTs for protected resources

### Login Security
- **Failed Attempt Tracking:** Tracks failed login attempts per user
- **Account Lockout:** 5 failed attempts → 15-minute lockout
- **Automatic Unlocking:** Lockout expires automatically after duration
- **Audit Logging:** All login attempts (successful and failed) are logged

### Rate Limiting
- **Endpoint:** `/api/auth/otp-signin`
- **Limit:** 10 requests per 5 minutes per IP address
- **Response:** HTTP 429 (Too Many Requests) when limit exceeded
- **Purpose:** Prevents brute force attacks on OTP codes

---

## Error Responses

### Invalid Credentials
```json
{
  "timestamp": "2026-01-05T13:20:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```

### Account Locked
```json
{
  "timestamp": "2026-01-05T13:20:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Account is temporarily locked due to multiple failed login attempts. Please try again later."
}
```

### Invalid MFA Token
```json
{
  "timestamp": "2026-01-05T13:20:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid MFA token"
}
```

### Invalid or Expired OTP
```json
{
  "timestamp": "2026-01-05T13:20:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired OTP"
}
```

### Rate Limit Exceeded
```
429 Too Many Requests
"Too many requests. Please try again later."
```

---

## Configuration

### Application Properties
```properties
# JWT Configuration
application.security.jwt.secret=rzEOH8QW2Ggj3uBgu2LDajCCfsN2waPA4JoFUj7sRBhNexM1wzYNWbkezkxNAsn9slPv4xKgyolS1uN9pKCi0w==
application.security.jwt.expiration=86400000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## Testing

### HTTP Client Test Files

The project includes two HTTP test files for IntelliJ IDEA's HTTP Client:

1. **mfa-login-success.http** - Tests successful authentication flow
2. **mfa-login-failure.http** - Tests various failure scenarios

### Test Scenarios

#### Success Cases
- Complete signup and signin workflow
- Email verification
- OTP verification with valid code
- Access to private API with valid JWT

#### Failure Cases
- Invalid credentials
- Wrong OTP
- Expired OTP (after 5 minutes)
- Invalid MFA token
- Using regular JWT for OTP verification
- Using MFA token for protected resources
- Missing authorization header
- Account lockout after failed attempts
- Rate limit exceeded

---

## Extensions Implemented

### ✅ OTP Expiration
- OTP is valid for exactly 5 minutes
- Expired OTPs are automatically rejected and cleared
- Automatic cleanup of expired OTP data

### ✅ Login Attempt Tracking & Account Lockout
- Failed login attempts are tracked per user
- Account locked after 5 failed attempts
- 15-minute lockout duration
- Automatic unlocking after lockout period expires
- Successful login resets failed attempt counter
- Comprehensive logging of all login attempts

### ✅ Rate Limiting
- Applied to `/api/auth/otp-signin` endpoint
- Maximum 10 requests per 5 minutes per IP address
- Returns HTTP 429 (Too Many Requests) when exceeded
- IP-based tracking with automatic cleanup
- Prevents brute force attacks on OTP codes

### ✅ Enhanced Error Handling
- Custom exceptions for all error scenarios
- Global exception handler for consistent error responses
- Proper HTTP status codes
- Detailed error messages with timestamps
- JSON error response format

### 🔄 Possible Future Enhancements
- Persist login attempts to database for long-term audit
- Send email alerts on multiple failed login attempts
- Implement progressive delays on repeated failures
- Add CAPTCHA after multiple failed attempts
- SMS-based OTP as alternative to email

---

## Architecture

### Services
- **OtpService:** OTP generation, validation, and storage
- **AuthService:** User authentication and MFA workflow
- **JwtService:** JWT and MFA token generation/validation
- **EmailService:** Email delivery for OTP and verification
- **LoginAttemptService:** Track failed logins and account lockouts

### Filters (in order)
1. **RateLimitingFilter:** Rate limiting for OTP endpoint
2. **MfaAuthenticationFilter:** Pre-authenticates MFA tokens
3. **JwtAuthenticationFilter:** Authenticates regular JWTs

### Controllers
- **AuthController:** Authentication endpoints
- **DemoController:** Protected resource examples

### Exception Handling
- **GlobalExceptionHandler:** Centralized error handling
- Custom exceptions for domain-specific errors

---

## License

This project is for educational purposes.
