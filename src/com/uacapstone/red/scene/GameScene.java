package com.uacapstone.red.scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import android.annotation.SuppressLint;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.uacapstone.red.base.BaseScene;
import com.uacapstone.red.manager.SceneManager;
import com.uacapstone.red.manager.SceneManager.SceneType;
import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.NetworkingConstants.MessageFlags;
import com.uacapstone.red.networking.PhysicsBodyState;
import com.uacapstone.red.networking.PlayerState;
import com.uacapstone.red.networking.WizardPlayerState;
import com.uacapstone.red.networking.messaging.FlaggedNetworkMessage;
import com.uacapstone.red.networking.messaging.NetworkMessage;
import com.uacapstone.red.networking.messaging.PlayerChangeDirectionMessage;
import com.uacapstone.red.networking.messaging.PlayerJumpMessage;
import com.uacapstone.red.object.Joe;
import com.uacapstone.red.object.Avatar;
import com.uacapstone.red.object.AvatarData;
import com.uacapstone.red.networking.messaging.PlayerStateMessage;
import com.uacapstone.red.networking.messaging.WizardPlayerStateMessage;
import com.uacapstone.red.object.Rabbit;
import com.uacapstone.red.object.Wizard;
//github.com/capstone-ua-redepsilon/projectred.git

@SuppressLint("DefaultLocale")
public class GameScene extends BaseScene implements IOnSceneTouchListener
{
	final Date mLastGameStateMessageReceived = new Date();
	
	private boolean mIsGamePaused;
	
    @Override
    public void createScene()
    {
        createBackground();
        createHUD();
        createPhysics();
        loadLevel(2);
        createCongratsText();
        setOnSceneTouchListener(this);
    }

