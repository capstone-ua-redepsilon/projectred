package com.uacapstone.red;

import java.io.IOException;
import java.net.Socket;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.multiplayer.adt.message.IMessage;
import org.andengine.extension.multiplayer.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.client.connector.SocketConnectionServerConnector;
import org.andengine.extension.multiplayer.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.server.SocketServer;
import org.andengine.extension.multiplayer.server.SocketServer.ISocketServerListener;
import org.andengine.extension.multiplayer.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.shared.SocketConnection;
import org.andengine.extension.multiplayer.util.MessagePool;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.WifiUtils;
import org.andengine.util.debug.Debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

//import com.uacapstone.red.networking.ClientMessageFlags;
import com.uacapstone.red.networking.ConnectionCloseServerMessage;
import com.uacapstone.red.networking.NetworkingConstants;
//import com.uacapstone.red.networking.ServerMessageFlags;
import com.uacapstone.red.networking.messaging.AddFaceServerMessage;
import com.uacapstone.red.networking.messaging.MoveFaceServerMessage;

import com.uacapstone.red.networking.NetworkingConstants.ServerMessageFlags;
import com.uacapstone.red.networking.NetworkingConstants.ClientMessageFlags;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 17:10:24 - 19.06.2010
 */
public class MultiplayerExample extends SimpleBaseGameActivity {
    // ===========================================================
    // Constants
    // ===========================================================

    private static final int CAMERA_WIDTH = 720;
    private static final int CAMERA_HEIGHT = 480;

    private static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
    private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
    private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;

    // ===========================================================
    // Fields
    // ===========================================================

    private BitmapTextureAtlas mBitmapTextureAtlas;
    private ITextureRegion mFaceTextureRegion;

    private int mFaceIDCounter;
    private final SparseArray<Sprite> mFaces = new SparseArray<Sprite>();

    private String mServerIP = NetworkingConstants.LOCALHOST_IP;
    private SocketServer<SocketConnectionClientConnector> mSocketServer;
    private ServerConnector<SocketConnection> mServerConnector;

    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

    private boolean isServerOrClientChosen;
    
    // ===========================================================
    // Constructors
    // ===========================================================

    public MultiplayerExample() {
        this.initMessagePool();
    }

