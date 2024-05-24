package sit305.a71p

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LostAndFoundScreen(navController: NavController, viewModel: LostAndFoundViewModel) {
    val items by viewModel.items.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_advert") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Advert")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Lost and Found") },
                actions = {
                    TextButton(
                        onClick = { navController.navigate("map") }
                    ) {
                        Text("Show on Map")
                    }
                }
            )
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(0.dp, 50.dp)) {
            items(items = items, key = { item -> item.id }) { item ->
                LostAndFoundItem(navController, viewModel, item = item)
            }
        }
    }
}

@Composable
fun LostAndFoundItem(nc: NavController, viewModel: LostAndFoundViewModel, item: LostAndFoundItem) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = item.postType.toString(), modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
        Text(text = item.name + " (" + item.location + ")", modifier = Modifier.fillMaxWidth())
        Text(text = "Date: " + item.date, modifier = Modifier.fillMaxWidth(), fontSize = 12.sp)
        Button(onClick = {
            nc.navigate("item_details/${item.id}")
        },
            modifier = Modifier.fillMaxWidth()) {
            Text("View Details")
        }
    }
}

@Composable
fun CreateAdvertScreen(nc: NavController, viewModel: LostAndFoundViewModel) {
    val postType = remember { mutableStateOf(PostType.LOST) }
    val name = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    // super verbose closure for requesting location permission
    // actually a terrible pattern but everything else I could find was experimental or depricated
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.getCurrentLocation { latLng, placeName ->
                    latLng?.let {
                        latitude = it.latitude
                        longitude = it.longitude
                    }
                    locationText = placeName ?: "Unknown Place"
                }
            }
        }
    )

    // I actually hate this but all the established patterns for making a nicer autocomplete UX are even more verbose
    // this is honestly the first time I've been frustrated with compose / android in general - compose falls over a bit when there's no SDK composable to wrap this in
    // and I am forced to think more deeply about what I am doing (lol)
    // it's fine I just got used to the declarative wrappers everywhere
    val placesAutocompleteIntent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
            .build(LocalContext.current)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // not handling anything lol i'm tired and this is doing my head in
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            place.let {
                locationText = place.name!!
                latitude = place.latLng?.latitude!!
                longitude = place.latLng?.longitude!!
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            RadioButton(
                selected = postType.value == PostType.LOST,
                onClick = { postType.value = PostType.LOST }
            )
            Text("Lost")
            RadioButton(
                selected = postType.value == PostType.FOUND,
                onClick = { postType.value = PostType.FOUND }
            )
            Text("Found")
        }
        TextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = phone.value,
            onValueChange = { phone.value = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = date.value,
            onValueChange = { date.value = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { launcher.launch(placesAutocompleteIntent) }
        ) {
            Text(
                text = locationText.ifEmpty { "Select location" },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { launcher.launch(placesAutocompleteIntent) }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
        Row {
            Button(
                onClick = {
                    permissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            ) {
                Text("Get Current Location")
            }
        }
        Button(onClick = {
            val item = LostAndFoundItem(
                postType = postType.value,
                name = name.value,
                phone = phone.value,
                description = description.value,
                date = date.value,
                location = locationText,
                latitude = latitude,
                longitude = longitude
            )
            viewModel.insertItem(item)
            nc.navigateUp()
        }) {
            Text("Save")
        }
        Button(onClick = {
            nc.navigateUp()
        }) {
            Text("Cancel")
        }
    }
}

@Composable
fun ItemDetailsScreen(nc: NavController, viewModel: LostAndFoundViewModel, itemId: Int) {
    val item = viewModel.getItem(itemId)

    if (item != null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Post Type: ${item.postType}", modifier = Modifier.fillMaxWidth())
            Text(text = "Name: ${item.name}", modifier = Modifier.fillMaxWidth())
            Text(text = "Phone: ${item.phone}", modifier = Modifier.fillMaxWidth())
            Text(text = "Description: ${item.description}", modifier = Modifier.fillMaxWidth())
            Text(text = "Date: ${item.date}", modifier = Modifier.fillMaxWidth())
            Text(text = "Location: ${item.location}", modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                nc.navigateUp()
            }) {
                Text("Back")
            }
            Button(onClick = {
                viewModel.removeItem(item)
                nc.navigateUp()
            }) {
                Text("Remove")
            }
        }
    } else {
        Text("Item not found")
    }
}

@Composable
fun MapScreen(navController: NavController, viewModel: LostAndFoundViewModel) {
    // overkill
    val items by viewModel.items.collectAsState(initial = emptyList())

    // start over australia
    val aus = LatLng(-25.2744, 133.7751)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(aus, 10f)
    }

    if (items.isNotEmpty()) {
        val latLngBuilder = LatLngBounds.Builder()
        items.forEach { item ->
            latLngBuilder.include(LatLng(item.latitude, item.longitude))
        }
        val latLngBounds = latLngBuilder.build()

        LaunchedEffect(items) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        items.forEach { item ->
            Marker(
                state = MarkerState(position = LatLng(item.latitude, item.longitude)),
                title = item.name,
                snippet = item.location
            )
        }
    }
}
