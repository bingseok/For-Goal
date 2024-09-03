package com.example.cc_project

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Locale

class ChartAdapter(private var data: List<GoalData>) : RecyclerView.Adapter<ChartAdapter.ChartViewHolder>() {

    class ChartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pieChart: PieChart = view.findViewById(R.id.pieChart)
        val textViewGoalName: TextView = view.findViewById(R.id.textViewGoalName)
        val textViewPeriod: TextView = view.findViewById(R.id.textViewPeriod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chart_layout, parent, false)
        return ChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        val (goalName, completionRate, startDate, endDate) = data[position]
        Log.d("goalName1", "$goalName")
        Log.d("completionRate1", "$completionRate")

        holder.textViewGoalName.text = goalName


        // Mango 폰트 로드
        val mangoFont: Typeface? = ResourcesCompat.getFont(holder.itemView.context, R.font.mango)

        // PieChart에 Mango 폰트 적용
        holder.pieChart.setEntryLabelTypeface(mangoFont)
        holder.pieChart.setEntryLabelColor(R.color.black)

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val formattedStartDate = formatDate(startDate, dateFormat)
        val formattedEndDate = formatDate(endDate, dateFormat)

        val period = "$formattedStartDate ~ $formattedEndDate"
        holder.textViewPeriod.text = period

        // PieEntry 수정
        val entries = listOf(
            PieEntry(completionRate, "완료"),
            PieEntry(100 - completionRate, "미완료")
        )

        val lightSkyBlueColor = ContextCompat.getColor(holder.itemView.context, R.color.light_blue)
        val lightGrayColor = ContextCompat.getColor(holder.itemView.context, R.color.light_gray)

        val colors = mutableListOf<Int>()
        colors.add(lightSkyBlueColor)
        colors.add(lightGrayColor)

        val dataSet = PieDataSet(entries, "").apply { // Remove the dataset label by setting it to an empty string
            setColors(colors)// Set value text color to black
        }

        val pieData = PieData(dataSet).apply {
            setValueTextSize(12f)
        }

        holder.pieChart.data = pieData
        holder.pieChart.setUsePercentValues(true)
        holder.pieChart.description.isEnabled = false

        // 도넛 모양으로 설정
        holder.pieChart.isDrawHoleEnabled = true
        holder.pieChart.holeRadius = 50f
        holder.pieChart.transparentCircleRadius = 61f

        holder.pieChart.legend.isEnabled = false


        holder.pieChart.invalidate()
    }

    override fun getItemCount(): Int = data.size

    fun setData(newData: List<GoalData>) {
        data = newData
        notifyDataSetChanged()
    }

    private fun formatDate(date: String, dateFormat: SimpleDateFormat): String {
        val parsedDate = dateFormat.parse(date)
        return SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(parsedDate)
    }
}
