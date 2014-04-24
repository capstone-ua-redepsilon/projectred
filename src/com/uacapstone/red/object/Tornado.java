package com.uacapstone.red.object;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.uacapstone.red.manager.ResourceManager;
import com.uacapstone.red.scene.GameScene;

public abstract class Tornado extends AnimatedSprite implements ICollisionTarget {

	private Body mBody;
	private PhysicsConnector mPhysicsConnector;
	private PhysicsWorld mPhysicsWorld;
	private GameScene mGameScene;
	
	private boolean mDidComplete = false;
	
	public Body getBody() {
		return mBody;
	}
	
	public PhysicsConnector getPhysicsConnector() {
		return mPhysicsConnector;
	}
	
	public boolean didComplete() {
		return mDidComplete;
	}
	
	public Tornado(float pX, float pY, final GameScene gameScene, int playerId, int speed)
	{
		super(pX, pY, ResourceManager.getInstance().tornado_region, ResourceManager.getInstance().vbom);
		
		mGameScene = gameScene;
		mPhysicsWorld = gameScene.getPhysicsWorld();
		
		this.animate(100);
		FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0, true);
		fixtureDef.filter.groupIndex = 1;
		fixtureDef.filter.categoryBits = 0x0004;
		fixtureDef.filter.maskBits = 0x0003;
		mBody = PhysicsFactory.createBoxBody(mPhysicsWorld, this, BodyType.KinematicBody, fixtureDef);
		
		mBody.setFixedRotation(true);
		mBody.setLinearVelocity(new Vector2(0, speed));
		
	    mPhysicsWorld.postRunnable(new Runnable() {
			
			@Override
			public void run() {
				mPhysicsConnector = new PhysicsConnector(Tornado.this, mBody, true, false)
			    {
			        @Override
			        public void onUpdate(float pSecondsElapsed)
			        {
			            super.onUpdate(pSecondsElapsed);
			        }
			    };
			    
				mPhysicsWorld.registerPhysicsConnector(mPhysicsConnector);
			}
		});
	    
	    
	}
	
	public void onComplete() {
		mDidComplete = true;
		mPhysicsWorld.postRunnable(new Runnable() {
			
			@Override
			public void run() {
				mPhysicsWorld.unregisterPhysicsConnector(mPhysicsConnector);
				mPhysicsWorld.destroyBody(mBody);
				mGameScene.detachChild(Tornado.this);
			}
		});
	}
}
