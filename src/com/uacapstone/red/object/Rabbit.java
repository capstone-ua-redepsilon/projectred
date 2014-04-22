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
public abstract class Rabbit extends Player
{
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public Rabbit(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld, int id)
    {
        super(pX, pY, vbo, camera, physicsWorld, ResourceManager.getInstance().rabbit_region, id);
        speed = 8;
        mFixtureDef.filter.categoryBits |= 0x0004;
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
        final long[] ANIMATE = new long[] { 100, 100, 100, 100, 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        	if (isOnGround())
        	{
        		animate(ANIMATE, 8, 13, true);
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
        final long[] ANIMATE = new long[] { 100, 100, 100, 100, 100, 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        }
    	animate(ANIMATE, 16, 22, false);
    }
    @Override
    protected void animateLand(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        }
    	animate(ANIMATE, new int[] {23, 0}, false);
    }
    @Override
    protected void animateFall(float direction)
    {
        if (direction != 0)
        {
        	setScaleX(direction);
        }
        stopAnimation();
        setCurrentTileIndex(1);
    }
    
    @Override
    protected void setupPhysics()
    {
    	
    }
    
    @Override
    protected void setupHud()
    {
    }
}