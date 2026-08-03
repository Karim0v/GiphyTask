package com.nursulton.giphytask.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nursulton.giphytask.feature.details.ui.DetailsScreenRoute
import com.nursulton.giphytask.feature.details.viewmodel.DetailsViewModel
import com.nursulton.giphytask.feature.search.ui.SearchScreenRoute
import com.nursulton.giphytask.feature.search.viewmodel.SearchViewModel

sealed class Screen(val route: String) {
    data object Search : Screen("search")
    data object Details : Screen("details/{${DetailsViewModel.KEY_GIF_ID}}") {
        fun createRoute(gifId: String) = "details/$gifId"
    }
}

@Composable
fun GiphyNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Search.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Search.route) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreenRoute(
                viewModel = viewModel,
                onGifClick = { gif ->
                    navController.navigate(Screen.Details.createRoute(gif.id))
                }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument(DetailsViewModel.KEY_GIF_ID) { type = NavType.StringType }
            )
        ) {
            val viewModel: DetailsViewModel = hiltViewModel()
            DetailsScreenRoute(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