    @Override
    public void onBackKeyPressed()
    {
    	quit();
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene()
    {
        
    	activity.leaveRoom();
    	resetCamera();

        // TODO code responsible for disposing scene
        // removing all game scene objects.
    }
    
    private void createBackground()
    {
        setBackground(new Background(.68f, .89f, 1.0f));
    }
    
    private void quit()
    {
    	disposeScene();
        SceneManager.getInstance().loadMenuScene(engine);
    }

    private void createHUD()
    {
        gameHUD = new HUD();
        
        // CREATE TEXT
		flagText = new Text(20, 420, resourceManager.font, "Flags: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        timeText = new Text(20, 370, resourceManager.font, "Time: 00:00", new TextOptions(HorizontalAlign.LEFT), vbom);
        flagText.setAnchorCenter(0, 0);
        timeText.setAnchorCenter(0, 0);
        flagText.setText("Flags: 0");
        startTime = System.currentTimeMillis();
        updateTimeDisplay();
        gameHUD.attachChild(flagText);
        gameHUD.attachChild(timeText);
        
        camera.setHUD(gameHUD);
    }

    private void createPhysics()
    {
        physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
        physicsWorld.setContactListener(contactListener());
        registerUpdateHandler(physicsWorld);
        //DebugRenderer debug = new DebugRenderer(physicsWorld, vbom);
        //this.attachChild(debug);
        FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
        FIXTURE_DEF.filter.categoryBits = 0x0001;
        
        registerUpdateHandler(new IUpdateHandler() {
        	
        	final Date timeOfLastMessage = new Date();
	    	@Override
	    	public void onUpdate(float pSecondsElapsed) {
	    		// wait for the game to be resumed
	    		if (mIsGamePaused) return;
	    	    updateTimeDisplay();
	    	    
	    	    Date currentTime = new Date();
	    	    if (activity.isMultiplayer()) {
		    	    if (/*activity.isHost() &&*/ (currentTime.getTime() - timeOfLastMessage.getTime()) > 250 ) {
		    	    	timeOfLastMessage.setTime(currentTime.getTime());
		    	    	sendGameStateUpdate();
		    	    }
	    	    }
	    	}

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
	    	
        });
    }
    
    private PlayerState createStateForPlayer(Avatar p) {
    	PlayerState pState = new PlayerState();
    	PhysicsBodyState bodyState = new PhysicsBodyState();
    	bodyState.x = p.getBody().getPosition().x;
    	bodyState.y = p.getBody().getPosition().y;
    	bodyState.velocityX = p.getBody().getLinearVelocity().x;
    	bodyState.velocityY = p.getBody().getLinearVelocity().y;
    	
    	pState.bodyState = bodyState;
    	pState.direction = p.getRunDirection();
    	pState.id = p.getId();
    	pState.playerFeetDown = p.getNumberOfFeetDown();
    	pState.hasJumped = p.getHasJumped();
    	
    	return pState;
    }
    
    protected void sendGameStateUpdate() {
    	// State updates for local player
    	
    	PlayerState genericState = createStateForPlayer(this.avatar);
    	
    	NetworkMessage msgToSend = null;
    	
    	// State updates for other entities (abilities)
    	if (Wizard.class.isInstance(this.avatar) )
    	{
    		Wizard w = (Wizard)this.avatar;
    		WizardPlayerStateMessage stateMessage = new WizardPlayerStateMessage();
    		
    		WizardPlayerState wizardState = new WizardPlayerState();
    		wizardState.playerState = genericState;
    		if (w.mDidJustCastTornado) {
    			wizardState.didCastTornado = w.mDidJustCastTornado;
    			w.mDidJustCastTornado = false;
    		}
    		
    		stateMessage.state = wizardState;
    		
    		msgToSend = stateMessage;
    	}
    	else if (Joe.class.isInstance(this.avatar) )
    	{
    		PlayerStateMessage stateMessage = new PlayerStateMessage();
    		stateMessage.state = genericState;
    		
    		msgToSend = stateMessage;
    	}
    	else 
    	{
    		PlayerStateMessage stateMessage = new PlayerStateMessage();
    		stateMessage.state = genericState;
    		
    		msgToSend = stateMessage;
    	}
    	
    	msgToSend.sequenceNumber = messageSequenceNumber++;
    	activity.sendMessage(new FlaggedNetworkMessage(msgToSend));
	}

	private void captureFlag()
    {
		setFlags(flagsRemaining-1);
    }
	
	private void resetFlags()
	{
        setFlags(numFlags);
	}
	
	private void setFlags(int n)
	{
		flagsRemaining = n;
        flagText.setText("Flags: " + flagsRemaining);
        if (flagsRemaining == 0)
        {
            activity.updateLeaderboard(System.currentTimeMillis() - startTime);
        	displayCongratsText();
        	Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    quit();
                }
            }, CelebrationTimeInMilliseconds);
        }
	}
    
    private void updateTimeDisplay()
    {
    	long currentTime = System.currentTimeMillis();
    	long runTimeMill = currentTime - startTime;
    	long second = (runTimeMill / 1000) % 60;
    	long minute = (runTimeMill / (1000 * 60)) % 60;
//    	long hour = (runTimeMill / (1000 * 60 * 60)) % 24;

    	String time = String.format("%02d:%02d", minute, second);
    	timeText.setText("Time: " + time);
    }
    
    private void loadLevel(int levelID)
    {
        final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
            {
                final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
                final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
                minPlayers = SAXUtils.getIntAttributeOrThrow(pAttributes, "numPlayers");
                numPlayers = activity.getNumPlayers();
                avatars = new Avatar[numPlayers];
                mId = activity.getNormalizedId();
                
                camera.setBounds(0, 0, width, height); // here we set camera bounds
                camera.setBoundsEnabled(true);

                return GameScene.this;
            }
        });
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
        {
        	int playerIndex = 0;
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
            {
                final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
                final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
                final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
                
                Sprite levelObject = null;
                
                if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
                {
                    levelObject = new Sprite(x, y, resourceManager.platform1_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BLOCK))
                {
                    levelObject = new Sprite(x, y, resourceManager.block_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("block");
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
                {
                    levelObject = new Sprite(x, y, resourceManager.platform2_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("platform2");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SWITCH))
                {
                    levelObject = new Sprite(x, y, resourceManager.switch_region, vbom);
                    hiddenPlatformSprite = new Sprite(x+100, y+100, resourceManager.platform3_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("switch");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLAG))
                {
                	numFlags++;
                    levelObject = new Sprite(x, y, resourceManager.flag_region, vbom)
                    {
                        @Override
                        protected void onManagedUpdate(float pSecondsElapsed) 
                        {
                            super.onManagedUpdate(pSecondsElapsed);

                            for (Avatar p : avatars)
                            {
                                if (p.collidesWith(this))
                                {
                                    captureFlag();
                                    this.setVisible(false);
                                    this.setIgnoreUpdate(true);
                                }
                            }
                        }
                    };
                    levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
                }     
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_JOE))
                {
                	if (playerIndex < numPlayers)
                	{
                    	Avatar p = new Joe(x, y, vbom, camera, physicsWorld, playerIndex)
                    	{
                    		@Override
                    		public void onDie()
                    		{
                    		    //getBody().setTransform(getStartPosition(), getBody().getAngle());
                    		}
                    	};
                    	levelObject = p;
                    	avatars[playerIndex++] = p;	
                	}
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WIZARD))
                {
                	if (playerIndex < numPlayers)
                	{
                    	Avatar p = new Wizard(x, y, vbom, camera, physicsWorld, playerIndex)
                    	{
                    		@Override
                    		public void onDie()
                    		{
                    		    //getBody().setTransform(getStartPosition(), getBody().getAngle());
                    		}
                    	};
                    	levelObject = p;
                    	avatars[playerIndex++] = p;	
                	}
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_RABBIT))
                {
                	if (playerIndex < numPlayers)
                	{
                    	Avatar p = new Rabbit(x, y, vbom, camera, physicsWorld, playerIndex)
                    	{
                    		@Override
                    		public void onDie()
                    		{
                    		    //getBody().setTransform(getStartPosition(), getBody().getAngle());
                    		}
                    	};
                    	levelObject = p;
                    	avatars[playerIndex++] = p;	
                	}
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                if (levelObject != null)
                {
	                levelObject.setCullingEnabled(true);
	
                }
                return levelObject;
            }
        });

        levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");

        resetFlags();
        avatar = avatars[mId];
        avatar.setupHud();
        camera.setChaseEntity(avatar);
    }
    
    private ContactListener contactListener()
    {
        ContactListener contactListener = new ContactListener()
        {
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                AvatarData pd1, pd2, pd = null, bd1, bd2 ;
                Fixture ft = null;
                Fixture o = null;

                if (x1.getUserData() != null && x1.getUserData() instanceof AvatarData)
                {
                	pd1 = (AvatarData)x1.getUserData();
                	bd1 = (AvatarData)x1.getBody().getUserData();
                	
                	Debug.d("Player description: "+ pd1.mDescription);
                	Debug.d("Body description: "+ bd1.mDescription);
                	
                	if (pd1.mDescription == "feet")
                	{
                		pd = pd1;
                    	ft = x1;
                    	o = x2;
                	}
                }
                else if (x2.getUserData() != null && x2.getUserData() instanceof AvatarData)
                {
                	pd2 = (AvatarData)x2.getUserData();
                	bd2 = (AvatarData)x2.getBody().getUserData();
                	
                	Debug.d("Player description: "+ pd2.mDescription);
                	Debug.d("Body description: "+ bd2.mDescription);
                	
                	if (pd2.mDescription == "feet")
                	{
                		pd = pd2;
                    	ft = x2;
                    	o = x1;
                	}
                }
            	if (ft != null && pd != null)
            	{
            		if ((ft.getFilterData().maskBits & o.getFilterData().categoryBits) == o.getFilterData().categoryBits)
            		{
            			avatars[Integer.parseInt(pd.mId)].increaseFootContacts();
            		}
                    if (o.getBody().getUserData() != null && o.getBody().getUserData().equals("switch"))
                    {
                    	mNumPlayersOnSwitch++;
                    	if (mNumPlayersOnSwitch == 1)
                    	{
                    		physicsWorld.postRunnable(new Runnable() {

								@Override
								public void run() {
									final Sprite levelObject = hiddenPlatformSprite;
					                hiddenPlatformBody = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					                hiddenPlatformBody.setUserData("platform3");
					                hiddenPlatformSprite.setCullingEnabled(true);
					                attachChild(hiddenPlatformSprite);
								}
                    			
                    		});
                    	}
                    }
                }
            }

            public void endContact(Contact contact)
            {
            	final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                AvatarData pd1, pd2, pd = null;
                Fixture ft = null;
                Fixture o = null;

                if (x1.getUserData() != null && x1.getUserData() instanceof AvatarData)
                {
                	pd1 = (AvatarData)x1.getUserData();
                	if (pd1.mDescription == "feet")
                	{
                		pd = pd1;
                    	ft = x1;
                    	o = x2;
                	}
                }
                else if (x2.getUserData() != null && x2.getUserData() instanceof AvatarData)
                {
                	pd2 = (AvatarData)x2.getUserData();
                	if (pd2.mDescription == "feet")
                	{
                		pd = pd2;
                    	ft = x2;
                    	o = x1;
                	}
                }
            	if (ft != null && pd != null)
            	{
                    avatars[Integer.parseInt(pd.mId)].decreaseFootContacts();
                    if (o.getBody().getUserData() != null && o.getBody().getUserData().equals("switch"))
                    {
                    	mNumPlayersOnSwitch--;
                    	if (mNumPlayersOnSwitch == 0 && hiddenPlatformBody != null)
                    	{
                    		physicsWorld.postRunnable(new Runnable() {

								@Override
								public void run() {
					                physicsWorld.destroyBody(hiddenPlatformBody);
					                hiddenPlatformBody = null;
					                detachChild(hiddenPlatformSprite);
								}
                    			
                    		});
                    	}
                    }
                }
            }

            public void preSolve(Contact contact, Manifold oldManifold)
            {

            }

            public void postSolve(Contact contact, ContactImpulse impulse)
            {
            	
            }
        };
        return contactListener;
    }
    
    private void createCongratsText()
    {
    	congratsText = new Text(activity.getScreenHalfWidth(), activity.getScreenHalfHeight(), resourceManager.font, "Congratulations!", vbom);
    }

    private void displayCongratsText()
    {
    	gameHUD.attachChild(congratsText);
        congratsDisplayed = true;
    }
    
    private HUD gameHUD;
    private Text flagText;
    private Text timeText;
    private long startTime;
    private PhysicsWorld physicsWorld;
    private Avatar avatar;
    private int mId;
    private int numFlags = 0;
    private int flagsRemaining;
    private static final int MaxPlayers = 4;
    private Avatar[] avatars;
	private int numPlayers;
	private int minPlayers = 0;
    private Vector2 lastTouchCoords;
    private static int DRAG_DISTANCE = 50;
    private static double HORIZ_MOVE_TOUCH_PERCENTAGE = .2;
    private static double VERT_MOVE_TOUCH_PERCENTAGE = .5;
    private boolean hasJumped = false;
    private Text congratsText;
    private boolean congratsDisplayed = false;
	private Body hiddenPlatformBody = null;
	private Sprite hiddenPlatformSprite;
	private int mNumPlayersOnSwitch = 0;
	
	private static final long CelebrationTimeInMilliseconds = 5000;
	private static final Vector2 TornadoForce = new Vector2(0.0f, 8.0f);
    
    private static final String TAG_ENTITY = "entity";
    private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
    private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
    private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
        
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BLOCK = "block";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SWITCH = "switch";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLAG = "flag";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_JOE = "joe";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WIZARD = "wizard";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_RABBIT = "rabbit";
    
    private static FixtureDef FIXTURE_DEF;
    
    private int messageSequenceNumber = 0;
    
