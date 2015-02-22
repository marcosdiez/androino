package org.androino.prototype;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

public class AudioArduinoService extends ArduinoService {

	private static final int GT540_SAMPLE_FREQ = 44100;
	private static final int DEVEL_SAMPLE_FREQ = 8000;
	private static final int AUDIO_SAMPLE_FREQ = GT540_SAMPLE_FREQ ;
	public static int ACQ_AUDIO_BUFFER_SIZE = 16000; 	

	public static int SOFT_MODEM_HIGH_FREQ = 3150;
	public static int SOFT_MODEM_LOW_FREQ = 1575;

	private static final String TAG = "AudioArduinoService";

	private byte[] testAudioArray;
	private int testAudioDuration = 2; //seconds
	private int testFrequency = 400;
	private int globalCounter = 0;
	private FSKDecoder mDecoder;
	
	public AudioArduinoService(Handler handler) {
		super(handler);
	}
	
	
	public void run() {
		this.forceStop = false;

		//testDecodeAmplitude();
		testDecode();
		
		// continuous loop
		while (true) {
			// stop if requested
			if (this.forceStop) {
				try {
					Thread.sleep(500*1);
				} catch (InterruptedException e) {
				}
				break;
			}
		}
	}

	private void analyzeSound(byte[] audioData, int nBytes) {
		// decode sound
		sendMessage(audioData.length, nBytes);
	}

	public void write(String message) {
		Log.i(TAG, "write message=" + message);
		// development purposes
		if (message.equals("REC")){
			testRecordAudio();
			return;
		} else if (message.equals("PLAY")){
			testPlayAudio(-1);
			return;
		} else if (message.equals("FILE")){
			testPlayAudio(-1002);
			return;
		} else if (message.equals("TONE")){
			testPlayAudio(1213);
			return;
		}
		int value = 0;
		try {
			value = Integer.parseInt(message);
		} catch (NumberFormatException e) {
			Log.e(TAG, "write() error parsing :" + message + ": to integer" );
			e.printStackTrace();
		}
		
		//char c = message.charAt(0);
		//int value = (int)c;
		//byte b = new Integer(value).byteValue();
		//Log.i(TAG, "write message c=" +  c + " int=" + value + " byte=" + b + " binary=" + Integer.toBinaryString(b));
		this.encodeMessage(value);
		
		//this.testEncode();
		//testRecordAudio();
		
		// testFrequency = testFrequency + 300;
		// if (testFrequency>3000) testFrequency = 400;
	}

	
	public void stopAndClean() {
		Log.i(TAG,"stopAndClean");
		this.mDecoder.stopAndClean();
		super.stopAndClean();
		
		//this.debugByteConversion();
	}
	
	private void debugByteConversion(){

		// memory byte representation
		byte[] bytes = this.testAudioArray;
		for (int i = 0; i < 10; i++) {
			byte b = bytes[i];
			int in = (int)b;
			Log.i(TAG, "MEMORY byte i=" + i + ":" + in + ":" + Integer.toBinaryString(b)); 
		}
	
		bytes = readAudioFromFile();
		for (int i = 0; i < 10; i++) {
			byte b = bytes[i];
			int in = (int)b;
			Log.i(TAG, "FILE byte i=" + i + ":" + in + ":" + Integer.toBinaryString(b)); 
		}

		
	}

	
	private byte[] generateTone(int frequency, long millis) {
		//int duration = millis/1000; // s
		int samplingRate = AUDIO_SAMPLE_FREQ; // Hz
		int numberOfSamples = (int)(millis * samplingRate/1000);
		numberOfSamples = numberOfSamples/2;
		double samplingTime = 1.0 / samplingRate;
		Log.i(TAG, "generateTone:samplingTime=" + samplingTime);
		ByteBuffer buf = ByteBuffer.allocate(4 * numberOfSamples);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		double amplitude = 30000.0; // max amplitude 32768
		double y = 0;
		for (int i = 0; i < numberOfSamples; i++) {
			y = amplitude
					* Math.sin(2 * Math.PI * frequency * i * samplingTime);
			try {
				// buf.putDouble(y);
				int yInt = (int) y;
				buf.putInt(yInt);
			} catch (Exception e) {
				Log.e(TAG, "generateTone:error i=" + i);
				e.printStackTrace();
				break;
			}
		}
		return buf.array();

	}
	private void encodeMessage(int value) {
		int AUDIO_BUFFER_SIZE = 16000;
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2,
				AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize)
			AUDIO_BUFFER_SIZE = minBufferSize;
		AudioTrack aT = new AudioTrack(AudioManager.STREAM_MUSIC,
				AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE,
				AudioTrack.MODE_STREAM);
		aT.play();
		//byte[] tone = generateTone(1000);
		
