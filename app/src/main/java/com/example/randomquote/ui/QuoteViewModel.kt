package com.example.randomquote.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.randomquote.data.QuoteRepository
import com.example.randomquote.model.Quote
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuoteCategoryFilter {
    ALL, FAVORITES
}

data class QuoteUiState(
    val currentQuote: Quote? = null,
    val allQuotes: List<Quote> = emptyList(),
    val favoriteQuotes: List<Quote> = emptyList(),
    val totalExploredCount: Int = 1,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val selectedCategory: String = "All",
    val availableCategories: List<String> = emptyList(),
    val isBrowseOpen: Boolean = false
)

sealed class QuoteUiEvent {
    data class ShowToast(val message: String) : QuoteUiEvent()
}

class QuoteViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuoteUiState())
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuoteUiEvent>()
    val events: SharedFlow<QuoteUiEvent> = _events.asSharedFlow()

    private val history = mutableListOf<Quote>()
    private var historyIndex = -1

    init {
        viewModelScope.launch {
            repository.quotes.collect { quotes ->
                val categories = listOf("All") + quotes.map { it.category }.distinct().sorted()
                val favorites = quotes.filter { it.isFavorite }
                
                val updatedCurrent = if (_uiState.value.currentQuote != null) {
                    quotes.find { it.id == _uiState.value.currentQuote?.id } ?: _uiState.value.currentQuote
                } else {
                    quotes.firstOrNull()
                }

                if (history.isEmpty() && updatedCurrent != null) {
                    history.add(updatedCurrent)
                    historyIndex = 0
                }

                _uiState.update { state ->
                    state.copy(
                        currentQuote = updatedCurrent,
                        allQuotes = quotes,
                        favoriteQuotes = favorites,
                        availableCategories = categories,
                        canGoBack = historyIndex > 0,
                        canGoForward = historyIndex < history.size - 1
                    )
                }
            }
        }
    }

    fun nextRandomQuote() {
        val currentId = _uiState.value.currentQuote?.id
        val newQuote = repository.getRandomQuote(excludeId = currentId)
        
        // If we navigated back, trim forward history
        if (historyIndex < history.size - 1) {
            while (history.size > historyIndex + 1) {
                history.removeAt(history.size - 1)
            }
        }
        history.add(newQuote)
        historyIndex = history.size - 1

        _uiState.update { state ->
            state.copy(
                currentQuote = newQuote,
                totalExploredCount = state.totalExploredCount + 1,
                canGoBack = historyIndex > 0,
                canGoForward = false
            )
        }
    }

    fun previousQuote() {
        if (historyIndex > 0) {
            historyIndex--
            val quote = history[historyIndex]
            _uiState.update { state ->
                state.copy(
                    currentQuote = quote,
                    canGoBack = historyIndex > 0,
                    canGoForward = historyIndex < history.size - 1
                )
            }
        }
    }

    fun forwardQuote() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            val quote = history[historyIndex]
            _uiState.update { state ->
                state.copy(
                    currentQuote = quote,
                    canGoBack = historyIndex > 0,
                    canGoForward = historyIndex < history.size - 1
                )
            }
        }
    }

    fun selectQuote(quote: Quote) {
        history.add(quote)
        historyIndex = history.size - 1
        _uiState.update { state ->
            state.copy(
                currentQuote = quote,
                totalExploredCount = state.totalExploredCount + 1,
                canGoBack = historyIndex > 0,
                canGoForward = false,
                isBrowseOpen = false
            )
        }
    }

    fun toggleFavorite(quoteId: Int) {
        repository.toggleFavorite(quoteId)
    }

    fun toggleBrowseSheet(open: Boolean? = null) {
        _uiState.update { state ->
            state.copy(isBrowseOpen = open ?: !state.isBrowseOpen)
        }
    }

    fun copyCurrentQuote(context: Context) {
        val quote = _uiState.value.currentQuote ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Quote", "\"${quote.text}\" — ${quote.category}")
        clipboard.setPrimaryClip(clip)
        viewModelScope.launch {
            _events.emit(QuoteUiEvent.ShowToast("Quote copied to clipboard!"))
        }
    }

    fun shareCurrentQuote(context: Context) {
        val quote = _uiState.value.currentQuote ?: return
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\"\n\n— ${quote.category} Principle (via Random Quote)")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Principle")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
