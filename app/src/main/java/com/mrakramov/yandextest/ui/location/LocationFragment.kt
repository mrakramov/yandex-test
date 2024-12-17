package com.mrakramov.yandextest.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mrakramov.yandextest.R
import com.mrakramov.yandextest.databinding.ConfirmationDialogBinding
import com.mrakramov.yandextest.databinding.FavoriteBottomSheetBinding
import com.mrakramov.yandextest.databinding.FavoriteLocationsBottomSheetBinding
import com.mrakramov.yandextest.databinding.FragmentDashboardBinding
import com.mrakramov.yandextest.domain.LocationEntity
import com.mrakramov.yandextest.ui.home.adapter.LocationsListAdapter
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class LocationFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var searchManager: SearchManager
    private val viewModel: LocationViewModel by viewModels()


    private val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) initLocation()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(requireContext())
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpObserver()
        setUpClicks()
//        initLocation()

    }

    private fun setUpClicks() {
        binding.fabLocation.setOnClickListener {
            initLocation()
        }

        binding.cardSearch.setOnClickListener {
            viewModel.loadLocations()
        }

    }

    private fun showLocations(list: List<LocationEntity>) {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomBinding = FavoriteLocationsBottomSheetBinding.inflate(
            LayoutInflater.from(requireContext()), null, false
        )
        bottomSheetDialog.behavior.peekHeight = 0
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetDialog.setContentView(bottomBinding.root)
        val adapter = LocationsListAdapter {

        }
        bottomBinding.rvLocation.adapter = adapter
        adapter.submitList(list)

        bottomBinding.etSearch.addTextChangedListener { text ->
            val searchedText = text.toString()
            val filtered = list.filter {
                it.name.lowercase().trim().contains(searchedText)
            }
            adapter.submitList(filtered)
        }

        bottomSheetDialog.show()
    }

    private fun setUpObserver() {
        viewModel.event.flowWithLifecycle(
            viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
        ).onEach { event -> handleEventChange(event) }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleEventChange(event: LocationEvents) {
        when (event) {
            is LocationEvents.Failure -> Toast.makeText(
                requireContext(), event.message, Toast.LENGTH_SHORT
            ).show()

            LocationEvents.SuccessfullyAdded -> Toast.makeText(
                requireContext(), "Successfully added !", Toast.LENGTH_SHORT
            ).show()

            is LocationEvents.ShowLocations -> showLocations(event.list)
        }
    }

    private val listener = CameraListener { map, cameraPosition, cameraUpdateReason, finished ->
        if (finished) {
            val centerPoint = cameraPosition.target
            reverseGeocode(centerPoint)
        }
    }

    private fun initLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissions.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }).addOnSuccessListener {
            moveToLocation(Point(it.latitude, it.longitude))
        }
    }

    private fun moveToLocation(point: Point) {
        binding.mapview.mapWindow.map.move(
            CameraPosition(point, 15.0f, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH, 1f), null
        )
    }

    private fun reverseGeocode(centerPoint: Point) {
        searchManager.submit(centerPoint, 16, SearchOptions(), object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                if (response.collection.children.isEmpty()) {
                    return
                }
                val metadata =
                    response.collection.children.firstOrNull()?.obj?.metadataContainer?.getItem(
                        ToponymObjectMetadata::class.java
                    )
                val buildingName = metadata?.formerName
                    ?: metadata?.address?.components?.find { component -> component.kinds.any { it?.name == "STREET" } }?.name
                    ?: metadata?.address?.components?.lastOrNull()?.name ?: "-"

                val formattedAddress = metadata?.address?.formattedAddress ?: ""

                showAddFavoriteDialog(buildingName, formattedAddress, centerPoint)
            }

            override fun onSearchError(p0: Error) {

            }
        })
    }

    private fun showAddFavoriteDialog(name: String, address: String, point: Point) {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomBinding =
            FavoriteBottomSheetBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        bottomSheetDialog.setContentView(bottomBinding.root)
        bottomBinding.tvName.text = name
        bottomBinding.tvAddress.text = address
        bottomBinding.tvRate.text = (1..1000).random().toString()

        bottomBinding.btnSave.setOnClickListener {
            bottomSheetDialog.dismiss()
            showConfirmationDialog(name, address, point)
        }

        bottomBinding.ibClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun showConfirmationDialog(name: String, address: String, point: Point) {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        val dialogBinding = ConfirmationDialogBinding.inflate(
            LayoutInflater.from(requireContext()), null, false
        )
        alertDialog.setView(dialogBinding.root)
        alertDialog.window?.setBackgroundDrawableResource(R.color.transparent)

        dialogBinding.etAddress.setText(address)

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        dialogBinding.btnConfirm.setOnClickListener {
            val text = dialogBinding.etAddress.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Addressni to'ldirish shart !", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            alertDialog.dismiss()
            viewModel.addFavoriteLocation(name, text, point)
        }
        alertDialog.show()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
        binding.mapview.mapWindow.map.addCameraListener(listener)
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        binding.mapview.mapWindow.map.removeCameraListener(listener)
        super.onStop()
    }


}