    private void initMessagePool() {
        this.mMessagePool.registerMessage(ServerMessageFlags.ADD_FACE, AddFaceServerMessage.class);
        this.mMessagePool.registerMessage(ServerMessageFlags.MOVE_FACE, MoveFaceServerMessage.class);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
        
        final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 38, 38, TextureOptions.BILINEAR);
        this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "game/coin.png", 0, 0);

        this.mBitmapTextureAtlas.load();
    }

    @Override
    public Scene onCreateScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());

        final Scene scene = new Scene();
        scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

        // wait until the server or client is chosen.
        while (!isServerOrClientChosen) {}
        
        /* We allow only the server to actively send around messages. */
        if(MultiplayerExample.this.mSocketServer != null) {
            scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
                @Override
                public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
                    if(pSceneTouchEvent.isActionDown()) {
                    	
                        final AddFaceServerMessage addFaceServerMessage = (AddFaceServerMessage) MultiplayerExample.this.mMessagePool.obtainMessage(ServerMessageFlags.ADD_FACE);
						addFaceServerMessage.set(MultiplayerExample.this.mFaceIDCounter++, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());

						MultiplayerExample.this.mSocketServer.sendBroadcastServerMessage(0, addFaceServerMessage);

						MultiplayerExample.this.mMessagePool.recycleMessage(addFaceServerMessage);
                        return true;
                    } else {
                        return true;
                    }
                }
            });

            scene.setOnAreaTouchListener(new IOnAreaTouchListener() {
                @Override
                public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                	
                	final Sprite face = (Sprite)pTouchArea;
					final Integer faceID = (Integer)face.getUserData();

					final MoveFaceServerMessage moveFaceServerMessage = (MoveFaceServerMessage) MultiplayerExample.this.mMessagePool.obtainMessage(ServerMessageFlags.MOVE_FACE);
					moveFaceServerMessage.set(faceID, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());

					MultiplayerExample.this.mSocketServer.sendBroadcastServerMessage(0, moveFaceServerMessage);							
					
					MultiplayerExample.this.mMessagePool.recycleMessage(moveFaceServerMessage);
                    return true;
                }
            });

            scene.setTouchAreaBindingOnActionDownEnabled(true);
        }

        return scene;
    }

    @Override
    protected Dialog onCreateDialog(final int pID) {
        switch(pID) {
            case DIALOG_SHOW_SERVER_IP_ID:
                try {
                    return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("Your Server-IP ...")
                    .setCancelable(false)
                    .setMessage("The IP of your Server is:\n" + WifiUtils.getWifiIPv4Address(this))
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
                } catch (final Exception e) {
                    return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Your Server-IP ...")
                    .setCancelable(false)
                    .setMessage("Error retrieving IP of your Server: " + e)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface pDialog, final int pWhich) {
                            MultiplayerExample.this.finish();
                        }
                    })
                    .create();
                }
            case DIALOG_ENTER_SERVER_IP_ID:
                final EditText ipEditText = new EditText(this);
                return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Enter Server-IP ...")
                .setCancelable(false)
                .setView(ipEditText)
                .setPositiveButton("Connect", new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface pDialog, final int pWhich) {
                        MultiplayerExample.this.mServerIP = ipEditText.getText().toString();
                        MultiplayerExample.this.initClient();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface pDialog, final int pWhich) {
                        MultiplayerExample.this.finish();
                    }
                })
                .create();
            case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
                return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Be Server or Client ...")
                .setCancelable(false)
                .setPositiveButton("Client", new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface pDialog, final int pWhich) {
                        MultiplayerExample.this.showDialog(DIALOG_ENTER_SERVER_IP_ID);
                        isServerOrClientChosen = true;
                    }
                })
                .setNeutralButton("Server", new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface pDialog, final int pWhich) {
                        MultiplayerExample.this.toast("You can add and move sprites, which are only shown on the clients.");
                        MultiplayerExample.this.initServer();
                        MultiplayerExample.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
                        isServerOrClientChosen = true;
                    }
                })
                .setNegativeButton("Both", new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface pDialog, final int pWhich) {
                        MultiplayerExample.this.toast("You can add sprites and move them, by dragging them.");
                        MultiplayerExample.this.initServerAndClient();
                        MultiplayerExample.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
                        isServerOrClientChosen = true;
                    }
                })
                .create();
            default:
                return super.onCreateDialog(pID);
        }
    }

    @Override
    protected void onDestroy() {
        if(this.mSocketServer != null) {
            try {
                this.mSocketServer.sendBroadcastServerMessage(0, new ConnectionCloseServerMessage());
            } catch (final Exception e) {
                Debug.e(e);
            }
            this.mSocketServer.terminate();
        }

        if(this.mServerConnector != null) {
            this.mServerConnector.terminate();
        }

        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
        switch(pKeyCode) {
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                return true;
        }
        return super.onKeyUp(pKeyCode, pEvent);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    public void addFace(final int pID, final float pX, final float pY) {
        final Scene scene = this.mEngine.getScene();
        /* Create the face and add it to the scene. */
        final Sprite face = new Sprite(0, 0, this.mFaceTextureRegion, this.getVertexBufferObjectManager());
        face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
        face.setUserData(pID);
        this.mFaces.put(pID, face);
        scene.registerTouchArea(face);
        scene.attachChild(face);
    }

    public void moveFace(final int pID, final float pX, final float pY) {
        /* Find and move the face. */
        final Sprite face = this.mFaces.get(pID);
        face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
    }

    private void initServerAndClient() {
        this.initServer();

        /* Wait some time after the server has been started, so it actually can start up. */
        try {
            Thread.sleep(500);
        } catch (final Throwable t) {
            Debug.e(t);
        }

        this.initClient();
        
        try {
            Thread.sleep(500);
        } catch (final Throwable t) {
            Debug.e(t);
        }
    }
    
    private void initServer() {
    	Thread serverThread = new Thread(new ServerThread());
    	serverThread.start();
    }

    private void initClient() {
    	Thread clientThread = new Thread(new ClientThread());
    	clientThread.start();
    }

    private void log(final String pMessage) {
        Debug.d(pMessage);
    }

    private void toast(final String pMessage) {
        this.log(pMessage);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MultiplayerExample.this, pMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private class ServerThread implements Runnable {

		@Override
		public void run() {
			
			MultiplayerExample.this.mSocketServer = new SocketServer<SocketConnectionClientConnector>(NetworkingConstants.SERVER_PORT, new ExampleClientConnectorListener(), new ExampleServerStateListener()) {
	            @Override
	            protected SocketConnectionClientConnector newClientConnector(final SocketConnection pSocketConnection) throws IOException {
	                return new SocketConnectionClientConnector(pSocketConnection);
	            }
	        };
	        
	        MultiplayerExample.this.mSocketServer.start();
	        
		}
    	
    }
    
    private class ClientThread implements Runnable {

		@Override
		public void run() {
			try {
				
				MultiplayerExample.this.mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(MultiplayerExample.this.mServerIP, NetworkingConstants.SERVER_PORT)), new ExampleServerConnectorListener());
				ServerConnector<SocketConnection> conn = MultiplayerExample.this.mServerConnector;
				// Finish client when disconnected from server
				conn.registerServerMessage(ServerMessageFlags.CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
	                @Override
	                public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
	                    MultiplayerExample.this.finish();
	                }
	            });

				// Add face to the client when receiving ADD_FACE message from the server
				conn.registerServerMessage(ServerMessageFlags.ADD_FACE, AddFaceServerMessage.class, new IServerMessageHandler<SocketConnection>() {
	                @Override
	                public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
	                    final AddFaceServerMessage addFaceServerMessage = (AddFaceServerMessage)pServerMessage;
	                    MultiplayerExample.this.addFace(addFaceServerMessage.mID, addFaceServerMessage.mX, addFaceServerMessage.mY);
	                }
	            });

				// Move face on the client when receiving MOVE_FACE message from the server
				conn.registerServerMessage(ServerMessageFlags.MOVE_FACE, MoveFaceServerMessage.class, new IServerMessageHandler<SocketConnection>() {
	                @Override
	                public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
	                    final MoveFaceServerMessage moveFaceServerMessage = (MoveFaceServerMessage)pServerMessage;
	                    MultiplayerExample.this.moveFace(moveFaceServerMessage.mID, moveFaceServerMessage.mX, moveFaceServerMessage.mY);
	                }
	            });

				// Start listening for messages
				conn.getConnection().start();
	        } catch (final Throwable t) {
	            Debug.e(t);
	        }
		}
    	
    }

    private class ExampleServerConnectorListener implements ISocketConnectionServerConnectorListener {
        @Override
        public void onStarted(final ServerConnector<SocketConnection> pConnector) {
            MultiplayerExample.this.toast("CLIENT: Connected to server.");
        }

        @Override
        public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
            MultiplayerExample.this.toast("CLIENT: Disconnected from Server...");
            MultiplayerExample.this.finish();
        }
    }

    private class ExampleServerStateListener implements ISocketServerListener<SocketConnectionClientConnector> {
        @Override
        public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
            MultiplayerExample.this.toast("SERVER: Started.");
        }

        @Override
        public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
            MultiplayerExample.this.toast("SERVER: Terminated.");
        }

        @Override
        public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer, final Throwable pThrowable) {
            Debug.e(pThrowable);
            MultiplayerExample.this.toast("SERVER: Exception: " + pThrowable);
        }
    }

    private class ExampleClientConnectorListener implements ISocketConnectionClientConnectorListener {
        @Override
        public void onStarted(final ClientConnector<SocketConnection> pConnector) {
            MultiplayerExample.this.toast("SERVER: Client connected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
        }

        @Override
        public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
            MultiplayerExample.this.toast("SERVER: Client disconnected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
        }
    }
}