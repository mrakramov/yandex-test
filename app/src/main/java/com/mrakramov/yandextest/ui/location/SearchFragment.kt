package com.mrakramov.yandextest.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.mrakramov.yandextest.databinding.FragmentSearchBinding
import com.mrakramov.yandextest.domain.SearchItem
import com.mrakramov.yandextest.ui.location.adapter.SearchLocationsAdapter
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchManager: SearchManager
    private lateinit var adapter: SearchLocationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpAdapter()
        setUpListeners()

    }

    private fun setUpAdapter() {
        adapter = SearchLocationsAdapter {
            setFragmentResult("selectedLocation", Bundle().apply {
                putParcelable("location", it)
            })
            findNavController().popBackStack()

        }
        binding.rvLocation.adapter = adapter
    }

    private fun setUpListeners() {
        binding.etSearch.addTextChangedListener {
            val value = it.toString()
            if (value.isEmpty()) return@addTextChangedListener
            searchQuery(value)
        }
    }

    private fun searchQuery(query: String) {
        searchManager.submit(query,
            Geometry.fromPoint(Point(0.0, 0.0)),
            SearchOptions(),
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val searchResults = mutableListOf<SearchItem>()
                    for (searchResult in response.collection.children) {
                        val point = searchResult.obj?.geometry?.get(0)?.point
                        val name = searchResult.obj?.name ?: "-"
                        if (point != null) {
                            searchResults.add(SearchItem(name, point.latitude, point.longitude))
                        }
                    }
                    adapter.submitList(searchResults)
                }

                override fun onSearchError(p0: com.yandex.runtime.Error) {
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}