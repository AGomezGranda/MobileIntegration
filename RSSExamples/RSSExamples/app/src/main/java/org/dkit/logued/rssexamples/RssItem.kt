package org.dkit.logued.rssexamples

// DL - Code from RSS Tutorial has been extended here.

// add this, IDE does not auto-suggest it
import android.os.Parcel
import android.os.Parcelable

data class RssItem(
    var title: String? = "",      // default values for no-arg construction
    var description: String? = "",
    var pubDate: String? = "",
    var link: String? = ""
) : Parcelable {                // implement Parcelable to allow objects to be
                                // passed between Android components (like serialization)
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(pubDate)
        parcel.writeString(link)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RssItem> {
        override fun createFromParcel(parcel: Parcel): RssItem {
            return RssItem(parcel)
        }
        override fun newArray(size: Int): Array<RssItem?> {
            return arrayOfNulls(size)
        }
    }
}
