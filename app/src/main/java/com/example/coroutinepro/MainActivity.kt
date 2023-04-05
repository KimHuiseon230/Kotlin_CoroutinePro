package com.example.coroutinepro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.example.coroutinepro.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var handler: Handler
    lateinit var channel: Channel<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        //1.핸들러를 등록
        handler = object : Handler() {
            // 나중에 스레드에서 데이터 값을 보낼것이라는 것을 알고 있음.
            // 메세지 -> 번들의 값.
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                // 프로토콜로 약속된 값ㅇ
//                var value =msg.obj as String
                binding.tvSumResult.text = "sum${msg.obj}"
            }
        }

        //2.메세지 역할 (코루틴 방식 -> 채널)
        channel = Channel<Long>()
        //2-2 핸들러 역할
        val mainScope = GlobalScope.launch(Dispatchers.Main) {
            channel.consumeEach {
                // 채널의 값이 it 으로 들어옴
                binding.tvSumResult.text = "sum${it}"
            }
        }
        //3. 스레드를 설계한다
        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())

        //+++ 클릭 이벤트
        //오랫 동안 시간이 걸리는 작업을(약 6 ~ 8초) 이벤트 요청
        binding.btnClick.setOnClickListener {
            backgroundScope.launch {
                var sum = 0L
                var time = measureTimeMillis {
                    for (i in 1..2_000_000_000) {
                        sum += i
                    }
                }
                Log.e("MainActivity", "${time}")
//                binding.tvSumResult.text = "sum${sum}"
                /*  val message: Message = Message()
                    //sum을 정수형 타입으로 주겠다는 의미
                    message.obj = "${sum}"
                    handler.handleMessage(message)*/
                channel.send(sum.toLong())

            }  //+++ 클릭 이벤트

            binding.btnClear.setOnClickListener {
                binding.tvSumResult.text = "합계 출력 : 0"
            }


        }
    }
}
/*
 * 이벤트 처리 클릭후 텍뷰에서 값을 입력하면 ANR이 발생하면서 앱이 강제적으로 꺼지는 현상이 발생된다.
 * 그러므로 핸들러에 명시한 것을 꼭 해줘야한다.
 * ANR을 해결하면서 다른 처리도 가능하다.
 * 스레드 부분을   backgroundScope.launch로 바꿔서 사용하면 됨 */