package sit305.a71p

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LostAndFoundViewModel(
    private val itemRepo: LostAndFoundItemRepo,
    private val context: Context
) : ViewModel() {
    val items: StateFlow<List<LostAndFoundItem>> = itemRepo.allItems

    fun getItem(itemId: Int): LostAndFoundItem? {
        return items.value.find { it.id == itemId }
    }

    fun insertItem(item: LostAndFoundItem) = viewModelScope.launch {
        itemRepo.insert(item)
    }

    fun removeItem(item: LostAndFoundItem) = viewModelScope.launch {
        itemRepo.delete(item)
    }

    fun getCurrentLocation(callback: (LatLng?, String?) -> Unit) {
        // permission prompt is handled in Screen
        if (ContextCompat.checkSelfPermission(
                context,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val placesClient = Places.createClient(context)
            val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            placesClient.findCurrentPlace(request)
                .addOnSuccessListener { response ->
                    val placeLikelihoodList = response.placeLikelihoods
                    val placeLikelihood = placeLikelihoodList.firstOrNull()
                    val latLng = placeLikelihood?.place?.latLng
                    val placeName = placeLikelihood?.place?.name
                    callback(latLng, placeName)
                }
                .addOnFailureListener { exception ->
                    callback(null, null)
                }
        }
        // do nothing if location permission is not granted
    }
}
