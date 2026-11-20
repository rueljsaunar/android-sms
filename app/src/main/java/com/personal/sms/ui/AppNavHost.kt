package com.personal.sms.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.personal.sms.di.AppContainer
import com.personal.sms.ui.conversations.ArchivedScreen
import com.personal.sms.ui.conversations.ConversationsScreen
import com.personal.sms.ui.conversations.ConversationsViewModel
import com.personal.sms.ui.newmessage.NewMessageScreen
import com.personal.sms.ui.newmessage.NewMessageViewModel
import com.personal.sms.ui.onboarding.DefaultSmsOnboardingScreen
import com.personal.sms.ui.search.SearchScreen
import com.personal.sms.ui.search.SearchViewModel
import com.personal.sms.ui.thread.ThreadScreen
import com.personal.sms.ui.thread.ThreadViewModel

object Routes {
    const val CONVERSATIONS = "conversations"
    const val NEW_MESSAGE = "new_message"
    const val SEARCH = "search"
    const val ONBOARDING = "onboarding"
    const val ARCHIVED = "archived"
    const val THREAD = "thread/{threadId}?address={address}"
    fun thread(threadId: Long, address: String) = "thread/$threadId?address=${Uri.encode(address)}"
}

@Composable
fun AppNavHost(
    container: AppContainer,
    navController: NavHostController,
    onRequestDefault: () -> Unit,
) {
    NavHost(navController = navController, startDestination = Routes.CONVERSATIONS) {

        composable(Routes.CONVERSATIONS) {
            val vm: ConversationsViewModel =
                viewModel(factory = ConversationsViewModel.factory(container))
            ConversationsScreen(
                viewModel = vm,
                onOpenThread = { id, address -> navController.navigate(Routes.thread(id, address)) },
                onCompose = { navController.navigate(Routes.NEW_MESSAGE) },
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenOnboarding = { navController.navigate(Routes.ONBOARDING) },
                onOpenArchived = { navController.navigate(Routes.ARCHIVED) },
            )
        }

        composable(
            route = Routes.THREAD,
            arguments = listOf(
                navArgument("threadId") { type = NavType.LongType },
                navArgument("address") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { entry ->
            val threadId = entry.arguments?.getLong("threadId") ?: 0L
            val address = Uri.decode(entry.arguments?.getString("address").orEmpty())
            val vm: ThreadViewModel = viewModel(
                key = "thread_${threadId}_$address",
                factory = ThreadViewModel.factory(container, threadId, address.ifEmpty { null }),
            )
            ThreadScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.NEW_MESSAGE) {
            val vm: NewMessageViewModel =
                viewModel(factory = NewMessageViewModel.factory(container))
            NewMessageScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onStartThread = { address ->
                    navController.navigate(Routes.thread(0L, address)) {
                        popUpTo(Routes.NEW_MESSAGE) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.SEARCH) {
            val vm: SearchViewModel = viewModel(factory = SearchViewModel.factory(container))
            SearchScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenThread = { id, address -> navController.navigate(Routes.thread(id, address)) },
            )
        }

        composable(Routes.ARCHIVED) {
            val vm: ConversationsViewModel =
                viewModel(factory = ConversationsViewModel.factory(container))
            ArchivedScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenThread = { id, address -> navController.navigate(Routes.thread(id, address)) },
            )
        }

        composable(Routes.ONBOARDING) {
            DefaultSmsOnboardingScreen(
                onSetDefault = onRequestDefault,
                onNotNow = { navController.popBackStack() },
            )
        }
    }
}
