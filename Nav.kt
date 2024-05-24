package sit305.a71p

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun LostAndFoundApp(viewModel: LostAndFoundViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "lost_and_found") {
        composable("lost_and_found") {
            LostAndFoundScreen(navController, viewModel)
        }
        composable("create_advert") {
            CreateAdvertScreen(navController, viewModel)
        }
        composable(
            route = "item_details/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId")
            requireNotNull(itemId) { "Item ID is required." }
            ItemDetailsScreen(navController, viewModel, itemId)
        }
        composable("map") {
            MapScreen(navController, viewModel)
        }
    }
}