		int[] bits = this.getBits(value);
		//int[] bits = {1,1,1,1,1,2,1,2};
		double[] sound = FSKModule.encode(bits);
		Log.i(TAG, "encodeMessage() message=" + value);
		ByteBuffer buf = ByteBuffer.allocate(4 * sound.length);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < sound.length; i++) {
			int yInt = (int) sound[i];
			buf.putInt(yInt);
		}
		byte[] tone = buf.array();
		
		int nBytes = aT.write(tone, 0, tone.length);
		aT.stop();
		aT.release();
	}

	private int[] getBits(int value){
		// if error
		int[] bits = new int[8];
		try {
			String binary = Integer.toBinaryString(value);
			int len = binary.length();
			if (len<8) {// adding zeros
				int nZeros = 8-len;
				for (int i = 0; i < nZeros; i++) {
					binary = "0" + binary;
				}
			}
			Log.i(TAG, "getBits() int=" + value+ " binary=" + binary);
			// change bit ordering arduino receives bits in a different order 
			char[] bitsC = binary.toCharArray();
			for (int i = 0; i < 8; i++) {
				char c = bitsC[i];
				if (c == '1') bits[7-i] = 2;
				else bits[7-i] = 1;
			}
			
			//for (int i = 0; i < bits.length; i++) {
			//	Log.i(TAG, "bit=" + bits[i]);
			//}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "getBits() ERROR:" + e.getMessage(),e);
		}
		return bits;
		
	}
	
	private void testEncode() {
		int AUDIO_BUFFER_SIZE = 16000;
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2,
				AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize)
			AUDIO_BUFFER_SIZE = minBufferSize;
		AudioTrack aT = new AudioTrack(AudioManager.STREAM_MUSIC,
				AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE,
				AudioTrack.MODE_STREAM);
		aT.play();
		//byte[] tone = generateTone(1000);
		int[] bits = {2,1,2,1,2,1,2,1};
		double[] sound = FSKModule.encode(bits);
		Log.i(TAG, "testEncode() sound lenght=" + sound.length);
		ByteBuffer buf = ByteBuffer.allocate(4 * sound.length);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < sound.length; i++) {
			int yInt = (int) sound[i];
			buf.putInt(yInt);
		}
		byte[] tone = buf.array();
		
		int nBytes = aT.write(tone, 0, tone.length);
		aT.stop();
		aT.release();
	}

	
	private void testPlayAudio(int frequency) {
		int AUDIO_BUFFER_SIZE = testAudioDuration*2*AUDIO_SAMPLE_FREQ; // 2 seconds / 2bytes=16bit
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2,
				AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize)
			AUDIO_BUFFER_SIZE = minBufferSize;

		byte[] audio;
		if (frequency >0) {
			Log.i(TAG, "testPlayAudio frequency=" + frequency);
			audio = generateTone(frequency,testAudioDuration*1000);
			}
		else if (frequency==-1002){
			// something wrong: only plays the first second
			audio = readAudioFromFile();
		} else 
			audio = this.testAudioArray;
		Log.i(TAG, "testPlayAudio length=" + audio.length);

		AudioTrack aT = new AudioTrack(AudioManager.STREAM_MUSIC,
				AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE,
				AudioTrack.MODE_STREAM);
		aT.play();
		int nBytes = aT.write(audio, 0,audio.length);
		//int nBytes = aT.write(this.testAudioArray, 0,this.testAudioArray.length);
		Log.i(TAG, "testPlayAudio written bytes=" + nBytes);
		// byte[] tone = generateTone(this.testFrequency);
		// int nBytes = aT.write(tone, 0, tone.length);
		aT.stop();
		aT.release();
	}

	
	private void testRecordAudio() {
		int AUDIO_BUFFER_SIZE = 2*AUDIO_SAMPLE_FREQ; // 2 bytes per sample (PCM 16bit)
		int recordingBufferSize = testAudioDuration *AUDIO_BUFFER_SIZE; // seconds recording
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2,
				AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize)
			AUDIO_BUFFER_SIZE = minBufferSize;

		Log.i(TAG, "buffer size:" + recordingBufferSize);
		byte[] audioData = new byte[recordingBufferSize];

		// appendDebugInfo("BufferSize", "" + AUDIO_BUFFER_SIZE);
		AudioRecord aR = new AudioRecord(MediaRecorder.AudioSource.MIC,
				AUDIO_SAMPLE_FREQ, 2, AudioFormat.ENCODING_PCM_16BIT,
				AUDIO_BUFFER_SIZE);

		// audio recording
		aR.startRecording();
		int nBytes = 0;
		int index = 0;
		int freeBuffer = recordingBufferSize;
		do {
			nBytes = aR.read(audioData, index, AUDIO_BUFFER_SIZE);
			if (nBytes < 0) {
				Log.e(TAG, "read error=" + nBytes);
				break; // error happened
			}
			freeBuffer = freeBuffer - nBytes;
			index = index + nBytes;
			Log.i(TAG, "read #bytes:" + nBytes);
		} while (freeBuffer >= AUDIO_BUFFER_SIZE);

		testAudioArray = audioData;
		String message = "Sampling=" + AUDIO_SAMPLE_FREQ + ":" + "buffer size="
				+ AUDIO_BUFFER_SIZE + "\n";
		
		// saving data to file
		saveAudioToFile(message, audioData);

		// finalization
		aR.stop();
		aR.release();
		Log.i(TAG, "audio recording stoped");
	}
	
	private byte[] readAudioFromFile(){
		String filePath = Utility.getFilePath();
		double[] data = FSKModule.readInfoFromFile(filePath, testAudioDuration* AUDIO_SAMPLE_FREQ );
		Log.i(TAG, "readAudioFromFile:" + filePath + " size=" + data.length);
		ByteBuffer buf = ByteBuffer.allocate(2 * data.length); //16bits
		buf.order(ByteOrder.LITTLE_ENDIAN);

		byte[] sound = new byte[0];
		try {
			for (int i = 0; i < data.length; i++) {
				short value = (short) data[i];
				buf.putShort(value);
			}
		} catch (Exception e) {
			Log.e(TAG, "readAudioFromFile Error " + e.getMessage(), e);
			e.printStackTrace();
		}
		sound = buf.array();
		Log.i(TAG, "readAudioFromFile() nbytes=" + sound.length);
		return sound;
	}

	private void saveAudioToFile(String header, byte[] audioData) {
		ByteBuffer buf = ByteBuffer.wrap(audioData, 0, audioData.length);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		StringBuffer strB = new StringBuffer(header);
		int counter = 0;
		while (buf.remaining() >= 2) {
			counter++;
			short s = buf.getShort();
			double mono = (double) s;
			strB.append(mono);
			strB.append("\n");
		}
		Utility.writeToFile(strB.toString());
	}

	private void showBytebuffer(byte[] audioData) {
		ByteBuffer buf = ByteBuffer.wrap(audioData, 0, audioData.length);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		int counter = 0;
		while (buf.remaining() >= 2) {
			counter++;
			short s = buf.getShort();
			double mono = (double) s;
			// double mono_norm = mono / 32768.0;
			Log.v(TAG, "" + mono);
			// msg += "Bytebuffer: " + mono + ":\n";
			// if (counter > 10) break;
		}
	}

	private void testDecodeAmplitude() {
		int AUDIO_BUFFER_SIZE = 1600;// 16000;
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2,
				AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize)
			AUDIO_BUFFER_SIZE = minBufferSize;

		Log.i(TAG, "buffer size:" + AUDIO_BUFFER_SIZE);
		byte[] audioData = new byte[AUDIO_BUFFER_SIZE];

		// appendDebugInfo("BufferSize", "" + AUDIO_BUFFER_SIZE);
		AudioRecord aR = new AudioRecord(MediaRecorder.AudioSource.MIC,
				AUDIO_SAMPLE_FREQ, 2, AudioFormat.ENCODING_PCM_16BIT,
				AUDIO_BUFFER_SIZE);

		// audio recording
		aR.startRecording();
		int nBytes = 0;
		int index = 0;
		int counter = 0;
		this.forceStop = false;
		// continuous loop
		String message = "";
		while (true) {
			counter++;
			nBytes = aR.read(audioData, index, AUDIO_BUFFER_SIZE);
			// Log.v(TAG, "nBytes=" + nBytes);
			if (nBytes < 0) {
				Log.e(TAG, "read error=" + nBytes);
				break; // error happened
			}
			int volume = (int)decodeAmplitude(audioData, nBytes);
			Log.i(TAG, "volume="+volume+"%");
			
			//double frequency = decodeFrequency(audioData, nBytes);
			//Log.i(TAG, "freq=" + frequency + "Hz");

			if (this.forceStop) {
				break;
			}
		}
		aR.stop();
		aR.release();
		Log.i(TAG, "audio recording stoped");
	}

	private double decodeAmplitude(byte[] audioData, int nBytes) {
		double volume = 0;
		ByteBuffer buf = ByteBuffer.wrap(audioData, 0, nBytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		double NORM_FACTOR = 32768.0;

		int counter = 0;
		double amplitude = 0;
		while (buf.remaining() >= 2) {
			counter++;
			short s = buf.getShort();
			double value = (double) s;
			value = value / NORM_FACTOR;
			if (value < 0)
				value = value * (-1.0);
			amplitude += value;
			// if (counter > 10) break;
		}
		volume = 100 * amplitude / counter;
		Log.v(TAG, "decodeAmplitude():volume=" + volume);
		sendMessage((int)volume, 0);
//		this.globalCounter++;
//		if (globalCounter>50){
//			sendMessage((int)volume, 0);
//			globalCounter = 0;
//		}
		
		return volume;
	}

	private double decodeFrequency(byte[] audioData, int nBytes) {
		double frequency = 0;
		ByteBuffer buf = ByteBuffer.wrap(audioData, 0, nBytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		int counter = 0;
		int signChangeCounter = 0;
		int sign = 1;
		while (buf.remaining() >= 2) {
			counter++;
			short s = buf.getShort();
			double value = (double) s;
			if (value > 0) {
				if (sign < 0) {
					signChangeCounter++;
					sign = 1;
				}
			} else { // value <0
				if (sign > 0) {
					signChangeCounter++;
					sign = -1;
				}
			}
		}
		int nCycles = signChangeCounter / 2;
		double time = (nBytes / 2.0) / AUDIO_SAMPLE_FREQ;

		frequency = nCycles / time;
		Log.v(TAG, "decodeFrequency():freq=" + frequency);
		// sendMessage(volume, 0);
		return frequency;
	}

	private void testDecode() {
		this.mDecoder = new FSKDecoder(this.mClientHandler);
		this.mDecoder.start();
		
		int AUDIO_BUFFER_SIZE = ACQ_AUDIO_BUFFER_SIZE; //44000;//200000;// 16000;
		int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2,
				AudioFormat.ENCODING_PCM_16BIT);
		if (AUDIO_BUFFER_SIZE < minBufferSize)
			AUDIO_BUFFER_SIZE = minBufferSize;

		Log.i(TAG, "buffer size:" + AUDIO_BUFFER_SIZE);
		byte[] audioData = new byte[AUDIO_BUFFER_SIZE];

		// appendDebugInfo("BufferSize", "" + AUDIO_BUFFER_SIZE);
		AudioRecord aR = new AudioRecord(MediaRecorder.AudioSource.MIC,
				AUDIO_SAMPLE_FREQ, 2, AudioFormat.ENCODING_PCM_16BIT,
				AUDIO_BUFFER_SIZE);

		// audio recording
		aR.startRecording();
		int nBytes = 0;
		int index = 0;
		int counter = 0;
		this.forceStop = false;
		// continuous loop
		String message = "";
		while (true) {
			counter++;
			nBytes = aR.read(audioData, index, AUDIO_BUFFER_SIZE);
			Log.d(TAG, "testDecode():audio acq: length=" + nBytes);
			// Log.v(TAG, "nBytes=" + nBytes);
			if (nBytes < 0) {
				Log.e(TAG, "read error=" + nBytes);
				break; // error happened
			}
			this.mDecoder.addSound(audioData, nBytes);
			
			//double frequency = decodeFrequency(audioData, nBytes);
			//Log.i(TAG, "freq=" + frequency + "Hz");

			if (this.forceStop) {
				break;
			}
		}
		aR.stop();
		aR.release();
		Log.i(TAG, "audio recording stoped");
	}
	
}
