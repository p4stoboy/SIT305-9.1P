package sit305.a71p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import sit305.a71p.ui.theme._71PTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(applicationContext, "KEY")

        val itemDao = getDatabase(applicationContext).itemDao()
        val itemRepo = LostAndFoundItemRepo(itemDao, lifecycleScope)
        val lostAndFoundViewModel = LostAndFoundViewModel(itemRepo, applicationContext)

        setContent {
            _71PTheme {
                LostAndFoundApp(lostAndFoundViewModel)
            }
        }
    }
}

