package com.uacapstone.red.object;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.AnimatedSpriteMenuItem;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.uacapstone.red.manager.ResourceManager;
import com.uacapstone.red.manager.SceneManager;
import com.uacapstone.red.scene.GameScene;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class Wizard extends Avatar
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
	
    public boolean mDidJustCastTornado = false;

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
    	animate(ANIMATE, 5, 6, false);
    }
    @Override
    protected void animateLand(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100 };
    	animate(ANIMATE, new int[] {7, 0}, false);
    }
    @Override
    protected void animateFall(float direction)
    {
        stopAnimation();
        setCurrentTileIndex(6);
    }
    
    protected void animateCast()
    {
    	// TODO
    }
    
    @Override
    protected void setupPhysics()
    {
    	
    }
    
    @Override
    public void setupHud()
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
    	        	wizard.castTornado(true);
    	        	return true;
    	        default:
    	            return false;
    		    }
    		}
    	});
    	menuScene.setBackgroundEnabled(false);
    	IMenuItem scaledTornadoMenuItem = new ScaleMenuItemDecorator(tornadoMenuItem, 2.0f, 2.0f);
    	scaledTornadoMenuItem.setX(resourceManager.activity.getScreenWidth()-scaledTornadoMenuItem.getWidth()-50);
    	scaledTornadoMenuItem.setY(resourceManager.activity.getScreenHeight()-scaledTornadoMenuItem.getHeight()-50);
    	menuScene.addMenuItem(scaledTornadoMenuItem);
    	hud.setChildScene(menuScene);
    }
    
    public void castTornado(boolean shouldCheckConditions) {
    	if (!shouldCheckConditions || mCanCast) {
    		mDidJustCastTornado = true;
    		mCanCast = false;
        	animateCast();
            final GameScene gameScene = SceneManager.getInstance().getGameScene();
            
            gameScene.createTornado(mId, mX, mY, 6, CastCooldownInMilliseconds, new Runnable() {
            	@Override
            	public void run()
            	{
            		mCanCast = true;
            	}
            });
    	}
    }
    
    private static final int CastCooldownInMilliseconds = 3000;
    private boolean mCanCast;
}