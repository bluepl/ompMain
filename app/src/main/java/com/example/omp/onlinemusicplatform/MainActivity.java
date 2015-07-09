package com.example.omp.onlinemusicplatform;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.sourceforge.jtds.jdbc.Driver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.HttpClient;


public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    CustomDrawerAdapter adapter;

    List<DrawerItem> dataList;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initializing
        dataList = new ArrayList<DrawerItem>();
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        dataList.add(new DrawerItem("New songs", R.drawable.ic_action_group));
        dataList.add(new DrawerItem("Record a song", R.drawable.ic_action_video));
        dataList.add(new DrawerItem("Upload a song", R.drawable.ic_action_import_export));
        dataList.add(new DrawerItem("Songs", R.drawable.ic_action_import_export));
        dataList.add(new DrawerItem("Sleep Mode", R.drawable.ic_action_import_export));
        dataList.add(new DrawerItem("Setting", R.drawable.ic_action_import_export));
        dataList.add(new DrawerItem("Quit", R.drawable.ic_action_import_export));

        adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }

    public void SelectItem(int position) {

        Fragment fragment = null;
        Bundle args = new Bundle();
        switch (position) {
            //New Songs
            case 0:

                break;

            //Record a Song
            case 1:
                i = new Intent(getApplicationContext(), AudioCaptureActivity.class);
                startActivity(i);
                break;
            //Upload a song
            case 2:
                i = new Intent(getApplicationContext(), UploadToServer.class);
                startActivity(i);
                break;
            //My Playlist
            case 3:
                i = new Intent(getApplicationContext(), AlbumsActivity.class);
                startActivity(i);
                break;
            //Sleep Mode
            case 4:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Sleep Mode");
                alert.setMessage("OMP will turn off after (mins):");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int value = Integer.parseInt(input.getText().toString());
                        final Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            public void run() {
                                finish();
                                System.exit(0);
                                t.cancel();
                            }
                        }, value * 60000);

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();

                break;
            //Setting
            case 5:
                //fragment = new FragmentThree();
                //args.putString(FragmentThree.ITEM_NAME, dataList.get(position)
                //        .getItemName());
                //args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(position)
                //        .getImgResID());
                break;
            //Quit
            case 6:
                Intent intent = new Intent(this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Exit me", true);
                startActivity(intent);
                finish();

                //finish();
                //System.exit(0);
                break;

        }
/*
        fragment.setArguments(args);
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
*/

        mDrawerLayout.closeDrawer(mDrawerList);

        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());

    }




    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            SelectItem(position);

        }
    }

}