//    public void sendPlayerJumpMessage() {
//    	PlayerJumpMessage message = new PlayerJumpMessage();
//    	message.sequenceNumber = messageSequenceNumber++;
//    	message.playerId = mId;
//    	
//		activity.sendMessage(new FlaggedNetworkMessage(message));
//    }
    
//    public void sendPlayerChangeDirectionMessage(int dir) {
//    	PlayerChangeDirectionMessage message = new PlayerChangeDirectionMessage();
//    	message.sequenceNumber = messageSequenceNumber++;
//    	message.playerId = mId;
//    	message.direction = dir;
//    	
//    	activity.sendMessage(new FlaggedNetworkMessage(message));
//    }
    
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// Don't accept inputs while the game is paused
		if (this.mIsGamePaused) return false;
		
		if (pSceneTouchEvent.isActionDown())
		{
			float x = pSceneTouchEvent.getX() - camera.getXMin();
			float y = pSceneTouchEvent.getY() - camera.getYMin();
			int xdir = 0;
			lastTouchCoords = new Vector2(x, y);
			if (y < camera.getHeight()*VERT_MOVE_TOUCH_PERCENTAGE)
			{
				if (x < camera.getWidth()*HORIZ_MOVE_TOUCH_PERCENTAGE)
				{
					xdir = -1;
				}
				else if (x > camera.getWidth() - camera.getWidth()*HORIZ_MOVE_TOUCH_PERCENTAGE)
				{
					xdir = 1;
				}
			}
			avatar.setRunDirection(xdir);
			
			sendGameStateUpdate();
			
//			sendPlayerChangeDirectionMessage(xdir);
			
			
			
//			byte[] message = new byte[8];
//			message[0] = (byte)mId;
//			message[1] = (byte)0;
//			message[2] = (byte)xdir;
//			activity.sendMessage(message);
		}
		else if (pSceneTouchEvent.isActionMove())
		{
			float x = pSceneTouchEvent.getX() - camera.getXMin();
			float y = pSceneTouchEvent.getY() - camera.getYMin();
			Vector2 currentCoords = new Vector2(x, y);
			Vector2 displacement = currentCoords.sub(lastTouchCoords);
			
			if (Math.abs(displacement.y) > DRAG_DISTANCE) {
				avatar.jump(true);
				sendGameStateUpdate();
			}
		}
		else if (pSceneTouchEvent.isActionUp())
		{
			if (avatar.isRunning())
			{
				avatar.setRunDirection(0);
				sendGameStateUpdate();
//				sendPlayerChangeDirectionMessage(0);
			}
		}
		return false;
	}
	
