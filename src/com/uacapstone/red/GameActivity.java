package com.uacapstone.red;

import java.io.IOException;

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
import org.andengine.ui.activity.BaseGameActivity;

import android.view.KeyEvent;

import com.uacapstone.red.manager.ResourcesManager;
import com.uacapstone.red.manager.SceneManager;
import com.uacapstone.red.networking.client.Client;
import com.uacapstone.red.networking.messaging.NetworkMessage;
import com.uacapstone.red.networking.server.Server;
import com.uacapstone.red.networking.server.ServerMovePlayerMessage;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public class GameActivity extends BaseGameActivity
{
	private static final String LOCALHOST_IP = "127.0.0.1";
	private static final String SERVER_PORT = "4444";
	
	private static Server server;
	private static Client client;
	
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
    	
    	
    	try {
    		server = new Server(SERVER_PORT);
    		System.out.println("Creating server in new thread...");
    		new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						server.startServer();
						System.out.println("Created!");
					} catch (Exception e) {
			    		System.err.println(e);
			    	}
					
				}
    			
    		}).start();
    		
    		System.out.println("Sleeping...");
    		Thread.sleep(2500);
    	} catch (Exception e) {
    		System.err.println(e);
    	}
    	
    	
    	
    	try {
    		System.out.println("Creating client");
    		client = new Client(LOCALHOST_IP, SERVER_PORT);
    		System.out.println("Connecting to server");
    		client.connectToServer();
    		System.out.println("Sending Test Message...");
    		client.sendMessage(new ServerMovePlayerMessage(0, 1, 2));
    	} catch (Exception e) {
    		System.err.println(e);
    	}
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
}
