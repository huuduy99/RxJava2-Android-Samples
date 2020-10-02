package com.rxjava2.android.samples.ui.cache

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.rxjava2.android.samples.R
import com.rxjava2.android.samples.ui.cache.model.Data
import com.rxjava2.android.samples.ui.cache.source.DataSource
import com.rxjava2.android.samples.ui.cache.source.DiskDataSource
import com.rxjava2.android.samples.ui.cache.source.MemoryDataSource
import com.rxjava2.android.samples.ui.cache.source.NetworkDataSource
import com.rxjava2.android.samples.utils.AppConstant
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CacheExampleActivity : AppCompatActivity() {
    lateinit var btn: Button
    lateinit var textView: TextView
    private lateinit var dataSource: DataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        btn = findViewById<View>(R.id.btn) as Button
        textView = findViewById<View>(R.id.textView) as TextView
        btn.setOnClickListener { doSomeWork() }
        dataSource = DataSource(MemoryDataSource(), DiskDataSource(), NetworkDataSource())
    }

    private fun doSomeWork() {
        val memory = dataSource.dataFromMemory
        val disk = dataSource.dataFromDisk
        val network = dataSource.dataFromNetwork
        Observable.concat(memory, disk, network).firstElement().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()).toObservable().subscribe(observer)
    }

    private val observer: Observer<Data>
        get() = object : Observer<Data> {
            override fun onSubscribe(d: Disposable) {
                Log.d(TAG, " onSubscribe : " + d.isDisposed)
            }

            override fun onNext(data: Data) {
                textView.append(" onNext : " + data.source)
                textView.append(AppConstant.LINE_SEPARATOR)
                Log.d(TAG, " onNext : " + data.source)
            }

            override fun onError(e: Throwable) {
                textView.append(" onError : " + e.message)
                textView.append(AppConstant.LINE_SEPARATOR)
                Log.d(TAG, " onError : " + e.message)
            }

            override fun onComplete() {
                textView.append(" onComplete")
                textView.append(AppConstant.LINE_SEPARATOR)
                Log.d(TAG, " onComplete")
            }
        }

    companion object {
        private val TAG = CacheExampleActivity::class.java.simpleName
    }
}