package com.rxjava2.android.samples.ui.pagination

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rxjava2.android.samples.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by amitshekhar on 15/03/17.
 */
class PaginationActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()
    private val paginator = PublishProcessor.create<Int>()
    private lateinit var paginationAdapter: PaginationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var loading = false
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem = 0
    private var totalItemCount = 0
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagination)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = layoutManager
        paginationAdapter = PaginationAdapter()
        recyclerView.adapter = paginationAdapter
        setUpLoadMoreListener()
        subscribeForData()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    /**
     * setting listener to get callback for load more
     */
    private fun setUpLoadMoreListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = layoutManager.itemCount
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                    pageNumber++
                    paginator.onNext(pageNumber)
                    loading = true
                }
            }
        })
    }

    /**
     * subscribing for data
     */
    private fun subscribeForData() {
        val disposable = paginator.onBackpressureDrop().doOnNext { _: Int? ->
            loading = true
            progressBar.visibility = View.VISIBLE
        }.concatMapSingle { page: Int ->
            dataFromNetwork(page).subscribeOn(Schedulers.io()).doOnError { _: Throwable? -> } // continue emission in case of error also
                    .onErrorReturn { ArrayList() }
        }.observeOn(AndroidSchedulers.mainThread()).subscribe { items: List<String> ->
            paginationAdapter.addItems(items)
            paginationAdapter.notifyDataSetChanged()
            loading = false
            progressBar.visibility = View.INVISIBLE
        }
        compositeDisposable.add(disposable)
        paginator.onNext(pageNumber)
    }

    /**
     * Simulation of network data
     */
    private fun dataFromNetwork(page: Int): Single<List<String>> {
        return Single.just(true).delay(2, TimeUnit.SECONDS).map {
            val items: MutableList<String> = ArrayList()
            for (i in 1..10) {
                items.add("Item " + (page * 10 + i))
            }
            items
        }
    }

    companion object {
        val TAG: String = PaginationActivity::class.java.simpleName
    }
}