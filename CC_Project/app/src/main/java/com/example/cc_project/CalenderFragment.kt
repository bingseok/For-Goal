package com.example.cc_project

import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cc_project.databinding.FragmentCalenderBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import org.threeten.bp.DayOfWeek
import java.util.Calendar
import java.util.Locale

class CalenderFragment : Fragment() {
    private var selectedDate: CalendarDay? = null
    private lateinit var recyclerView: RecyclerView
    private var scheduleListAdapter: ScheduleListAdapter? = null
    private var selectedDateSchedules = mutableListOf<Map<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCalenderBinding.inflate(inflater, container, false)
        val calendarView = binding.calenderView
        val textView = binding.CalendarText
        val today = CalendarDay.today()

        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader)
        calendarView.setTitleFormatter(MonthArrayTitleFormatter(resources.getTextArray(R.array.custom_months)))
        calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.custom_weekdays)))

        val dayDecorator = DayDecorator(requireContext())
        val todayDecorator = TodayDecorator(requireContext())
        val sundayDecorator = SundayDecorator()
        val saturdayDecorator = SaturdayDecorator()
        var selectedMonthDecorator = SelectedMonthDecorator(today.month)

        val eventDecorator = fetchAndCreateEventDecorator(requireContext(), calendarView)

        calendarView.addDecorators(dayDecorator, todayDecorator, sundayDecorator, saturdayDecorator, selectedMonthDecorator, eventDecorator)

        val dateFormatter = SimpleDateFormat("M.d E", Locale.getDefault())

        textView.text = dateFormatter.format(Calendar.getInstance().apply {
            set(today.year, today.month - 1, today.day)
        }.time)

        calendarView.setOnMonthChangedListener { widget, date ->
            calendarView.removeDecorators()
            calendarView.invalidateDecorators()
            selectedMonthDecorator = SelectedMonthDecorator(date.month)
            calendarView.addDecorators(dayDecorator, todayDecorator, sundayDecorator, saturdayDecorator, selectedMonthDecorator, eventDecorator)
        }

        recyclerView = binding.eventListView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        selectedDate = today
        fetchSchedulesForDate(today)

        calendarView.setOnDateChangedListener { widget, date, selected ->
            if (selected) {
                selectedDate = date
                textView.text = dateFormatter.format(Calendar.getInstance().apply {
                    set(selectedDate?.year ?: today.year, (selectedDate?.month ?: today.month) - 1, selectedDate?.day ?: today.day)
                }.time)
                Log.d("checkk1", "$selectedDate")
                fetchSchedulesForDate(date)
            } else {
                selectedDate = today
                fetchSchedulesForDate(today)
            }
        }
        return binding.root
    }

    private fun fetchAndCreateEventDecorator(context: Context, calendarView: MaterialCalendarView): EventDecorator {
        val eventDecorator = EventDecorator(context)
        eventDecorator.setCalendarView(calendarView)
        eventDecorator.fetchEventDates()
        return eventDecorator
    }

    private fun fetchSchedulesForDate(date: CalendarDay) {
        selectedDateSchedules.clear()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val db = FirebaseFirestore.getInstance()
        val dateStr = sdf.format(Calendar.getInstance().apply {
            set(date.year, date.month - 1, date.day)
        }.time)

        val uid = Firebase.auth.currentUser?.uid ?: return
        Log.d("uid", "$uid")

        val goalCollectionRef = db.collection("uID").document(uid).collection("Goal")

        goalCollectionRef.get()
            .addOnSuccessListener { goalResult ->
                Log.d("CalenderFragment", "Goals fetched successful" + "ly: ${goalResult.size()}")
                for (goalDocument in goalResult) {
                    val goalId = goalDocument.id

                    val specGoalCollectionRef = goalCollectionRef.document(goalId).collection("specGoal")
                    specGoalCollectionRef
                        .whereLessThanOrEqualTo("startdate", dateStr)
                        .whereGreaterThanOrEqualTo("finishdate", dateStr)
                        .get()
                        .addOnSuccessListener { specGoalResult ->
                            Log.d("CalenderFragment", "SpecGoals fetched successfully: ${specGoalResult.size()}")
                            for (document in specGoalResult) {
                                val specGoalName = document.getString("specgoalname") ?: ""
                                val startDate = document.getString("startdate") ?: ""
                                Log.d("startDate1", "$startDate")
                                val finishDate = document.getString("finishdate") ?: ""
                                Log.d("finishDate2", "$finishDate")

                                val dayOfWeekArray = document.get("dayofweek") as? List<Boolean> ?: continue
                                val dayOfWeek = (Calendar.getInstance().apply {
                                    set(date.year, date.month - 1, date.day)
                                }.get(Calendar.DAY_OF_WEEK) + 5) % 7
                                Log.d("dayOfWeekArray", "$dayOfWeekArray")
                                Log.d("dayOfWeek", "$dayOfWeek")
                                if (!dayOfWeekArray[dayOfWeek]) {
                                    continue
                                }

                                selectedDateSchedules.add(
                                    mapOf(
                                        "specgoalname" to specGoalName,
                                        "startdate" to startDate,
                                        "finishdate" to finishDate
                                    )
                                )
                            }
                            updateListView()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("CalenderFragment", "Error getting specGoal documents: $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CalenderFragment", "Error getting goal documents: $exception")
            }
    }

    private fun updateListView() {
        scheduleListAdapter = ScheduleListAdapter(selectedDateSchedules)
        recyclerView.adapter = scheduleListAdapter
    }

    inner class DayDecorator(context: Context) : DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_selector)

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day != null && !day.equals(CalendarDay.today())
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setSelectionDrawable(drawable!!)
        }
    }

    inner class TodayDecorator(context: Context) : DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_circle_gray)
        private var currentDate: CalendarDay? = null

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            currentDate = CalendarDay.today()
            return day?.equals(currentDate) == true
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setBackgroundDrawable(drawable!!)
        }
    }

    inner class SelectedMonthDecorator(val selectedMonth: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day!!.month != selectedMonth
        }

        override fun decorate(view: DayViewFacade?) {
            context?.let {
                view!!.addSpan(ForegroundColorSpan(ContextCompat.getColor(it, R.color.gray)))
            }
        }
    }

    class SundayDecorator : DayViewDecorator {
        private val sunday = DayOfWeek.SUNDAY.value
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day!!.date.dayOfWeek.value == sunday
        }

        override fun decorate(view: DayViewFacade?) {
            view!!.addSpan(ForegroundColorSpan(Color.RED))
        }
    }

    class SaturdayDecorator : DayViewDecorator {
        private val saturday = DayOfWeek.SATURDAY.value
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day!!.date.dayOfWeek.value == saturday
        }

        override fun decorate(view: DayViewFacade?) {
            view!!.addSpan(ForegroundColorSpan(Color.BLUE))
        }
    }

    class EventDecorator(context: Context) : DayViewDecorator {
        private val eventDates = HashSet<CalendarDay>()
        private var calendarView: MaterialCalendarView? = null
        private val context: Context = context

        fun setCalendarView(calendarView: MaterialCalendarView) {
            this.calendarView = calendarView
        }

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return eventDates.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(DotSpan(10f, ContextCompat.getColor(context, R.color.main_color)))
        }

        fun fetchEventDates() {
            val db = FirebaseFirestore.getInstance()
            val uid = Firebase.auth.currentUser?.uid ?: return
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            Log.d("userid", "$uid")

            val goalCollectionRef = db.collection("uID").document(uid).collection("Goal")

            goalCollectionRef.get()
                .addOnSuccessListener { goalResult ->
                    Log.d("EventDecorator", "Goals fetched successfully: ${goalResult.size()}")
                    for (goalDocument in goalResult) {
                        val goalId = goalDocument.id

                        val specGoalCollectionRef = goalCollectionRef.document(goalId).collection("specGoal")

                        specGoalCollectionRef.get()
                            .addOnSuccessListener { specGoalResult ->
                                Log.d("EventDecorator", "SpecGoals fetched successfully: ${specGoalResult.size()}")
                                for (specGoalDocument in specGoalResult) {
                                    val finishDate = specGoalDocument.getString("finishdate")
                                    Log.d("finishDate", "$finishDate")
                                    val startDate = specGoalDocument.getString("startdate")
                                    Log.d("StartDate", "$startDate")

                                    val dayOfWeekArray = specGoalDocument.get("dayofweek") as? List<Boolean> ?: continue

                                    if (startDate != null && finishDate != null) {
                                        val startCalendarDay = stringToCalendarDay(startDate, sdf)
                                        val finishCalendarDay = stringToCalendarDay(finishDate, sdf)

                                        if (startCalendarDay != null && finishCalendarDay != null) {
                                            addDatesInRangeWithDayOfWeek(startCalendarDay, finishCalendarDay, dayOfWeekArray)
                                        }
                                    }
                                }
                                calendarView?.invalidateDecorators()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("EventDecorator", "Error getting specGoal documents: $exception")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("EventDecorator", "Error getting goal documents: $exception")
                }
        }

        private fun addDatesInRangeWithDayOfWeek(startDate: CalendarDay, finishDate: CalendarDay, dayOfWeekArray: List<Boolean>) {
            val startCalendar = Calendar.getInstance().apply {
                set(startDate.year, startDate.month - 1, startDate.day)
            }
            val endCalendar = Calendar.getInstance().apply {
                set(finishDate.year, finishDate.month - 1, finishDate.day)
            }

            while (!startCalendar.after(endCalendar)) {
                val currentDayOfWeek = (startCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
                if (dayOfWeekArray[currentDayOfWeek]) {
                    val day = CalendarDay.from(
                        startCalendar.get(Calendar.YEAR),
                        startCalendar.get(Calendar.MONTH) + 1,
                        startCalendar.get(Calendar.DAY_OF_MONTH)
                    )
                    eventDates.add(day)
                }
                startCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        private fun stringToCalendarDay(dateString: String, sdf: SimpleDateFormat): CalendarDay? {
            return try {
                val date = sdf.parse(dateString)
                val calendar = Calendar.getInstance()
                date?.let {
                    calendar.time = it
                    CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
