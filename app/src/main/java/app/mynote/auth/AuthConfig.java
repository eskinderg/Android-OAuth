package app.mynote.auth;

public final class AuthConfig {
    public static final String CLIENT_ID = "Android";
    public static final String GRANT_TYPE = "password";
    public static final String SCOPE = "openid";
    public static final String BASE_API_URL = "http://192.168.100.5:3000/api/";
    public static final String KEYCLOAK_URL = "http://192.168.100.5:8080";
    public static User USER;
}
