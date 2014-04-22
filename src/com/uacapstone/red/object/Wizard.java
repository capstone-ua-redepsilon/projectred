package com.uacapstone.red.object;

import java.util.Timer;
import java.util.TimerTask;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.AnimatedSpriteMenuItem;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.uacapstone.red.manager.ResourceManager;
import com.uacapstone.red.manager.SceneManager;
import com.uacapstone.red.scene.GameScene;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class Wizard extends Player
{
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public Wizard(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld, int id)
    {
        super(pX, pY, vbo, camera, physicsWorld, ResourceManager.getInstance().wizard_region, id);
        mCanCast = true;
    }
    
    // ---------------------------------------------
    // VARIABLES
    // ---------------------------------------------
	

    // ---------------------------------------------
    // LOGIC
    // ---------------------------------------------
    
    @Override
    protected void animateRun(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100, 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        	if (isOnGround())
        	{
        		animate(ANIMATE, 1, 4, true);
        	}
        }
        else
        {
        	if (isOnGround())
        	{
            	stopAnimation();
            	this.setCurrentTileIndex(0);
        	}
        }
    } 
    @Override
    protected void animateJump(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        }
    	animate(ANIMATE, 5, 6, false);
    }
    @Override
    protected void animateLand(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        }
    	animate(ANIMATE, new int[] {7, 0}, false);
    }
    @Override
    protected void animateFall(float direction)
    {
        if (direction != 0)
        {
        	setScaleX(direction);
        }
        stopAnimation();
        setCurrentTileIndex(6);
    }
    
    @Override
    protected void setupPhysics()
    {
    	
    }
    
    @Override
    protected void setupHud()
    {
    	ResourceManager resourceManager = ResourceManager.getInstance();
    	final HUD hud = mCamera.getHUD();
    	final Wizard wizard = this;
    	AnimatedSpriteMenuItem tornadoMenuItem = new AnimatedSpriteMenuItem(0, resourceManager.tornado_region, mVbom);
    	MenuScene menuScene = new MenuScene(mCamera, new IOnMenuItemClickListener() {
    		@Override
    		public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
    		{
    	        switch(pMenuItem.getID())
    	        {
    	        case 0:
    	        	wizard.tryCastTornado();
    	        	return true;
    	        default:
    	            return false;
    		    }
    		}
    	});
    	menuScene.setBackgroundEnabled(false);
    	IMenuItem scaledTornadoMenuItem = new ScaleMenuItemDecorator(tornadoMenuItem, 2.0f, 2.0f);
    	scaledTornadoMenuItem.setX(resourceManager.activity.getScreenWidth()-scaledTornadoMenuItem.getWidth()-10);
    	scaledTornadoMenuItem.setY(resourceManager.activity.getScreenHeight()-scaledTornadoMenuItem.getHeight()-10);
    	menuScene.addMenuItem(scaledTornadoMenuItem);
    	hud.setChildScene(menuScene);
    }
    
    public boolean tryCastTornado()
    {
    	boolean result = mCanCast;
    	if (mCanCast)
    		castTornado();
    	return result;
    }
    
    private void castTornado()
    {
    	mCanCast = false;
    	final ResourceManager resourceManager = ResourceManager.getInstance();
        final GameScene gameScene = SceneManager.getInstance().getGameScene();
    	final AnimatedSprite tornado = new AnimatedSprite(mX, mY, resourceManager.tornado_region, mVbom);
    	tornado.animate(100);
    	FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
    	fixtureDef.filter.maskBits = 0x0004;
    	final Body tornadoBody = PhysicsFactory.createBoxBody(mPhysicsWorld, tornado, BodyType.KinematicBody, fixtureDef);
        gameScene.attachChild(tornado);
    	tornadoBody.setUserData(new PlayerData(mId, "tornado"));
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
        mPhysicsWorld.registerPhysicsConnector(physConn);
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
        	@Override
        	public void run()
        	{
        		mPhysicsWorld.postRunnable(new Runnable()
        		{
        			public void run()
        			{
        				mPhysicsWorld.unregisterPhysicsConnector(physConn);
        				mPhysicsWorld.destroyBody(tornadoBody);
        				gameScene.detachChild(tornado);
        			}
        		});
        		mCanCast = true;
        	}
        }, CastCooldownInMilliseconds);
    }
    
    private static final int CastCooldownInMilliseconds = 3000;
    private boolean mCanCast;
}