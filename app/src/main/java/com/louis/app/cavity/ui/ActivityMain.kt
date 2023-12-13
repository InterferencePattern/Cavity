package com.louis.app.cavity.ui

import android.app.ActivityManager.TaskDescription
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUiSaveStateControl
import androidx.navigation.ui.setupWithNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.ui.account.LoginViewModel
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.ui.tasting.TastingViewModel
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.showSnackbar
import com.louis.app.cavity.util.themeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityMain : AppCompatActivity(), SnackbarProvider {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: Fragment
    private val addItemViewModel: AddItemViewModel by viewModels()
    private val tastingViewModel: TastingViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val isAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        if (!isAndroid12) {
            setTheme(R.style.CavityTheme)
        } else {
            initSplashScreen()
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        polishAppSwitcherApparence()
        setupNavigation()
        observe()
        setupOnBackPressed()

        if (hasNavigationRail()) {
            lockDrawer()
        }
    }

    private fun initSplashScreen() {
        var isSplashScreenFinished = false

        installSplashScreen()

        lifecycleScope.launch(Dispatchers.Default) {
            delay(1900)
            isSplashScreenFinished = true
        }

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isSplashScreenFinished) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )
    }

    // We have to support old android 7.1 TaskDescription constructor
    @Suppress("deprecation")
    private fun polishAppSwitcherApparence() {
        val appName = getString(R.string.app_name)
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        setTaskDescription(TaskDescription(appName, bitmap, themeColor(com.google.android.material.R.attr.colorSurface)))
    }

    @OptIn(NavigationUiSaveStateControl::class)
    private fun setupNavigation() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
        val navController = navHostFragment.findNavController()

        binding.navView.apply {
            setupWithNavController(navController)
            setNavigationItemSelectedListener { item ->
                NavigationUI.onNavDestinationSelected(item, navController, false)
                binding.drawer.close()
                true
            }
        }

        binding.navigationRail?.apply {
            setOnItemSelectedListener {
                val fromHomeToHome =
                    (navController.currentDestination?.id ?: 0) == R.id.home_dest &&
                            it.itemId == R.id.home_dest

                if (fromHomeToHome) {
                    return@setOnItemSelectedListener false
                }

                NavigationUI.onNavDestinationSelected(it, navController, false)
                true
            }

            navController.addOnDestinationChangedListener { controller, _, _ ->
                updateNavigationRailState(controller)
            }
        }
    }

    private fun observe() {
        addItemViewModel.userFeedback.observe(this) {
            it.getContentIfNotHandled()?.let { stringRes ->
                onShowSnackbarRequested(stringRes)
            }
        }

        loginViewModel.userFeedback.observe(this) {
            it.getContentIfNotHandled()?.let { stringRes ->
                onShowSnackbarRequested(stringRes)
            }
        }

        loginViewModel.userFeedbackString.observe(this) {
            it.getContentIfNotHandled()?.let { string ->
                binding.main.coordinator.showSnackbar(string)
            }
        }

        tastingViewModel.undoneTastings.observe(this) { tastings ->
            val hasTastingToday = tastings.any { DateFormatter.isToday(it.tasting.date) }
            showTastingIndicator(hasTastingToday)
        }
    }

    private fun showTastingIndicator(show: Boolean) {
        if (hasNavigationRail()) {
            binding.navigationRail!!.getOrCreateBadge(R.id.tasting_dest).apply {
                backgroundColor = binding.navigationRail!!.context.themeColor(com.google.android.material.R.attr.colorPrimary)
                isVisible = show
            }
        } else {
            val tastingItem = binding.navView.menu.getItem(1)

            if (show) {
                tastingItem.setActionView(R.layout.dot)
            } else {
                tastingItem.actionView = null
            }
        }
    }

    private fun lockDrawer() {
        binding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun updateNavigationRailState(navController: NavController) {
        val destination = navController.currentDestination ?: return

        binding.navigationRail?.menu?.forEach { item ->
            val destinations = generateSequence(destination) { it.parent }
            if (destinations.any { it.id == item.itemId }) {
                item.isChecked = true
            }
        }
    }

    private fun hasNavigationRail() = binding.navigationRail != null

    fun requestMediaPersistentPermission(mediaUri: Uri, silent: Boolean = false) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        try {
            contentResolver?.takePersistableUriPermission(mediaUri, flags)
                ?: throw NullPointerException()
        } catch (e: SecurityException) {
            if (!silent) {
                onShowSnackbarRequested(R.string.persistent_permission_error)
            }
        } catch (e: NullPointerException) {
            if (!silent) {
                onShowSnackbarRequested(R.string.base_error)
            }
        }
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()

                if (hasNavigationRail()) {
                    val navController = navHostFragment.findNavController()
                    updateNavigationRailState(navController)
                }
            }
        }
    }

    override fun onShowSnackbarRequested(stringRes: Int) {
        val currentDestination = navHostFragment.findNavController().currentDestination
        val isHome = currentDestination?.id == R.id.home_dest
        val anchor = if (isHome) binding.main.snackbarAnchor else null

        binding.main.coordinator.showSnackbar(stringRes, anchorView = anchor)
    }
}
