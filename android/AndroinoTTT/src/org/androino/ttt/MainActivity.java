/*		
* Copyright (C) 2011 Androino authors		
*		
* Licensed under the Apache License, Version 2.0 (the "License");		
* you may not use this file except in compliance with the License.		
* You may obtain a copy of the License at		
*		
*      http://www.apache.org/licenses/LICENSE-2.0		
*		
* Unless required by applicable law or agreed to in writing, software		
* distributed under the License is distributed on an "AS IS" BASIS,		
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.		
* See the License for the specific language governing permissions and		
* limitations under the License.		
*/

package org.androino.ttt;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
	
	private static final String TAG = "MainActivity";
	private TicTacToe mTTT;
	
	public MainActivity() {
		this.mTTT = new TicTacToe(this);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		final Button button = (Button) findViewById(R.id.Button);
		final RadioButton radio = (RadioButton) findViewById(R.id.RadioButton01);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (radio.isChecked()){
					mTTT.stop();
				} else 
					mTTT.start();
			}
		});
		final Button sendB = (Button) findViewById(R.id.SendButton);
		sendB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					TextView txt = (TextView) findViewById(R.id.NumberText);
					int number = Integer.parseInt(""+txt.getText()); 
					mTTT.developmentSendMessage(number);
				} catch (Exception e) {
					showDebugMessage("ERROR happened, check number format",true);
				}
			}
		});
		final Button send2Server = (Button) findViewById(R.id.SendButton1);
		send2Server.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					TextView txt = (TextView) findViewById(R.id.NumberText1);
					int number = Integer.parseInt(""+txt.getText()); 
					mTTT.developmentSendServerMessage(number);
				} catch (Exception e) {
					showDebugMessage("ERROR happened, check number format",true);
				}
			}
		});

	
	}

	protected void onPause() {
		Log.i("MainActivity:lifecycle", "onPause");
		super.onPause();

		//this.mTTT.stop();
		// restore volume
	}

	protected void onResume() {
		Log.i("MainActivity:lifecycle", "onResume");
		super.onResume();

		//this.mTTT.start();
		// setting max volume

	}
	void showDebugMessage(String message, boolean showToast){
		try {
			if (showToast){
				Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			} else {
				TextView txt = (TextView) findViewById(R.id.DebugText);
				String info = txt.getText().toString();
				if (info.length()> 300)
					info = info.substring(0, 30);
				info = message + "\n" + info;
				txt.setText(info);
			}
		} catch (Exception e) {
			Log.e(TAG, "ERROR showDebugMessage()=" + message, e);
		}
	}


}