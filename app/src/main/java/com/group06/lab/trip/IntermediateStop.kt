package com.group06.lab.trip

import android.os.Parcel
import android.os.Parcelable

class IntermediateStop(var place: String?, var lat: Double, var lon: Double, var date: Long) : Parcelable{

    var estimatedMinute: Int = -1
    var estimatedHour: Int = -1
    var estimatedDay: Int = -1

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readLong()
    )

    constructor() : this("", 0.0, 0.0,-1)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(place)
        parcel.writeDouble(lat)
        parcel.writeDouble(lon)
        parcel.writeLong(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IntermediateStop> {
        override fun createFromParcel(parcel: Parcel): IntermediateStop {
            return IntermediateStop(parcel)
        }

        override fun newArray(size: Int): Array<IntermediateStop?> {
            return arrayOfNulls(size)
        }
    }
}