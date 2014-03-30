package com.uacapstone.red;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LandingActivity extends Activity {

	// Listeners
	final OnClickListener clickStart = new OnClickListener() {
		@Override
		public void onClick(View v) {
//			setContentView(R.layout.activity_game);
			NetworkHandler handler = new NetworkHandler();
			try {
				handler.runtest();
				handler.testZMQ();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	};
	
	// Main Code
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_landing);
		
		Button btnStart = (Button) findViewById(R.id.btnStart);
		
		btnStart.setOnClickListener(clickStart);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.landing, menu);
		return true;
	}

}