//	void handlePlayerChangeDirectionMessage(PlayerChangeDirectionMessage msg) {
//		players[msg.playerId].setRunDirection(msg.direction);
//	}
//	void handlePlayerJumpMessage(PlayerJumpMessage msg) {
//		players[msg.playerId].jump();
//	}
//	void handleSetHostMessage(SetHostMessage msg) {
//		activity.setHost(msg.participantId);
//	}
	
	void handlePlayerStateMessage(PlayerStateMessage message) {
		
		for (Avatar p: this.avatars) {
			if (p.getId() == message.state.id) {
				message.state.apply(p);
			}
		}
	}
	
	void handleWizardPlayerStateMessage(WizardPlayerStateMessage message) {
		
		// handle wizard thingies
		for (Avatar p: this.avatars) {
			if (p.getId() == message.state.playerState.id) {
				message.state.apply((Wizard)p);
			}
		}
		
	}
	
	public void createTornado(int playerId, float x, float y, int speed, long duration, final Runnable onComplete) {
		final AnimatedSprite tornado = new AnimatedSprite(x, y, resourceManager.tornado_region, vbom);
    	tornado.animate(100);
    	FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
    	//fixtureDef.filter.categoryBits = 0x0004;
    	//fixtureDef.filter.maskBits = ~0x0002;
    	final Body tornadoBody = PhysicsFactory.createBoxBody(this.physicsWorld, tornado, BodyType.KinematicBody, fixtureDef);
        this.attachChild(tornado);
    	tornadoBody.setUserData(new AvatarData(playerId, "tornado", tornado));
    	tornadoBody.setFixedRotation(true);
    	tornadoBody.setLinearVelocity(new Vector2(0, 6));
    	
    	final PhysicsConnector physConn = new PhysicsConnector(tornado, tornadoBody, true, false)
        {
            @Override
            public void onUpdate(float pSecondsElapsed)
            {
                super.onUpdate(pSecondsElapsed);
            }
        };
        physicsWorld.registerPhysicsConnector(physConn);
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
        	@Override
        	public void run()
        	{
        		physicsWorld.postRunnable(new Runnable()
        		{
        			public void run()
        			{
        				GameScene.this.physicsWorld.unregisterPhysicsConnector(physConn);
        				GameScene.this.physicsWorld.destroyBody(tornadoBody);
        				GameScene.this.detachChild(tornado);
        			}
        		});
        		
        		onComplete.run();
        	}
        }, duration);
	}
	
