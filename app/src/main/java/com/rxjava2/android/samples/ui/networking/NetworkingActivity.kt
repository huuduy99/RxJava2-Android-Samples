package com.rxjava2.android.samples.ui.networking

import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rx2androidnetworking.Rx2AndroidNetworking
import com.rxjava2.android.samples.R
import com.rxjava2.android.samples.model.ApiUser
import com.rxjava2.android.samples.model.User
import com.rxjava2.android.samples.model.UserDetail
import com.rxjava2.android.samples.utils.Utils
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by amitshekhar on 04/02/17.
 */
class NetworkingActivity() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_networking)
    }

    /**
     * Map Operator Example
     */
    fun map(view: View?) {
        Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAnUser/{userId}").addPathParameter(
                "userId", "1").build().getObjectObservable(ApiUser::class.java).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()).map { apiUser -> // here we get ApiUser from server
            // then by converting, we are returning user
            User(apiUser)
        }.subscribe(object : Observer<User> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(user: User) {
                Log.d(TAG, "user : $user")
            }

            override fun onError(e: Throwable) {
                Utils.logError(TAG, e)
            }

            override fun onComplete() {
                Log.d(TAG, "onComplete")
            }
        })
    }
    /**
     * zip Operator Example
     */
    /**
     * This observable return the list of User who loves cricket
     */
    private val cricketFansObservable: Observable<List<User>>
        get() = Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllCricketFans").build().getObjectListObservable(
                User::class.java).subscribeOn(Schedulers.io())

    /*
     * This observable return the list of User who loves Football
     */
    private val footballFansObservable: Observable<List<User>>
        get() = Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllFootballFans").build().getObjectListObservable(
                User::class.java).subscribeOn(Schedulers.io())

    /*
     * This do the complete magic, make both network call
     * and then returns the list of user who loves both
     * Using zip operator to get both response at a time
     */
    private fun findUsersWhoLovesBoth() {
        // here we are using zip operator to combine both request
        Observable.zip(cricketFansObservable, footballFansObservable,
                       { cricketFans, footballFans -> filterUserWhoLovesBoth(cricketFans, footballFans) }).subscribeOn(
                Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<List<User>> {
            override fun onSubscribe(d: Disposable) {}
            override fun onNext(users: List<User>) {
                Log.d(TAG, "userList size : " + users.size)
                for (user: User in users) {
                    Log.d(TAG, "user : $user")
                }
            }

            override fun onError(e: Throwable) {
                Utils.logError(TAG, e)
            }

            override fun onComplete() {
                Log.d(TAG, "onComplete")
            }
        })
    }

    private fun filterUserWhoLovesBoth(cricketFans: List<User>, footballFans: List<User>): List<User> {
        val userWhoLovesBoth: MutableList<User> = ArrayList()
        for (footballFan: User in footballFans) {
            if (cricketFans.contains(footballFan)) {
                userWhoLovesBoth.add(footballFan)
            }
        }
        return userWhoLovesBoth
    }

    fun zip(view: View?) {
        findUsersWhoLovesBoth()
    }

    /**
     * flatMap and filter Operators Example
     */
    private val allMyFriendsObservable: Observable<List<User>>
        get() = Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllFriends/{userId}").addPathParameter(
                "userId", "1").build().getObjectListObservable(User::class.java)

    fun flatMapAndFilter(view: View?) {
        allMyFriendsObservable.flatMap { usersList ->

            // flatMap - to return users one by one
            Observable.fromIterable(usersList) // returning user one by one from usersList.
        }.filter { user -> // filtering user who follows me.
            user.isFollowing
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<User> {
            override fun onSubscribe(d: Disposable) {}
            override fun onNext(user: User) {
                // only the user who is following me comes here one by one
                Log.d(TAG, "user : $user")
            }

            override fun onError(e: Throwable) {
                Utils.logError(TAG, e)
            }

            override fun onComplete() {
                Log.d(TAG, "onComplete")
            }
        })
    }

    /**
     * take Operator Example
     */
    fun take(view: View?) {
        userListObservable.flatMap { usersList ->

            // flatMap - to return users one by one
            Observable.fromIterable(usersList) // returning user one by one from usersList.
        }.take(4) // it will only emit first 4 users out of all
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<User> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(user: User) {
                        // // only four user comes here one by one
                        Log.d(TAG, "user : $user")
                    }

                    override fun onError(e: Throwable) {
                        Utils.logError(TAG, e)
                    }

                    override fun onComplete() {
                        Log.d(TAG, "onComplete")
                    }
                })
    }

    /**
     * flatMap Operator Example
     */
    fun flatMap(view: View?) {
        userListObservable.flatMap { usersList ->

            // flatMap - to return users one by one
            Observable.fromIterable(usersList) // returning user one by one from usersList.
        }.flatMap(Function<User, ObservableSource<UserDetail>> { user -> // here we get the user one by one
            // and returns corresponding getUserDetailObservable
            // for that userId
            getUserDetailObservable(user.id)
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<UserDetail> {
            override fun onSubscribe(d: Disposable) {}
            override fun onError(e: Throwable) {
                Utils.logError(TAG, e)
            }

            override fun onNext(userDetail: UserDetail) {
                // do anything with userDetail
                Log.d(TAG, "userDetail : $userDetail")
            }

            override fun onComplete() {
                Log.d(TAG, "onComplete")
            }
        })
    }

    /**
     * flatMapWithZip Operator Example
     */
    private val userListObservable: Observable<List<User>>
        private get() = Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllUsers/{pageNumber}").addPathParameter(
                "pageNumber", "0").addQueryParameter("limit", "10").build().getObjectListObservable(User::class.java)

    private fun getUserDetailObservable(id: Long): Observable<UserDetail> {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAnUserDetail/{userId}").addPathParameter(
                "userId", id.toString()).build().getObjectObservable(UserDetail::class.java)
    }

    fun flatMapWithZip(view: View?) {
        userListObservable.flatMap { usersList ->

            // flatMap - to return users one by one
            Observable.fromIterable(usersList) // returning user one by one from usersList.
        }.flatMap { user ->
            // here we get the user one by one and then we are zipping
            // two observable - one getUserDetailObservable (network call to get userDetail)
            // and another Observable.just(user) - just to emit user
            Observable.zip(getUserDetailObservable(user.id), Observable.just(user), { userDetail, user ->
                // runs when network call completes
                // we get here userDetail for the corresponding user
                Pair(userDetail, user) // returning the pair(userDetail, user)
            })
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<Pair<UserDetail, User>> {
            override fun onComplete() {
                // do something onCompleted
                Log.d(TAG, "onComplete")
            }

            override fun onError(e: Throwable) {
                // handle error
                Utils.logError(TAG, e)
            }

            override fun onSubscribe(d: Disposable) {}
            override fun onNext(pair: Pair<UserDetail, User>) {
                // here we are getting the userDetail for the corresponding user one by one
                val userDetail = pair.first
                val user = pair.second
                Log.d(TAG, "user : $user")
                Log.d(TAG, "userDetail : $userDetail")
            }
        })
    }

    companion object {
        val TAG = NetworkingActivity::class.java.simpleName
    }
}