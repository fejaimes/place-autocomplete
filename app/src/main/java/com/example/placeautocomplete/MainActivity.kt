package com.example.placeautocomplete

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var adapter: PredictionAdapter
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPlaces()
        initView()
    }

    private fun initView() {
        adapter = PredictionAdapter(this, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)
        compositeDisposable.add(
            autoCompleteTextView.textChanges()
                .subscribeOn(Schedulers.io())
                .filter {
                    it.length > 3
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    getPlacesAutocomplete(it.toString())
                }, { exception ->
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: ${exception.statusCode} ${exception.message}")
                    }
                })
        )
        autoCompleteTextView.setOnItemClickListener { adapterView, _, i, _ ->
            val itemSelected = adapterView.getItemAtPosition(i) as Prediction
            getPlaceDetailForId(itemSelected.id)
        }
    }

    private fun initPlaces() {
        // Initialize the SDK
        Places.initialize(this, "Your Key", Locale("es", "CO"))

        // Create a new Places client instance
        placesClient = Places.createClient(this)
    }

    private fun getPlacesAutocomplete(query: String) {
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()

        // Create a RectangularBounds object.
        /*val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),
            LatLng(-33.858754, 151.229596)
        )*/
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationBias(bounds) //.setLocationRestriction(bounds)
                .setCountry("CO")
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val data = mutableListOf<Prediction>()
                for (prediction in response.autocompletePredictions) {
                    val predictionItem = Prediction(
                        id = prediction.placeId,
                        textPrimary = prediction.getPrimaryText(null).toString(),
                        textSecondary = prediction.getSecondaryText(null).toString()
                    )
                    data.add(predictionItem)
                }
                adapter.setData(data)
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode} ${exception.message}")
                }
            }
    }

    private fun getPlaceDetailForId(placeId: String) {
        // Specify the fields to return.
        val placeFields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.i(TAG, "Place found: ${place.name}, ${place.address}, ${place.latLng}")
                Toast.makeText(
                    this,
                    "Place found: ${place.name}, ${place.address}, ${place.latLng}",
                    Toast.LENGTH_LONG
                ).show()
            }.addOnFailureListener { exception: java.lang.Exception ->
                if (exception is ApiException) {
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: ${exception.statusCode} ${exception.message}")
                }
            }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