//	public final LinkedList<NetworkMessage> unconfirmedMessages = new LinkedList<NetworkMessage>();
	
	public void handleMessage(FlaggedNetworkMessage message) throws IOException
	{
//		NetworkMessage m = null;
		switch (message.messageFlag) {
		case MessageFlags.MESSAGE_FROM_SERVER_PLAYER_STATE:
			if (new Date(message.timestamp).after(mLastGameStateMessageReceived)) {
				mLastGameStateMessageReceived.setTime(message.timestamp);
				handleMessage(NetworkingConstants.messagePackInstance.read(message.messageBytes, PlayerStateMessage.class));
			}
			break;
		case MessageFlags.MESSAGE_WIZARD_PLAYER_STATE:
			if (new Date(message.timestamp).after(mLastGameStateMessageReceived)) {
				mLastGameStateMessageReceived.setTime(message.timestamp);
				handleMessage(NetworkingConstants.messagePackInstance.read(message.messageBytes, WizardPlayerStateMessage.class));
			}
			break;
//		case MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_DIRECTION:
//			if (m == null)
//				m = NetworkingConstants.messagePackInstance.read(message.messageBytes, PlayerChangeDirectionMessage.class);
//		case MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_JUMP:
//			if (m == null)
//				m = NetworkingConstants.messagePackInstance.read(message.messageBytes, PlayerJumpMessage.class);
//			
//			if (m.sequenceNumber > messageSequenceNumber) {
//				handleMessage(m);
//				messageSequenceNumber = m.sequenceNumber;
//			}
		}
		return;
	}
	
	public void handleMessage(NetworkMessage message) {
		switch (message.getFlag()) {
//		case MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_DIRECTION:
//			handlePlayerChangeDirectionMessage((PlayerChangeDirectionMessage)message);
//			break;
//		case MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_JUMP:
//			handlePlayerJumpMessage((PlayerJumpMessage)message);
//			break;
		case MessageFlags.MESSAGE_FROM_SERVER_PLAYER_STATE:
			handlePlayerStateMessage((PlayerStateMessage)message);
			break;
		case MessageFlags.MESSAGE_WIZARD_PLAYER_STATE:
			handleWizardPlayerStateMessage((WizardPlayerStateMessage)message);
			break;
		}
	}
	

	public void resumeGame() {
		if (this.mIsGamePaused) {
			this.engine.start();
			this.mIsGamePaused = false;
		}
	}
	
	public void pauseGame() {
		if (!this.mIsGamePaused) {
			this.engine.stop();
			this.mIsGamePaused = true;
		}
	}
}
