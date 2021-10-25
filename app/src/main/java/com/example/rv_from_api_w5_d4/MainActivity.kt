package com.example.rv_from_api_w5_d4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rv_from_api_w5_d4.Constants.url
import com.example.rv_from_api_w5_d4.Constants.urlTag
import com.example.rv_from_api_w5_d4.databinding.ActivityMainBinding
import com.example.rv_from_api_w5_d4.databinding.RecyclerLayoutBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeBinding()
        initializeRecycler()
        requestAPI()
    }

    private fun requestAPI() {
        CoroutineScope(IO).launch {
            Log.d("MAIN", "fetch advice")
            val advice = async { fetchAdvice() }.await()
            if (advice?.isNotEmpty() == true) {
                updateTextView(advice)
            } else {
                Log.d("MAIN", "Unable to get data")
            }
        }
    }


    private fun fetchAdvice(): Names? {
        Log.d("MAIN", "went inside fetch")
        val apiInterface = APIClient().getClient()?.create(APIInterface::class.java)
        val call: Call<Names> = apiInterface!!.getAdvice()
        var advice: Names? = null
        try {
            val response = call.execute()
            advice = response.body()
            Log.d("MAIN", "read advice")
        } catch (e: Exception) {
            Log.d("MAIN", "ISSUE: $e")
        }


        Log.d("MAIN", "advice is ${advice.toString()}")
        return advice
    }

    private suspend fun updateTextView(advice: Names?) {
        withContext(Main) {
            for (i in advice!!) {
                messages.add(i.name)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private lateinit var adapter: Recycler
    private lateinit var messages: ArrayList<String>
    private fun initializeRecycler() {
        messages = ArrayList()
        adapter = Recycler(messages)
        binding.rvMain.adapter = adapter
        binding.rvMain.layoutManager = LinearLayoutManager(this)
    }

    private lateinit var binding: ActivityMainBinding
    private fun initializeBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}


object Constants {
    const val url = "https://dojo-recipes.herokuapp.com/"
    const val urlTag = "people/"
}

interface APIInterface {
    @GET(urlTag)
    fun getAdvice(): Call<Names>
}

class APIClient {
    private var retrofit: Retrofit? = null
    fun getClient(): Retrofit? {
        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit
    }
}

class Names : ArrayList<NamesItem>(){}

data class NamesItem(val name: String)


class Recycler(val messages: ArrayList<String>) : RecyclerView.Adapter<Recycler.ViewHolder>() {
    class ViewHolder(val binding: RecyclerLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.binding.apply {
            textView.text = message
        }
    }

    override fun getItemCount() = messages.size
}