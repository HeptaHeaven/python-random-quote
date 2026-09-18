package com.example.randomquote.data

import android.content.Context
import android.content.SharedPreferences
import com.example.randomquote.model.Quote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QuoteRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("random_quote_prefs", Context.MODE_PRIVATE)

    private val defaultQuotes = listOf(
        Quote(1, "Responsive is better than fast", "Performance"),
        Quote(2, "It’s not fully shipped until it’s fast", "Performance"),
        Quote(3, "Anything added dilutes everything else", "Simplicity"),
        Quote(4, "Practicality beats purity", "Simplicity"),
        Quote(5, "Approachable is better than simple", "Simplicity"),
        Quote(6, "Mind your words, they are important", "Communication"),
        Quote(7, "Speak like a human", "Communication"),
        Quote(8, "Half measures are as bad as nothing at all", "Craftsmanship"),
        Quote(9, "Encourage flow", "Craftsmanship"),
        Quote(10, "Non-blocking is better than blocking", "Architecture"),
        Quote(11, "Favor focus over features", "Focus"),
        Quote(12, "Avoid administrative distraction", "Focus"),
        Quote(13, "Design for failure", "Architecture"),
        Quote(14, "Keep it logically awesome", "Philosophy")
    )

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    init {
        loadQuotes()
    }

    private fun loadQuotes() {
        val favoriteIds = prefs.getStringSet("favorites", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        _quotes.value = defaultQuotes.map { quote ->
            quote.copy(isFavorite = favoriteIds.contains(quote.id))
        }
    }

    fun toggleFavorite(quoteId: Int) {
        val currentFavorites = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        val idString = quoteId.toString()
        if (currentFavorites.contains(idString)) {
            currentFavorites.remove(idString)
        } else {
            currentFavorites.add(idString)
        }
        prefs.edit().putStringSet("favorites", currentFavorites).apply()

        _quotes.update { currentList ->
            currentList.map { quote ->
                if (quote.id == quoteId) {
                    quote.copy(isFavorite = !quote.isFavorite)
                } else {
                    quote
                }
            }
        }
    }

    fun getRandomQuote(excludeId: Int? = null): Quote {
        val list = _quotes.value
        if (list.isEmpty()) return defaultQuotes.first()
        if (list.size == 1) return list.first()

        val candidates = if (excludeId != null) list.filter { it.id != excludeId } else list
        return candidates.randomOrNull() ?: list.random()
    }
}
