package com.uacapstone.red;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.GoogleBaseGameActivity;
import com.uacapstone.red.manager.ResourcesManager;
import com.uacapstone.red.manager.SceneManager;
import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.NetworkingConstants.MessageFlags;
import com.uacapstone.red.networking.messaging.FlaggedNetworkMessage;
import com.uacapstone.red.networking.messaging.SetHostMessage;
import com.uacapstone.red.scene.GameScene;
//import org.andengine.ui.activity.BaseGameActivity;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public class GameActivity extends GoogleBaseGameActivity implements RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener, OnInvitationReceivedListener
{
	final static String TAG = "ButtonClicker2000";
	// Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

	
	// Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;
    String mHostId = null;
    
    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[8];
    
    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();
	
	private BoundCamera camera;
	private ResourcesManager resourcesManager;
    
	public EngineOptions onCreateEngineOptions()
	{
	    camera = new BoundCamera(0, 0, 800, 480);
	    EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(800, 480), this.camera);
	    engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
	    engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
	    return engineOptions;
	}

    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException
    {
    	ResourcesManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
    	resourcesManager = ResourcesManager.getInstance();
    	pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException
    {
    	SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
    }

    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException
    {
    	mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() 
        {
                public void onTimePassed(final TimerHandler pTimerHandler) 
                {
                    mEngine.unregisterUpdateHandler(pTimerHandler);
                    SceneManager.getInstance().createMenuScene();
                }
        }));
        pOnPopulateSceneCallback.onPopulateSceneFinished();   
    }
    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) 
    {
        return new LimitedFPSEngine(pEngineOptions, 60);
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
        System.exit(0);	
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
        }
        else if (keyCode == KeyEvent.KEYCODE_HOME)
        {
    		finish();
        }
        return false; 
    }

    /**
     * Called by the base class (BaseGameActivity) when sign-in has failed. For
     * example, because the user hasn't authenticated yet. We react to this by
     * showing the sign-in button.
     */
    @Override
    public void onSignInFailed() {
        Log.d(TAG, "Sign-in failed.");
        //switchToScreen(R.id.screen_sign_in);
    }

    /**
     * Called by the base class (BaseGameActivity) when sign-in succeeded. We
     * react by going to our main screen.
     */
    @Override
    public void onSignInSucceeded() {
        Log.d(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        
        Games.Invitations.registerInvitationListener(getApiClient(), this);

        // if we received an invite via notification, accept it; otherwise, go to main screen
        if (getInvitationId() != null) {
            acceptInviteToRoom(getInvitationId());
            return;
        }
        switchToMainScreen();
    }
    
    public void SignOut() {
    	signOut();
    }
    
    //TODO: remove if not needed
    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        /*for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);*/
    }
    
    //Menu buttons
    public void StartQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        //switchToScreen(R.id.screen_wait);
        //keepScreenOn();
        //resetGameVars();
        Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
    }
    
    public void InviteFriends()
    {
    	Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 3);
        //switchToScreen(R.id.screen_wait);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }
    
    public void SeeInvitations()
    {
    	Intent intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
        //switchToScreen(R.id.screen_wait);
        startActivityForResult(intent, RC_INVITATION_INBOX);
    }
    
    
    //TODO: remove if not needed
    void switchToMainScreen() {
        //switchToScreen(isSignedIn() ? R.id.screen_main : R.id.screen_sign_in);
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */
    
	@Override
	public void onConnectedToRoom(Room room) {
		Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(getApiClient()));
        ArrayList<String> ids = new ArrayList<String>(room.getParticipantIds());
        Collections.sort(ids);
        normalizedId = ids.indexOf(mMyId);
        
        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
	}
	
	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            //showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
		
	}
	
	@Override
	public void onRoomConnected(int statusCode, Room room) {
		Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            //showGameError();
            return;
        }
		updateRoom(room);
	}
	
	@Override
	public void onRoomCreated(int statusCode, Room room) {
		Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            //showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
		
	}
	
	// Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }


	
	private int normalizedId = 0;
	public int getNormalizedId()
	{
		return normalizedId;
	}
	public int getNumPlayers()
	{
		if (mParticipants != null)
		{
			return mParticipants.size();
		}
		return 1;
	}
	
	@Override
	public void onLeftRoom(int arg0, String arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDisconnectedFromRoom(Room arg0) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void onP2PConnected(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onP2PDisconnected(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeerDeclined(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onPeerInvitedToRoom(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onPeerJoined(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onPeerLeft(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onPeersConnected(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> arg1) {
		updateRoom(room);
	}

	@Override
	public void onRoomAutoMatching(Room room) {
		updateRoom(room);
	}

	@Override
	public void onRoomConnecting(Room room) {
		updateRoom(room);
	}
	
	public void setHost(String hostId) {
		this.mHostId = hostId;
	}
	
	public boolean isHost() {
		return mHostId.compareTo(mMyId) == 0;
	}
	
	public Participant findParticipantById(String id) {
		for (Participant p : mParticipants) {
			if (p.getParticipantId() == id) {
				return p;
			}
		}
		
		return null;
	}
	
	void updateHost() {
		if (mParticipants != null) {
			
			Participant host = findParticipantById(mHostId);
			
			// if the host does not exist or has left the room, pick a new one
			if (host == null || host.isConnectedToRoom()==false) {
				mHostId = mParticipants.get(0).getParticipantId();
				SetHostMessage msg = new SetHostMessage();
				msg.participantId = mHostId;
				
				sendMessageToAll(new FlaggedNetworkMessage(msg), MessageReliabilityType.Unreliable, false);
			}
			
		}
	}
	
	void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        
        updateHost();
        
        /*if (mParticipants != null) {
            updatePeerScoresDisplay();
        }*/
    }
	
	// Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
    	
        byte[] buf = rtm.getMessageData();
        
        SceneManager m = SceneManager.getInstance();
        GameScene gameScene = m.getGameScene();
        
        try {
			FlaggedNetworkMessage msg = NetworkingConstants.messagePackInstance.read(buf, FlaggedNetworkMessage.class);
			
			if (msg.messageFlag == MessageFlags.MESSAGE_SET_HOST) {
				SetHostMessage hostMessage = NetworkingConstants.messagePackInstance.read(msg.messageBytes, SetHostMessage.class);
				setHost(hostMessage.participantId);
			} else {
				gameScene.handleMessage(msg);
			}
			
        } catch (Exception e) {
        	e.printStackTrace();
        }
        	
    }
    
    private enum MessageReliabilityType {
    	Unreliable,
    	Reliable
    }
    
    public void sendMessage(FlaggedNetworkMessage m) {
    	try {
    		// only send messages to other players if this player is the host,
    		// otherwise send messages to the host
    		if (mHostId == mMyId) {
    			sendMessageToAll(m, MessageReliabilityType.Reliable, false);
    		} else {
    			sendMessageToParticipant(m, this.findParticipantById(mHostId), MessageReliabilityType.Reliable);
    		}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
    private void sendMessageToAll(FlaggedNetworkMessage message, MessageReliabilityType reliability, boolean allowSelf) {
    	for (Participant p : mParticipants) {
    		// skip sending to self, when 'allowSelf' is not set
            if (allowSelf == false && p.getParticipantId().compareTo(mMyId) == 0)
                continue;
            
            sendMessageToParticipant(message, p, reliability);
        }
    }
    
    private void sendMessageToParticipant(FlaggedNetworkMessage message, Participant participant, MessageReliabilityType reliability) {
    	
    	if ( mRoomId == null || participant == null ) return;
    	
    	byte[] rawMessage = message.getBytes();
    	
    	switch (reliability) {
    	case Unreliable:
    		Games.RealTimeMultiplayer.sendUnreliableMessage(getApiClient(), rawMessage, mRoomId, participant.getParticipantId());
    		break;
    	case Reliable:
    		Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, rawMessage, mRoomId, participant.getParticipantId());
    		break;
    	}
    }
    
    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore) {
        if (!mMultiplayer)
            return; // playing single-player mode

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        //mMsgBuf[1] = (byte) mScore;

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (finalScore) {
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, mMsgBuf,
                        mRoomId, p.getParticipantId());
            } else {
                // it's an interim score notification, so we can use unreliable
                Games.RealTimeMultiplayer.sendUnreliableMessage(getApiClient(), mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }
	
	// Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        mMultiplayer = multiplayer;
        SceneManager.getInstance().loadGameScene(mEngine);
        //updateScoreDisplay();
        //broadcastScore(false);
        //switchToScreen(R.id.screen_game);

        //findViewById(R.id.button_click_me).setVisibility(View.VISIBLE);

        // run the gameTick() method every second to update the game.
        final Handler h = new Handler();
        /*h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSecondsLeft <= 0)
                    return;
                gameTick();
                h.postDelayed(this, 1000);
            }
        }, 1000);*/
    }
	
	@Override
    public void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    startGame(true);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    //leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    //leaveRoom();
                }
                break;
        }
    }
	
	// Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        //switchToScreen(R.id.screen_wait);
        keepScreenOn();
        //resetGameVars();
        Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }
    
    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }
	
	// Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        //switchToScreen(R.id.screen_wait);
        keepScreenOn();
        //resetGameVars();
        Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());
    }
    
    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        //switchToScreen(R.id.screen_wait);
        super.onStop();
    }
    
    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        //mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
            mRoomId = null;
            //switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }
    
    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

	@Override
	public void onInvitationReceived(Invitation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInvitationRemoved(String arg0) {
		// TODO Auto-generated method stub
		
	}

}
