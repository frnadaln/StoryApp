package com.dicoding.storyapp.views.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.adapter.StoryAdapter
import com.dicoding.storyapp.data.ListStory
import com.dicoding.storyapp.helper.DataDummy
import com.dicoding.storyapp.helper.MainDispatcherRule
import com.dicoding.storyapp.helper.getOrAwaitValue
import com.dicoding.storyapp.repository.Repository
import com.dicoding.storyapp.view.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {


    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock private lateinit var repository: Repository

    @Test
    fun `when get Stories Should Not Null and Return Data`() = runTest {
        val dummyStory = DataDummy.generateDummyStoryResponse()
        val data : PagingData<ListStory> = StoryPagingSource.snapshot(dummyStory)
        val expectData = MutableLiveData<PagingData<ListStory>>()
        expectData.value = data

        Mockito.`when`(repository.getAllStories()).thenReturn(expectData)

        val mainViewModel = MainViewModel(repository)
        val actualStory : PagingData<ListStory> = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStory.size, differ.snapshot().size)
        Assert.assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStory> = PagingData.from(emptyList())
        val expectedQuote = MutableLiveData<PagingData<ListStory>>()
        expectedQuote.value = data
        Mockito.`when`(repository.getAllStories()).thenReturn(expectedQuote)
        val mainViewModel = MainViewModel(repository)
        val actualQuote: PagingData<ListStory> = mainViewModel.story.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback{
    override fun onInserted(position: Int, count: Int) {
    }

    override fun onRemoved(position: Int, count: Int) {
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
    }

}

class StoryPagingSource : PagingSource<Int, ListStory>() {
    override fun getRefreshKey(state: PagingState<Int, ListStory>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStory> {
        return LoadResult.Page(emptyList(), prevKey = 0, nextKey = 1)
    }

    companion object {
        fun snapshot(items : List<ListStory>) : PagingData<ListStory> {
            return PagingData.from(items)
        }
    }
}