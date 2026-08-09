package plugin.test

import arc.util.Log
import arc.util.Ratekeeper

fun test() {
    val keep = Ratekeeper()

    for (i in 1..10) {
        if(keep.allow(1000, 5)) {
            Log.info("Allowed!")
            // doing something...
        } else {
            Log.info("Rejected!")
            Thread.sleep(1000)
        }
    }
}