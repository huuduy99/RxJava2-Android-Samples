package com.rxjava2.android.samples.ui.operators

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rxjava2.android.samples.R
import com.rxjava2.android.samples.ui.operators.model.Address
import com.rxjava2.android.samples.ui.operators.model.User
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.Thread.currentThread
import java.util.*

class FlatMapActivity : AppCompatActivity() {
    private var disposable: Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flat_map)


        usersObservable.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).flatMap {
            getAddressObservable(it)
        }.subscribe(object : Observer<User> {
            override fun onSubscribe(d: Disposable) {
                Log.e(TAG, "onSubscribe")

                disposable = d
            }

            override fun onNext(user: User) {
                Log.e(TAG, "onNext: " + user.name + ", " + user.gender + ", " + user.address.address)
            }

            override fun onComplete() {
                Log.e(TAG, "All users emitted!")
            }

            override fun onError(e: Throwable) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable!!.dispose()
    }

    /**
     * Assume this as a network call
     * returns Users with address filed added
     */
    private fun getAddressObservable(user: User): Observable<User> {
        val addresses = arrayOf("1600 Amphitheatre Parkway, Mountain View, CA 94043",
                                "2300 Traverwood Dr. Ann Arbor, MI 48105", "500 W 2nd St Suite 2900 Austin, TX 78701",
                                "355 Main Street Cambridge, MA 02142")
        return Observable.create { emitter: ObservableEmitter<User> ->
            println("Processing item on2: " + currentThread().name)
            val address = Address()
            address.address = addresses[Random().nextInt(2) + 0]
            if (!emitter.isDisposed) {
                user.address = address
                // Generate network latency of random duration
                val sleepTime = Random().nextInt(1000) + 500
                Thread.sleep(sleepTime.toLong())
                emitter.onNext(user)
                emitter.onComplete()
            }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Assume this is a network call to fetch users
     * returns Users with name and gender but missing address
     */
    private val usersObservable: Observable<User>
        private get() {
            val maleUsers = arrayOf("Mark", "John", "Trump", "Obama")
            val users: MutableList<User> = ArrayList()
            for (name in maleUsers) {
                val user = User()
                user.name = name
                user.gender = "male"
                users.add(user)
            }
            return Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<User> ->
                for (user in users) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(user)
                    }
                }
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            } as ObservableOnSubscribe<User>).subscribeOn(Schedulers.io())
        }

    companion object {
        private val TAG = FlatMapActivity::class.java.simpleName
    }
}