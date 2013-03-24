package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.EditText;
import android.widget.TextView;
import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    SharedPreferences sp;
    TextView loginMessage = null;
    List<Repository> repos;

    public static final String APP_ID = "com.TeddyHyde.app";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        loginMessage = (TextView)findViewById(R.id.loginMessage);

        sp = this.getSharedPreferences( APP_ID, MODE_PRIVATE );
        String email = sp.getString( "email", null );
        String password = sp.getString( "password", null );

        if( null != email && null != password ) {
            EditText etU = (EditText)findViewById(R.id.githubEmail);
            EditText etP = (EditText)findViewById(R.id.githubPassword);
            etU.setText( email );
            etP.setText(password);
        }

        findViewById(R.id.button).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
//
                        loginMessage.setText( "Logging in...");
                        EditText etU = (EditText)findViewById(R.id.githubEmail);
                        EditText etP = (EditText)findViewById(R.id.githubPassword) ;

                        String email = etU.getText().toString();
                        String password = etP.getText().toString();
                        sp.edit().putString( "email", email ).commit();
                        sp.edit().putString( "password", password ).commit();
                        new GetReposTask().execute();
                    }
                });

    }



    private class GetReposTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void...voids) {
            Boolean rv = true;
            String email, password;

            EditText etU = (EditText)findViewById(R.id.githubEmail);
            EditText etP = (EditText)findViewById(R.id.githubPassword);
            email = etU.getText().toString();
            password = etP.getText().toString();


            OAuthService oauthService = new OAuthService();

            // Replace with actual login and password
            oauthService.getClient().setCredentials(email, password);


            // Create authorization with 'gist' scope only
            Authorization auth = new Authorization();
            auth.setScopes(Arrays.asList("gist", "repo"));
            String authToken = null;
            try {
                auth = oauthService.createAuthorization(auth);
                authToken = auth.getToken();

                // Store it for other activities
                sp.edit().putString( "authToken", authToken ).commit();

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

//            // OAuth authentication
//            GitHubClient client = new GitHubClient();
//            client.setOAuth2Token(auth.getToken());

            RepositoryService service = new RepositoryService();
            service.getClient().setOAuth2Token(authToken);
            repos = null;
            try {
                repos = service.getRepositories();

                // If we succeeded, get the user information and store it
                UserService userService = new UserService();
                userService.getClient().setOAuth2Token(authToken);
                String name = userService.getUser().getName();
                String login = userService.getUser().getLogin();
                sp.edit().putString( "name", name ).commit();
                sp.edit().putString( "login", login ).commit();
            }
            catch( Exception e ) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            if( !result ) {
                loginMessage.setText( "Invalid credentials, try again.");
            }
            else {
                showRepoList( repos );
            }

        }
    }

    public void showRepoList( List<Repository> repos ) {

        if( null != repos ) {
            Intent i = new Intent(this, RepoListActivity.class);

            String [] repoNames = new String[repos.size()];
            for( int j = 0; j < repos.size(); j++ ) {
                repoNames[j] = repos.get(j).getName();
            }
            Bundle bundle = new Bundle();
            bundle.putStringArray("repos", repoNames);
            i.putExtras(bundle);
            startActivity(i);

        }

    }
}
