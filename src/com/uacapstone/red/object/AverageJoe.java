package com.uacapstone.red.object;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.uacapstone.red.manager.ResourceManager;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class AverageJoe extends Player
{
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public AverageJoe(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld, int id)
    {
        super(pX, pY, vbo, camera, physicsWorld, ResourceManager.getInstance().player_region, id);
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
        final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        	if (isOnGround())
        	{
        		animate(PLAYER_ANIMATE, 0, 2, true);
        	}
        }
        else
        {
        	stopAnimation();
        	this.setCurrentTileIndex(0);
        }
    }
    @Override
    protected void animateJump(float direction)
    {
        if (direction != 0)
        {
        	setScaleX(direction);
        }
    	stopAnimation();
    	this.setCurrentTileIndex(0);
    }
    @Override
    protected void animateLand(float direction)
    {
        if (direction != 0)
        {
        	setScaleX(direction);
        }
    	stopAnimation();
    	this.setCurrentTileIndex(0);
    }
    @Override
    protected void animateFall(float direction)
    {
        if (direction != 0)
        {
        	setScaleX(direction);
        }
    	stopAnimation();
    	this.setCurrentTileIndex(0);
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