package com.wanma.wzweather.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.wanma.wzweather.logic.dao.PlaceDao
import com.wanma.wzweather.logic.model.Place
import com.wanma.wzweather.logic.model.Weather
import com.wanma.wzweather.logic.network.WzWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

object Repository {

    //开启子线程，通过LiveData对象进行数据返回
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = WzWeatherNetwork.searchPlaces(query)
        if(placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        }else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                WzWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                WzWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if(realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            }else {
                Result.failure(RuntimeException("realtimeResponse status is ${realtimeResponse.status}" +
                        ",dailyResponse status is ${dailyResponse.status}"))
            }
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
            liveData<Result<T>>(context) {
                val result = try {
                    block()
                }catch (e: Exception) {
                    Result.failure<T>(e)
                }
                emit(result)
            }

    //    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
//        val result = try {
//            val placeResponse = WzWeatherNetwork.searchPlaces(query)
//            if(placeResponse.status == "ok") {
//                val places = placeResponse.places
//                Result.success(places)
//            }else {
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//        }catch (e: Exception) {
//            Result.failure<List<Place>>(e)
//        }
//        emit(result)
//    }

    //    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {
//        val result = try {
//            coroutineScope {
//                val deferredRealtime = async {
//                    WzWeatherNetwork.getRealtimeWeather(lng, lat)
//                }
//                val deferredDaily = async {
//                    WzWeatherNetwork.getDailyWeather(lng, lat)
//                }
//                val realtimeResponse = deferredRealtime.await()
//                val dailyResponse = deferredDaily.await()
//                if(realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
//                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
//                    Result.success(weather)
//                }else {
//                    Result.failure(RuntimeException("realtimeResponse status is ${realtimeResponse.status}" +
//                            ",dailyResponse status is ${dailyResponse.status}"))
//                }
//            }
//        }catch (e: Exception) {
//            Result.failure<Weather>(e)
//        }
//        emit(result)
//    }

}