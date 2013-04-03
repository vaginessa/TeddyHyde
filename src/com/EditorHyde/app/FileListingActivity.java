package com.EditorHyde.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import org.eclipse.egit.github.core.*;

import org.eclipse.egit.github.core.service.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileListingActivity extends Activity {

    private static final String MARKDOWN_EXTENSION = ".md";
    private ProgressDialog pd;
    private Cwd cwd;
    Tree repoTree;
    FileListAdapter adapter;
    private List<TreeEntry> values;
    String repoName;
    String authToken;
    String login;
    List<TreeEntry> entries;
    Context ctx;
    Repository theRepo;
    TextView repoTv;

    @Override
    public void onBackPressed() {
        Log.d("com.EditorHyde.app", "onBackPressed Called");
        if( cwd.atRoot()) {
            finish();
        }
        else {
            ascend();
            rebuildFilesList();
        }

    }

    private void promptForFilename( final String root, final String prefix, final String template, final String type ) {
        // Set an EditText view to get user input
        final String[] filename = new String[1];
        final LinearLayout ll = new LinearLayout(FileListingActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(FileListingActivity.this);
        final TextView finalFilename = new TextView(FileListingActivity.this);
        ll.addView(finalFilename);
        ll.addView(input);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable text = input.getText();
                String title = text.toString();
                String whitespaceStripped = title.toLowerCase().replaceAll( "\\W+", "-");

                filename[0] = root + prefix + whitespaceStripped + MARKDOWN_EXTENSION;

                finalFilename.setText( "Filename: " + filename[0] );
            }

            @Override
            public void afterTextChanged(Editable s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        AlertDialog show = new AlertDialog.Builder(FileListingActivity.this)
                .setTitle(type + " title")
                .setMessage("Provide a title for your " + type.toLowerCase())
                .setView(ll)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Convert it to proper format
                        loadEditor( template, filename[0], repoName );
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String filename;
        String template;
        switch ( itemId ) {

            case R.id.action_add_new_page:
                template = getString(R.string.page_template);
                promptForFilename( cwd.getFullPathWithTrailingSlash(), "", template, "Page" );

                return true;

            case R.id.action_add_new_post:
                cwd.descendTo("_posts");
                SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-" );
                String prefix = sdf.format( new Date() );

                template = getString(R.string.page_template);
                promptForFilename("_posts/", prefix, template, "Post");

                // create a new post
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_file_listing, menu);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ctx = this;
        cwd = new Cwd();
        values = new ArrayList<TreeEntry>();
        setContentView(R.layout.file_list);

        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);
        login = sp.getString("login", null );

        Bundle extras = getIntent().getExtras();
        repoName = extras.getString("repo");

        repoTv = (TextView)findViewById(R.id.repoName);
        repoTv.setText( repoName );

        pd = ProgressDialog.show( this, "", "Loading repository data..", true);

        new GetRepoFiles().execute();

    }


    class LoadImageTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... unused) {
            // Find all images in the repository
            // Only add items at the root for now
            String cnameContents = null;
            String baseUrl = repoName;
            ArrayList<String> images = new ArrayList<String>();

            for( TreeEntry entry: entries) {

                String name = entry.getPath();

                // Get the CNAME file. If not there, use the repository name as the URL
                if( name.equals( "CNAME" ) ) {
                    try {
                        String fileSha = entry.getSha();
                        cnameContents = ThGitClient.GetFile(authToken, repoName, login, fileSha);
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

            }

            if( null != cnameContents) {
                baseUrl = cnameContents.trim();
            }

            for( TreeEntry entry: entries) {

                String name = entry.getPath();

                if( name.contains( "assets/images") && name.contains( "thumb") ) {
                    String fullUrl = "http://" + baseUrl + "/" + name;
                    images.add( fullUrl );
                }
            }

            RemoteFileCache.loadImages(images);
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            if (pd.isShowing())
                pd.dismiss();

            showFiles(values);
        }
    }



    private void showFiles( List<TreeEntry> files ) {

        ListView listView;
        listView = (ListView) findViewById(R.id.repoFilesList);

        adapter = new FileListAdapter( this, files );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TreeEntry treeEntry = (TreeEntry) adapterView.getItemAtPosition(i);
                String type = treeEntry.getType();
                String file = treeEntry.getPath();

                if( 0 == "tree".compareTo( type  )) {
                    descend(file);
                    rebuildFilesList();
                }
                else {
                    String fileSha = treeEntry.getSha();
                    showEditor( file, fileSha );
                }
            }
        });
    }

    private void filterArray() {
        values.clear();

        if( null != adapter ) {
            adapter.setFrontPath( cwd.getFullPathWithTrailingSlash() );
        }

        // Only add items at the root for now
        for( TreeEntry entry: entries) {

            String type = entry.getType();
            String name = entry.getPath();

            String path = cwd.getFullPathWithTrailingSlash();
            if( -1 != name.indexOf( path )  ) {

                int slashesInCwd = countOccurrences( path, '/' );
                int slashesInFile = countOccurrences( name, '/' );

                if( slashesInCwd == slashesInFile ) {
                    values.add( entry );
                }

            }
        }
    }

    // Thanks: http://stackoverflow.com/questions/275944/how-do-i-count-the-number-of-occurrences-of-a-char-in-a-string
    public static int countOccurrences(String haystack, char needle)
    {
        int count = 0;
        for (int i=0; i < haystack.length(); i++)
        {
            if (haystack.charAt(i) == needle)
            {
                count++;
            }
        }
        return count;
    }

    private void rebuildFilesList() {
        // filter out those with the proper path
        filterArray();

        // Update the repository name
        String theCwd = cwd.getFullPathWithTrailingSlash();
        String repoPlusCwd = repoName;
        if( theCwd.equals("") ) {
            repoPlusCwd += ":" + theCwd;
        }
        repoTv.setText( repoPlusCwd  );

        // update the list
        adapter.notifyDataSetChanged();
    }

    private void descend( String directory ) {
        cwd.descendTo(directory);
    }

    private void ascend() {
        cwd.ascendOne();
    }

    private class GetRepoFiles extends AsyncTask<Void, Void, Boolean> {

        String authToken;
        List<User> users;

        protected Boolean doInBackground(Void...unused) {

            Boolean rv = true;

            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(authToken);

            try {
                CommitService cs = new CommitService();
                cs.getClient().setOAuth2Token(authToken);
                theRepo = repositoryService.getRepository(login, repoName);

                List<RepositoryBranch> branches = repositoryService.getBranches(theRepo);
                RepositoryBranch theBranch = null;
                RepositoryBranch master = null;
                // Iterate over the branches and find gh-pages or master
                for( RepositoryBranch i : branches ) {
                    String theName = i.getName().toString();
                    if( theName.equalsIgnoreCase("gh-pages") ) {
                        theBranch = i;
                    }
                    else if( theName.equalsIgnoreCase("master") ) {
                        master = i;
                    }
                }
                if( null == theBranch ) {
                    theBranch = master;
                }

                String masterCommitSha = master.getCommit().getSha();
                String baseCommitSha = theBranch.getCommit().getSha();
                DataService ds = new DataService();
                ds.getClient().setOAuth2Token(authToken);
                repoTree = ds.getTree( theRepo, baseCommitSha, true );
                entries  = repoTree.getTree();
                filterArray();

                // See if we have multiple collaborators, and if so, warn the user
                // theRepo.
                CollaboratorService cos = new CollaboratorService();
                cos.getClient().setOAuth2Token(authToken);

                try {
                    users = cos.getCollaborators(theRepo);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            } catch (Exception e) {
                e.printStackTrace();
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            // Determine the images, and load them
            pd.setMessage( "Loading and caching images...");
            new LoadImageTask().execute();

            // This does not work. Why?
            if( null != users && users.size() > 1 ) {
                Toast.makeText( FileListingActivity.this, "WARNING: This repository has multiple collaborators. Teddy Hyde does not always detect recent file changes made via other collaborators. (bug #1)", Toast.LENGTH_LONG );
            }
        }
    }

    public void showEditor( String filename, String fileSha ) {

        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        String authToken = sp.getString("authToken", null);
        String login = sp.getString("login", null );

        pd = ProgressDialog.show( this, "", "Loading file data...", true);

        new GetFileTask().execute( login, authToken, repoName, filename, fileSha );

    }


    private class GetFileTask extends AsyncTask<String, Void, Boolean> {

        String theMarkdown;
        String theFilename;

        protected Boolean doInBackground(String...strings) {

            Boolean rv = true;
            String username = strings[0];
            String authToken = strings[1];
            String repoName = strings[2];
            theFilename = strings[3];
            String fileSha = strings[4];

            try {
                theMarkdown = ThGitClient.GetFile(authToken, repoName, username, fileSha);
            } catch (IOException e) {
                e.printStackTrace();
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            pd.hide();
            loadEditor( theMarkdown, theFilename, repoName );
        }
    }

    private void loadEditor( String theMarkdown, String theFilename, String repoName ) {
        Intent i;
        i = new Intent(ctx, ScreenSlideActivity.class);
        Bundle extras = getIntent().getExtras();
        extras.putString( "markdown", theMarkdown );
        extras.putString( "filename", theFilename );
        extras.putString( "repo", repoName );
        extras.putString( "login", login );

        i.putExtras(extras);
        startActivity(i);

    }
}