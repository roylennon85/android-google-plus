package com.rgm.googleplusdemo;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.moments.ItemScope;
import com.google.android.gms.plus.model.moments.Moment;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

public class MainActivity extends ActionBarActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, OnClickListener,
		ResultCallback<LoadPeopleResult> {

	// Used to invoke sign in user interactions.
	private static final int RC_SIGN_IN = 0;

	// Client used to interact with Google APIs.
	private GoogleApiClient mGoogleApiClient;

	// Prevents us to start further intents.
	private boolean mIntentInProgress;

	private SignInButton btnSignIn;
	private boolean mSignInClicked;
	private ConnectionResult mConnectionResult;

	private Button btnSignOut, btnRevoke, btnShare, btnMoment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Plus.PlusOptions plusOptions = new Plus.PlusOptions.Builder()
				.addActivityTypes("http://schema.org/AddAction",
						"http://schema.com/BuyAction").build();

		// Initialize GoogleApiClient object.
		// Scope is your app initial scope. You can add more later.
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN)
				.addScope(Plus.SCOPE_PLUS_PROFILE).build();

		// Add onclick listener to the button.
		btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
		btnSignIn.setOnClickListener(this);
		btnSignOut = (Button) findViewById(R.id.sign_out_button);
		btnSignOut.setOnClickListener(this);
		btnRevoke = (Button) findViewById(R.id.revoke_button);
		btnRevoke.setOnClickListener(this);
		btnShare = (Button) findViewById(R.id.share_button);
		btnShare.setOnClickListener(this);
		btnMoment = (Button) findViewById(R.id.share_moment);
		btnMoment.setOnClickListener(this);
	}

	// GoogleApiClient lifecycle management
	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	// Handle pending intent result.
	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Handle connection errors or connection attepts with pending intents.
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			mConnectionResult = result;
			if (mSignInClicked) {
				resolveSignInError();
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		mSignInClicked = false;
		btnSignIn.setVisibility(View.GONE);
		btnSignOut.setVisibility(View.VISIBLE);
		btnRevoke.setVisibility(View.VISIBLE);
		btnShare.setVisibility(View.VISIBLE);
		btnMoment.setVisibility(View.VISIBLE);
		Toast.makeText(this, "User is connected", Toast.LENGTH_SHORT).show();

		// Get people in circles.
		Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(
				this);

		// Get a list of people.
		List<String> userIds = new ArrayList<String>();
		userIds.add("107117483540235115863");
		userIds.add("+LarryPage");
		Plus.PeopleApi.load(mGoogleApiClient, userIds).setResultCallback(this);
		
		// Get user info
		getUserInfo();
	}

	// Try to connect again.
	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sign_in_button
				&& !mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			resolveSignInError();
		} else if (view.getId() == R.id.sign_out_button) {
			if (mGoogleApiClient.isConnected()) {
				Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
				mGoogleApiClient.disconnect();
				mGoogleApiClient.connect();
				btnSignIn.setVisibility(View.VISIBLE);
				btnSignOut.setVisibility(View.GONE);
				btnRevoke.setVisibility(View.GONE);
				btnShare.setVisibility(View.GONE);
				btnMoment.setVisibility(View.GONE);
			}
		} else if (view.getId() == R.id.revoke_button) {
			revokeAccess();
		} else if (view.getId() == R.id.share_button) {
			share();
		} else if (view.getId() == R.id.share_moment) {
			shareMoment();
		}
	}

	// Connect.
	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				startIntentSenderForResult(mConnectionResult.getResolution()
						.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	private void revokeAccess() {
		Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
				.setResultCallback(new ResultCallback<Status>() {

					@Override
					public void onResult(Status status) {
						// delete all info from the app and system.
						Toast.makeText(MainActivity.this, "Access revoked",
								Toast.LENGTH_SHORT).show();
						btnSignIn.setVisibility(View.VISIBLE);
						btnSignOut.setVisibility(View.GONE);
						btnRevoke.setVisibility(View.GONE);
						btnShare.setVisibility(View.GONE);
						btnMoment.setVisibility(View.GONE);
					}
				});
	}

	private void share() {
		Intent shareIntent = new PlusShare.Builder(this).setType("text/plain")
				.setText("Welcome to the Google+ platform.")
				.setContentUrl(Uri.parse("https://developers.google.com/+/"))
				.getIntent();

		startActivityForResult(shareIntent, 0);
	}

	private void shareMoment() {
		ItemScope target = new ItemScope.Builder()
				.setId("myuniqueidforthissong")
				.setName("When Johnny Comes Marching Home")
				.setDescription(
						"A song about missing one's family members fighting in the American Civil War")
				.setImage("http://example.com/images/albumThumb.png")
				.setType("http://schema.org/MusicRecording").build();

		Moment moment = new Moment.Builder()
				.setType("http://schema.org/ListenAction").setTarget(target)
				.build();

		if (mGoogleApiClient.isConnected()) {
			Plus.MomentsApi.write(mGoogleApiClient, moment);
		}
	}

	@Override
	public void onResult(LoadPeopleResult peopleData) {
		if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
			PersonBuffer personBuffer = peopleData.getPersonBuffer();

			try {
				int count = personBuffer.getCount();
				for (int i = 0; i < count; i++) {
					Log.d("GooglePlus", "Display name: "
							+ personBuffer.get(i).getDisplayName());
				}
			} finally {
				Log.d("GooglePlus", "==============================================");
				personBuffer.close();
			}
		} else {
			// Error requesting visible circles.
		}
	}
	
	private void getUserInfo(){
		if(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null){
			Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
			String personName = currentPerson.getDisplayName();
			String personPhoto = currentPerson.getImage().getUrl();
			String personGooglePlusProfile = currentPerson.getUrl();
			String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
			
			Log.d("GooglePlus", "=========== Profile ============");
			Log.d("GooglePlus", "Name: " + personName + " Photo: " + personPhoto + " Google+ Profile: " + personGooglePlusProfile + " Email: " + email);
		}
	}
}
