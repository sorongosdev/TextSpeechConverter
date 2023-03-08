package com.sorongos.textspeechconverter

import android.R
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.sorongos.textspeechconverter.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var recognitionListener: RecognitionListener
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermission()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") //음성인식 언어 설정

        setSTT()
        binding.playButton.setOnClickListener {
            startSTT()
        }
        binding.stopButton.setOnClickListener {
            stopRecord()
        }

        setTTS()
        binding.ttsButton.setOnClickListener {
            startTTS(binding.TtsEditText.text.toString())
        }
    }

    private fun setTTS() {
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "해당언어는 지원되지 않습니다.")
                    return@OnInitListener
                }
            }
        })

    }

    private fun startTTS(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        tts?.playSilentUtterance(750, TextToSpeech.QUEUE_ADD, null)
    }

    private fun stopRecord() {
        speechRecognizer!!.stopListening() //녹음 중지
        Toast.makeText(this, "음성 기록 중지", Toast.LENGTH_SHORT).show()
    }

    private fun startSTT() {
        Toast.makeText(this, "음성 기록 시작", Toast.LENGTH_SHORT).show()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(recognitionListener)
        speechRecognizer?.startListening(intent)
    }

    /**권한 팝업*/
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECORD_AUDIO), 0
        )
    }

    /**RecognitionListener 초기화*/
    private fun setSTT() {
        recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) =
                Toast.makeText(this@MainActivity, "음성인식 시작", Toast.LENGTH_SHORT).show()

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                var message: String

                when (error) {
                    SpeechRecognizer.ERROR_AUDIO ->
                        message = "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT ->
                        message = "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        message = "퍼미션 없음"
                    SpeechRecognizer.ERROR_NETWORK ->
                        message = "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        message = "네트워크 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        message = "찾을 수 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        message = "RECOGNIZER가 바쁨"
                    SpeechRecognizer.ERROR_SERVER ->
                        message = "서버가 이상함"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        message = "말하는 시간초과"
                    else ->
                        message = "알 수 없는 오류"
                }
                Toast.makeText(applicationContext, "에러 발생 $message", Toast.LENGTH_SHORT).show()
            }

            /**UI 업데이트*/
            override fun onResults(results: Bundle) {
                binding.SttEditText.apply {
                    setText(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0])
                }
            }
        }
    }
}