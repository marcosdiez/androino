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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

public class ArduinoService implements Runnable {

	private static final int EMULATOR_SAMPLE_FREQ = 8000; // emulator sample freq 8000Hz
	private static final int GT540_SAMPLE_FREQ = 44100; // LG GT540 
	private static final int AUDIO_SAMPLE_FREQ = GT540_SAMPLE_FREQ;
	public static int ACQ_AUDIO_BUFFER_SIZE = 16000; 	

	private static final String TAG = "ArduinoService";
	
	private Handler mClientHandler;
	private FSKDecoder mDecoder;
	private static boolean forceStop = false;
	public static final int HANDLER_MESSAGE_FROM_ARDUINO = 2000;
	public static final int RECORDING_ERROR = -1333;

	public ArduinoService(Handler handler) {
		this.mClientHandler = handler;
	}

	@Override
	public void run() {
		this.forceStop = false;
		// Decoder initialization
		this.mDecoder = new FSKDecoder(this.mClientHandler);
		this.mDecoder.start();
		
		// Sound recording loop
		this.audioRecordingRun();
		
		// DEVELOPMENTDUMMYARDUINO
		//dummyRecordingRun();
	}
	
	public synchronized void write(int message) {
		encodeMessage(message);
	}
	public void stopAndClean() {
		Log.i(TAG, "STOP stopAndClean():");
		this.forceStop = true;
	}	 

	private void dummyRecordingRun(){
		while (!forceStop) {
			double num = Math.random();
			if (num>0.8) {
				int n = 20 + (int) (10.0 * Math.random());
				//this.mClientHandler.obtainMessage(HANDLER_MESSAGE_FROM_ARDUINO,n,0).sendToTarget();
			}
			Log.i(TAG, "dummyRecordingRun()");
			try {Thread.sleep(2*1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	
	private void audioRecordingRun(){
		int AUDIO_BUFFER_SIZE = ACQ_AUDIO_BUFFER_SIZE; //44000;//200000;// 16000;
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2, AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize) AUDIO_BUFFER_SIZE = minBufferSize;

		Log.i(TAG, "buffer size:" + AUDIO_BUFFER_SIZE);
		byte[] audioData = new byte[AUDIO_BUFFER_SIZE];

		AudioRecord aR = new AudioRecord(MediaRecorder.AudioSource.MIC,
				AUDIO_SAMPLE_FREQ, 2, AudioFormat.ENCODING_PCM_16BIT,
				AUDIO_BUFFER_SIZE);

		// audio recording
		aR.startRecording();
		int nBytes = 0;
		int index = 0;
		this.forceStop = false;
		// continuous loop
		while (true) {
			nBytes = aR.read(audioData, index, AUDIO_BUFFER_SIZE);
			Log.d(TAG, "audio acq: length=" + nBytes);
			// Log.v(TAG, "nBytes=" + nBytes);
			if (nBytes < 0) {
				Log.e(TAG, "audioRecordingRun() read error=" + nBytes);
				this.mClientHandler.obtainMessage(ArduinoService.HANDLER_MESSAGE_FROM_ARDUINO, RECORDING_ERROR, 0).sendToTarget();
			}
			
			this.mDecoder.addSound(audioData, nBytes);

			if (this.forceStop) {
				this.mDecoder.stopAndClean();
				break;
			}
		}
		
		aR.stop();
		aR.release();
		Log.i(TAG, "STOP audio recording stoped");
		
	}

	private void encodeMessage(int value) {
		// audio initialization
		int AUDIO_BUFFER_SIZE = 16000;
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2, AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize) AUDIO_BUFFER_SIZE = minBufferSize;
		AudioTrack aT = new AudioTrack(AudioManager.STREAM_MUSIC,
				AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE,
				AudioTrack.MODE_STREAM);
		aT.play();
		
		//error detection encoding 
		Log.i(TAG, "encodeMessage() value=" + value);
		value = ErrorDetection.createMessage(value);
		Log.i(TAG, "encodeMessage() message=" + value);
		// sound encoding
		double[] sound = FSKModule.encode(value);

		ByteBuffer buf = ByteBuffer.allocate(4 * sound.length);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < sound.length; i++) {
			int yInt = (int) sound[i];
			buf.putInt(yInt);
		}
		byte[] tone = buf.array();
		// play message
		int nBytes = aT.write(tone, 0, tone.length);
		aT.stop();
		aT.release();
	}

	
}
