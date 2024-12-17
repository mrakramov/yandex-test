package com.mrakramov.yandextest.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mrakramov.yandextest.R
import com.mrakramov.yandextest.databinding.FragmentHomeBinding
import com.mrakramov.yandextest.ui.home.adapter.LocationsListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: LocationsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpAdapter()
        setUpObserver()
    }

    private fun setUpAdapter() {
        adapter = LocationsListAdapter {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Manzil o'chirish")
                .setMessage("Siz rostan ham manzilni o'chirib yubormoqchimisiz ?")
                .setPositiveButton("Albatta") { v, _ ->
                    v.dismiss()
                    viewModel.deleteLocation(it)
                }.setNegativeButton("Bekor qilish") { v, _ ->
                    v.dismiss()
                }.show()
        }
        binding.rvLocations.adapter = adapter
    }

    private fun setUpObserver() {
        viewModel.screenState.flowWithLifecycle(
            viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
        ).onEach { state -> handleStateChange(state) }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleStateChange(state: MainScreenState) {

        if (state.loading) {
            binding.progress.visibility = View.VISIBLE
        } else {
            binding.progress.visibility = View.GONE
        }

        if (state.list.isNotEmpty()) {
            adapter.submitList(state.list)
        }

        if (!state.loading && state.list.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.tvEmpty.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}