import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints

class CompositeDateValidator(
    private val startDate: Long,
    private val endDate: Long
) : CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {
        return date in startDate..endDate
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(startDate)
        dest.writeLong(endDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CompositeDateValidator> {
        override fun createFromParcel(parcel: Parcel): CompositeDateValidator {
            val startDate = parcel.readLong()
            val endDate = parcel.readLong()
            return CompositeDateValidator(startDate, endDate)
        }

        override fun newArray(size: Int): Array<CompositeDateValidator?> {
            return arrayOfNulls(size)
        }
    }
}
