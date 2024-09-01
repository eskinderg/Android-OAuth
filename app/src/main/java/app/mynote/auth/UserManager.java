package app.mynote.auth;
import android.content.Context;
import app.mynote.core.MyNote;

public class UserManager {

    public static User getUser(Context context) {
        User user = new User();
        String id =MyNote.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("sub", null);
        String email = MyNote.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("email", null);
        String givenName = MyNote.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("given_name", null);

        if(id == null || email == null || givenName == null)
            return null;

        user.setId(id);
        user.setEmail(email);
        user.setGivenName(givenName);
        AuthConfig.USER = user;
       return user;
    }
}
