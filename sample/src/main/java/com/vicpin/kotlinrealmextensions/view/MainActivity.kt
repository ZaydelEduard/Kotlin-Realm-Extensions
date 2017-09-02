package com.vicpin.kotlinrealmextensions.view

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.vicpin.kotlinrealmextensions.R
import com.vicpin.kotlinrealmextensions.extensions.isMainThread
import com.vicpin.kotlinrealmextensions.extensions.wait
import com.vicpin.kotlinrealmextensions.model.Address
import com.vicpin.kotlinrealmextensions.model.Item
import com.vicpin.kotlinrealmextensions.model.User
import com.vicpin.krealmextensions.RealmConfigStore
import com.vicpin.krealmextensions.deleteAll
import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.saveAll
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val dbSize = 100
    val userSize = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //***********************************
        //See tests for complete usage
        //***********************************

        performTest("main thread") {
            Thread { performTest("background thread items") }.start()
        }

        // User perform Test
        performUserTest("main thread users") {
            Thread { performUserTest("background thread users") }.start()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        User().deleteAll()
        Item().deleteAll()
    }

    private fun performUserTest(threadName: String, finishCallback: (() -> Unit)? = null) {

        addMessage("Starting test on $threadName with User realm configuration")

        User().deleteAll()
        populateUserDb(userSize)

        addMessage("DB populated with $userSize users")

        addMessage("Querying users on $threadName...")

        addMessage("Result: ${User().queryAll().size} items ")

        addMessage("Deleting users on $threadName...")

        User().deleteAll()

        addMessage("Querying users on $threadName...")

        addMessage("Result: ${User().queryAll().size} items ")

        addMessage("Observing table changes...")

        wait(1) {
            populateUserDb(10)
        }

        wait(if (isMainThread()) 2 else 1) {
            populateUserDb(10)
        }

        wait(if (isMainThread()) 3 else 1) {
            populateUserDb(10)
        }

        wait(if (isMainThread()) 4 else 1) {
            addMessage("Subscription finished")
            var defaultRealm = Realm.getDefaultInstance();
            var userRealm = Realm.getInstance(RealmConfigStore.fetchConfiguration(User::class.java))
            var defaultCount = defaultRealm.where(User::class.java).count()
            var userCount = userRealm.where(User::class.java).count()

            addMessage("All users from default configuration : ${defaultCount}")
            addMessage("All users from configured : $userCount")
            finishCallback?.invoke()
        }

    }

    private fun performTest(threadName: String, finishCallback: (() -> Unit)? = null) {

        addMessage("Starting test on $threadName...", important = true)

        Item().deleteAll()
        populateDB(numItems = dbSize)

        addMessage("DB populated with $dbSize items")

        addMessage("Querying items on $threadName...")

        addMessage("Result: ${Item().queryAll().size} items ")

        addMessage("Deleting items on $threadName...")

        Item().deleteAll()

        addMessage("Querying items on $threadName...")

        addMessage("Result: ${Item().queryAll().size} items ")

        addMessage("Observing table changes...")

        wait(1) {
            populateDB(numItems = 10)
        }

        wait(if (isMainThread()) 2 else 1) {
            populateDB(numItems = 10)
        }

        wait(if (isMainThread()) 3 else 1) {
            populateDB(numItems = 10)
        }

        wait(if(isMainThread()) 4 else 1) {
            addMessage("Subscription finished")
            finishCallback?.invoke()
        }
    }

    private fun populateUserDb(numUsers: Int) {
        Array(numUsers) { User("name_%d".format(it), Address("street_%d".format(it))) }.toList().saveAll()
    }

    private fun populateDB(numItems: Int) {
        Array(numItems) { Item() }.toList().saveAll()
    }

    private fun addMessage(message: String, important: Boolean = false) {
        Handler(Looper.getMainLooper()).post {
            val view = TextView(this)
            if (important) view.typeface = Typeface.DEFAULT_BOLD
            view.text = message
            mainContainer.addView(view)
        }
    }

}
