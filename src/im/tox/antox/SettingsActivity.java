package im.tox.antox;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	Spinner dhtSpinner;
	Spinner statusSpinner;
	EditText dhtIP;
	EditText dhtPort;
	EditText dhtKey;
	private boolean usingSpinner;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		/* Check if connected to the Internet */
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// Executes in a separate thread so UI experience isn't affected
			new DownloadDHTList().execute("http://markwinter.me/servers.php");
		} else {
			// TODO: Decide on a whole what to do if the user isnt connected to
			// the Internet and using antox
		}

		usingSpinner = false;
		dhtSpinner = (Spinner) findViewById(R.id.dhtSpinner);
		statusSpinner = (Spinner) findViewById(R.id.settings_spinner_status);
		dhtIP = (EditText) findViewById(R.id.settings_dht_ip);
		dhtPort = (EditText) findViewById(R.id.settings_dht_port);
		dhtKey = (EditText) findViewById(R.id.settings_dht_key);
		
		String[] dhtItems = new String[] { "stqism - US", "NSA - US",
				"sonOfRa - DE", "stal - US" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, dhtItems);
		dhtSpinner.setAdapter(adapter);

		String[] statusItems = new String[] { "online", "away", "busy" };
		ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, statusItems);
		statusSpinner.setAdapter(statusAdapter);
		
		/* Get saved preferences */
		SharedPreferences pref = getSharedPreferences("settings",
				Context.MODE_PRIVATE);

		/* If the preferences aren't blank/default, then add them to text fields */
		/*
		 * If they are blank/default, the text from strings.xml will be
		 * displayed instead
		 */
		TextView userKey = (TextView) findViewById(R.id.settings_user_key);
		Intent intent = getIntent();
		userKey.setText(intent.getStringExtra(MainActivity.EXTRA_MESSAGE));
		
		if (pref.getString("saved_name_hint", "") != "") {
			EditText nameHint = (EditText) findViewById(R.id.settings_name_hint);
			nameHint.setText(pref.getString("saved_name_hint", ""));
		}
		
		if (pref.getString("saved_dht_ip", "192.254.75.98") != "192.254.75.98") {
			dhtIP.setText(pref.getString("saved_dht_ip", "192.254.75.98"));
		}
	
		if (pref.getString("saved_dht_port", "33445") != "33445") {
			dhtPort.setText(pref.getString("saved_dht_port", "33445"));
		}
	
		if (pref.getString("saved_dht_key",
				"FE3914F4616E227F29B2103450D6B55A836AD4BD23F97144E2C4ABE8D504FE1B") 
				!= "FE3914F4616E227F29B2103450D6B55A836AD4BD23F97144E2C4ABE8D504FE1B") {
			dhtKey.setText(pref.getString("saved_dht_key", 
					"FE3914F4616E227F29B2103450D6B55A836AD4BD23F97144E2C4ABE8D504FE1B"));
		}

		String savedDHT = pref.getString("saved_dht_spinner", "");
		int statusPosDHT = adapter.getPosition(savedDHT);
		dhtSpinner.setSelection(statusPosDHT);

		if (pref.getString("saved_note_hint", "") != "") {
			EditText noteHint = (EditText) findViewById(R.id.settings_note_hint);
			noteHint.setText(pref.getString("saved_note_hint", ""));
		}

		if (pref.getString("saved_status_hint", "") != "") {
			String savedStatus = pref.getString("saved_status_hint", "");
			int statusPos = statusAdapter.getPosition(savedStatus);
			statusSpinner.setSelection(statusPos);		
		}

	}

	public void updateSettings(View view) {
		/* Get all text from the fields */
		TextView userKeyText = (TextView) findViewById(R.id.settings_user_key);
		EditText nameHintText = (EditText) findViewById(R.id.settings_name_hint);
		EditText dhtIpHintText = (EditText) findViewById(R.id.settings_dht_ip);
		EditText dhtKeyHintText = (EditText) findViewById(R.id.settings_dht_key);
		EditText dhtPortHintText = (EditText) findViewById(R.id.settings_dht_port);
		EditText noteHintText = (EditText) findViewById(R.id.settings_note_hint);
		//EditText statusHintText = (EditText) findViewById(R.id.settings_status_hint);

		/* Save settings to file */

		SharedPreferences pref = getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		/*
		 * If the fields aren't equal to the default strings in strings.xml then
		 * they contain user entered data so they need saving
		 */
		if (nameHintText.getText().toString() != getString(R.id.settings_name_hint))
			editor.putString("saved_name_hint", nameHintText.getText()
					.toString());
		if (userKeyText.getText().toString() != getString(R.id.settings_user_key))
			editor.putString("saved_user_key_hint", userKeyText.getText()
					.toString());
		if(!usingSpinner) {
			if (dhtIpHintText.getText().toString() != getString(R.id.settings_dht_ip))
				editor.putString("saved_dht_ip", dhtIpHintText.getText().toString());
			if (dhtKeyHintText.getText().toString() != getString(R.id.settings_dht_key))
				editor.putString("saved_dht_key", dhtKeyHintText.getText()
						.toString());
			if (dhtPortHintText.getText().toString() != getString(R.id.settings_dht_port))
				editor.putString("saved_dht_port", dhtPortHintText.getText()
						.toString());
		}
		if (noteHintText.getText().toString() != getString(R.id.settings_note_hint))
			editor.putString("saved_note_hint", noteHintText.getText()
					.toString());

		editor.putString("saved_dht_spinner", dhtSpinner.getSelectedItem().toString());
		editor.putString("saved_status_hint", statusSpinner.getSelectedItem().toString());
		
		editor.apply();

		Context context = getApplicationContext();
		CharSequence text = "Settings updated";
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();

		finish();
	}

	public void onDHTPreClicked(View view) {
		usingSpinner = true;
		dhtSpinner.setVisibility(View.VISIBLE);
		dhtIP.setVisibility(View.GONE);
		dhtPort.setVisibility(View.GONE);
		dhtKey.setVisibility(View.GONE);
	}

	public void onDHTSelfClicked(View view) {
		usingSpinner = false;
		dhtSpinner.setVisibility(View.GONE);
		dhtIP.setVisibility(View.VISIBLE);
		dhtPort.setVisibility(View.VISIBLE);
		dhtKey.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class DownloadDHTList extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			// params comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		private String downloadUrl(String myurl) throws IOException {
			InputStream is = null;
			// Only display the first 500 characters of the retrieved
			// web page content.
			int len = 500;

			try {
				URL url = new URL(myurl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query
				conn.connect();
				is = conn.getInputStream();

				// Convert the InputStream into a string
				String contentAsString = readIt(is, len);
				return contentAsString;

				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}

		public String readIt(InputStream stream, int len) throws IOException,
				UnsupportedEncodingException {
			Reader reader = null;
			reader = new InputStreamReader(stream, "UTF-8");
			char[] buffer = new char[len];
			reader.read(buffer);
			// TODO: Parse the JSON
			return new String(buffer);
		}
	}

}
